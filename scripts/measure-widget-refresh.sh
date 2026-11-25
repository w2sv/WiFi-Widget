#!/usr/bin/env bash
set -euo pipefail

# Save as scripts/measure-widget-refresh.sh in the WiFi Widget repository.
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

PACKAGE=com.w2sv.wifiwidget
APK=app/build/outputs/apk/release/app-release.apk
OUT="build/widget-refresh-measurements/$(date +%Y%m%d-%H%M%S)"
EXPIRE_SECONDS="${EXPIRE_SECONDS:-60}"
OBSERVE_SECONDS="${OBSERVE_SECONDS:-15}"
TAG=WidgetRefreshMeasurement
SCENARIO_NUMBER=0
FAILED_ATTEMPTS=0
PASSED_SCENARIOS=0
LAST_PID=
LAST_WIDGET=
LAST_CALLBACK_LINE=0
PRIME_READY_LINE=0
LOGCAT_PID=
APP_LOGCAT_PID=

[[ -x ./gradlew ]] || { echo 'Run from a checkout with this script in scripts/.' >&2; exit 1; }
command -v adb >/dev/null
command -v unzip >/dev/null
adb get-state >/dev/null
[[ "$EXPIRE_SECONDS" =~ ^[1-9][0-9]*$ ]] || { echo "EXPIRE_SECONDS must be a positive integer." >&2; exit 1; }
[[ "$OBSERVE_SECONDS" =~ ^[1-9][0-9]*$ ]] || { echo "OBSERVE_SECONDS must be a positive integer." >&2; exit 1; }
mkdir -p "$OUT"

echo 'Building optimized release with diagnostic logging...'
./gradlew :app:assembleRelease -PenableDiagnosticLogging=true
[[ -f "$APK" ]] || { echo "Signed release APK not found: $APK" >&2; exit 1; }
unzip -Z1 "$APK" | grep -x 'assets/dexopt/baseline.prof' >/dev/null || {
    echo 'APK does not contain a Baseline Profile.' >&2; exit 1;
}
adb install -r "$APK"

echo 'Installing and compiling Baseline Profile...'
profile_result="$(adb shell am broadcast \
    -a androidx.profileinstaller.action.INSTALL_PROFILE \
    -n "$PACKAGE/androidx.profileinstaller.ProfileInstallReceiver")"
printf '%s\n' "$profile_result" | tee "$OUT/profile-install.txt"
if ! grep -Eq 'result=(1|2)([^0-9]|$)' <<< "$profile_result"; then
    echo 'Profile installation did not report success or already-installed status.' >&2
    exit 1
fi
adb shell am force-stop "$PACKAGE"
compile_result="$(adb shell cmd package compile -f -m speed-profile "$PACKAGE")"
printf '%s\n' "$compile_result" | tee "$OUT/profile-compile.txt"
if ! grep -q 'Success' <<< "$compile_result"; then
    echo 'Profile-guided compilation did not report success.' >&2; exit 1
fi
# Retain only the exact package section (not the debug variant or other apps).
adb shell dumpsys package dexopt | awk -v package="$PACKAGE" '
    { sub(/\r$/, "") }
    /^  \[[^]]+\]/ { selected = ($0 == "  [" package "]") }
    selected { print }
' > "$OUT/dexopt.txt"
cat "$OUT/dexopt.txt"
if ! grep -q 'status=speed-profile' "$OUT/dexopt.txt"; then
    echo 'Warning: speed-profile status could not be verified in dexopt output.' >&2
fi

{
    echo "Device: $(adb shell getprop ro.product.model)"
    echo "Android: $(adb shell getprop ro.build.version.release)"
    echo "Battery Saver (low_power): $(adb shell settings get global low_power)"
    adb shell dumpsys battery
} | tee "$OUT/device.txt"

# Resolve the installed app UID for filtering across process restarts.
USER_ID="$(adb shell am get-current-user | tr -d '\r')"
APP_UID="$(adb shell cmd package list packages -U --user "$USER_ID" "$PACKAGE" | awk -v package="$PACKAGE" '
    $1 == "package:" package { sub(/\r$/, "", $2); sub(/^uid:/, "", $2); print $2 }
')"
[[ "$APP_UID" =~ ^[0-9]+$ ]] || { echo 'Could not resolve application UID.' >&2; exit 1; }
printf 'Application UID: %s\n' "$APP_UID" | tee -a "$OUT/device.txt"

echo 'Starting continuous system and application log capture...'
cleanup() {
    local pid
    for pid in "$LOGCAT_PID" "$APP_LOGCAT_PID"; do
        [[ -n "$pid" ]] || continue
        kill "$pid" 2>/dev/null || true
        wait "$pid" 2>/dev/null || true
    done
    echo "Measurements saved to $ROOT/$OUT"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
adb logcat -b main -b system -b crash -v threadtime -T 1 > "$OUT/logcat.txt" 2> "$OUT/logcat-errors.txt" &
LOGCAT_PID=$!
adb logcat -b main -b system -b crash -v threadtime -T 1 --uid="$APP_UID" > "$OUT/app-logcat.txt" 2> "$OUT/app-logcat-errors.txt" &
APP_LOGCAT_PID=$!
printf 'scenario\tstatus\tcallbacks\tcallback_errors\n' > "$OUT/scenario-results.tsv"

check_capture() {
    if ! kill -0 "$LOGCAT_PID" 2>/dev/null || ! kill -0 "$APP_LOGCAT_PID" 2>/dev/null; then
        echo 'Log capture stopped unexpectedly. Inspect the logcat error files.' >&2
        exit 1
    fi
}

mark() {
    adb shell log -p i -t "$TAG" "$1" || { echo "Failed to write log marker: $1" >&2; exit 1; }
}
prompt() {
    read -r -p "$1" _ || { echo "Input closed; ending measurement." >&2; exit 1; }
}
countdown() {
    local remaining="$1" label="$2"
    while (( remaining > 0 )); do
        if [[ -t 1 ]]; then
            printf '\r%s: %3ds remaining' "$label" "$remaining"
        else
            printf '%s: %3ds remaining\n' "$label" "$remaining"
        fi
        sleep 1
        remaining=$((remaining - 1))
    done
    if [[ -t 1 ]]; then
        printf '\r%s: done.                  \n' "$label"
    else
        printf '%s: done.\n' "$label"
    fi
}
observe() {
    countdown "$OBSERVE_SECONDS" 'Capturing logs (no interaction needed)'
}
# Wait for the marker to reach the host file before inspecting the scenario.
wait_for_marker() {
    local marker="$1" remaining=5
    until grep -Eq "[[:space:]]${TAG}: ${marker}[[:space:]]*$" "$OUT/logcat.txt"; do
        check_capture
        if (( remaining == 0 )); then
            echo "Missing log marker: $marker" >&2
            return 1
        fi
        countdown 1 "Waiting for log marker $marker ($remaining attempts left)"
        remaining=$((remaining - 1))
    done
}

# Use the app log's stable line positions and the callback's PID/widget ID to
# distinguish the measured session from other widgets and earlier sessions.
marker_time() {
    awk -v tag="$TAG" -v marker="$1" '
        $0 ~ ("[[:space:]]" tag ": " marker "[[:space:]]*$") { print substr($0, 1, 18); exit }
    ' "$OUT/logcat.txt"
}

# Returns callback count, error count, PID, widget ID, callback line, first
# consumption line, successful immediate worker, new session, cold process.
refresh_stats() {
    local begin="$1" end="${2:-}"
    awk -v begin="$begin" -v end="$end" '
        /^[0-9][0-9]-[0-9][0-9] / {
            ts = substr($0, 1, 18)
            if (ts < begin || (end != "" && ts > end)) next
            if (index($0, "Process " $3 " created for com.w2sv.wifiwidget")) created[$3]=1
            if (index($0, "BroadcastReceiver execution failed") ||
                (index($0, "NoSuchMethodException") && index($0, "RefreshWidgetAction"))) errors++
            if (index($0, "Manual widget refresh received:")) {
                callbacks++
                if (callbacks == 1 && match($0, /appWidgetId=[0-9]+/)) {
                    pid=$3; widget=substr($0, RSTART+12, RLENGTH-12); callback=NR
                }
            }
            if (!callback || $3 != pid) next
            if (index($0, "appWidgetId=" widget ")")) {
                if (index($0, "Widget provideGlance invoked:")) new_session=1
                if (index($0, "Widget presentation consumed:") && !consumed) consumed=NR
            }
            if (!work && index($0, "Enqueueing widget refresh: reason=Immediate") && match($0, /workId=[a-fA-F0-9-]+/))
                work=substr($0, RSTART+7, RLENGTH-7)
            if (work && index($0, "Widget refresh finished: reason=Immediate") &&
                index($0, "workId=" work ",") && index($0, "result=Success")) success=1
        }
        END { print callbacks+0, errors+0, pid+0, widget+0, callback+0, consumed+0, success+0, new_session+0, created[pid]+0 }
    ' "$OUT/app-logcat.txt"
}

# The latest lifecycle event wins: an unrelated close or a session which has
# since been reopened must not terminate the expiration countdown.
session_state() {
    local pid="$1" widget="$2" after="$3" before="${4:-0}"
    awk -v pid="$pid" -v widget="$widget" -v after="$after" -v before="$before" '
        NR > after && (before == 0 || NR < before) && $3 == pid {
            if (index($0, "GlanceSessionManager:") && index($0, "Closing session appWidget-" widget " ")) state="closed"
            if (index($0, "Widget provideGlance invoked:") && index($0, "appWidgetId=" widget ")")) state="open"
        }
        END { print state == "" ? "unknown" : state }
    ' "$OUT/app-logcat.txt"
}

status_message() {
    local kind="$1" message="$2" color
    case "$kind" in
        SUCCESS) color=32 ;;
        ERROR) color=31 ;;
        *) color=33 ;;
    esac
    if [[ -t 1 && -z "${NO_COLOR:-}" && "${TERM:-}" != dumb ]]; then
        printf '\033[%sm[%s]\033[0m %s\n' "$color" "$kind" "$message"
    else
        printf '[%s] %s\n' "$kind" "$message"
    fi
}

report_result() {
    local scenario="$1" status="$2" callbacks="$3" errors="$4" message="$5"
    printf '%s\t%s\t%s\t%s\n' "$scenario" "$status" "$callbacks" "$errors" >> "$OUT/scenario-results.tsv"
    if [[ "$status" == SUCCESS ]]; then
        status_message SUCCESS "$scenario: $message"
        [[ "$scenario" == PRIME_SESSION_* ]] || PASSED_SCENARIOS=$((PASSED_SCENARIOS + 1))
        return 0
    fi
    status_message ERROR "$scenario: $message"
    FAILED_ATTEMPTS=$((FAILED_ATTEMPTS + 1))
    return 1
}

# Priming only needs to establish a usable session. Do not spend the full
# observation window waiting before the measured active-session tap.
observe_prime() {
    local begin="$1" remaining="$OBSERVE_SECONDS" stats callbacks errors pid widget callback consumed success new_session cold
    while :; do
        check_capture
        stats="$(refresh_stats "$begin")"
        read -r callbacks errors pid widget callback consumed success new_session cold <<< "$stats"
        if (( errors > 0 || callbacks > 1 || (callbacks == 1 && consumed > 0 && success == 1) )); then
            return 0
        fi
        (( remaining > 0 )) || return 0
        countdown 1 "Waiting for priming refresh ($remaining seconds until timeout)"
        remaining=$((remaining - 1))
    done
}

wait_for_session_termination() {
    local remaining="$EXPIRE_SECONDS" state
    while :; do
        check_capture
        if [[ -n "$LAST_PID" && -n "$LAST_WIDGET" ]]; then
            state="$(session_state "$LAST_PID" "$LAST_WIDGET" "$LAST_CALLBACK_LINE")"
            if [[ "$state" == closed ]]; then
                if [[ -t 1 ]]; then printf '\r\033[K'; fi
                status_message SUCCESS "Glance session appWidget-$LAST_WIDGET terminated (PID $LAST_PID)."
                return 0
            fi
        fi
        if (( remaining == 0 )); then
            if [[ -t 1 ]]; then printf '\r\033[K'; fi
            status_message WARNING "No session termination detected within ${EXPIRE_SECONDS}s; continuing with the expired-session test."
            return 0
        fi
        if [[ -t 1 ]]; then
            printf '\rWaiting for Glance session termination: %3ds until timeout' "$remaining"
        else
            printf 'Waiting for Glance session termination: %3ds until timeout\n' "$remaining"
        fi
        sleep 1
        remaining=$((remaining - 1))
    done
}

validate_refresh() {
    local scenario="$1" begin="$2" end="$3" stats callbacks errors pid widget callback consumed success new_session cold
    local previous_pid="$LAST_PID" previous_widget="$LAST_WIDGET" previous_callback="$LAST_CALLBACK_LINE"
    stats="$(refresh_stats "$begin" "$end")"
    read -r callbacks errors pid widget callback consumed success new_session cold <<< "$stats"
    if (( callbacks == 1 && callback > 0 )); then
        LAST_PID="$pid"; LAST_WIDGET="$widget"; LAST_CALLBACK_LINE="$callback"
    fi
    if (( errors > 0 )); then
        report_result "$scenario" CALLBACK_ERROR "$callbacks" "$errors" 'Glance callback failed. Inspect app-logcat.txt.'
    elif (( callbacks != 1 || callback == 0 )); then
        report_result "$scenario" INVALID_CALLBACK_COUNT "$callbacks" "$errors" 'Expected exactly one identifiable manual refresh callback.'
    elif (( consumed == 0 || success == 0 )); then
        report_result "$scenario" INCOMPLETE "$callbacks" "$errors" 'No presentation consumption or successful immediate worker within the observation window.'
    elif [[ "$scenario" == PRIME_SESSION_* ]]; then
        PRIME_READY_LINE="$consumed"
        report_result "$scenario" SUCCESS "$callbacks" "$errors" 'Priming callback, presentation and immediate worker completed.'
    elif [[ "$scenario" == ACTIVE_SESSION_* ]] && {
        [[ "$pid" != "$previous_pid" || "$widget" != "$previous_widget" ]] ||
        (( new_session != 0 )) ||
        [[ "$(session_state "$pid" "$widget" "$PRIME_READY_LINE" "$callback")" != unknown ]];
    }; then
        report_result "$scenario" SESSION_CHANGED "$callbacks" "$errors" 'Glance session changed after priming; not a valid active-session measurement.'
    elif [[ "$scenario" == EXPIRED_SESSION_* ]] && {
        (( new_session == 0 )) ||
        [[ -n "$previous_pid" && ( "$pid" != "$previous_pid" || "$widget" != "$previous_widget" ) ]];
    }; then
        report_result "$scenario" SESSION_NOT_VERIFIED "$callbacks" "$errors" 'Expected a new Glance session in the same process for the same widget.'
    elif [[ "$scenario" == COLD_PROCESS_* ]] && (( cold == 0 )); then
        report_result "$scenario" PROCESS_NOT_COLD "$callbacks" "$errors" 'No process creation logged before the callback.'
    else
        report_result "$scenario" SUCCESS "$callbacks" "$errors" 'Callback, presentation and immediate worker completed; scenario validated.'
    fi
}

validate_startup() {
    local scenario="$1" begin="$2" end="$3"
    if awk -v begin="$begin" -v end="$end" '
        /^[0-9][0-9]-[0-9][0-9] / {
            ts=substr($0,1,18)
            if (ts < begin || ts > end) next
            if (index($0,"Process " $3 " created for com.w2sv.wifiwidget")) created[$3]=1
            if (created[$3] && index($0,"VRI[MainActivity]:")) started=1
        }
        END { exit !started }
    ' "$OUT/app-logcat.txt"; then
        report_result "$scenario" SUCCESS - - 'New process and MainActivity startup observed.'
    else
        report_result "$scenario" STARTUP_NOT_VERIFIED - - 'No MainActivity startup in a newly created process detected.'
    fi
}

record() {
    local scenario="$1" message="$2" mode="${3:-refresh}" begin end
    SCENARIO_NUMBER=$((SCENARIO_NUMBER + 1))
    scenario="${scenario}_${SCENARIO_NUMBER}"
    check_capture
    mark "BEGIN_$scenario"
    if ! wait_for_marker "BEGIN_$scenario"; then
        report_result "$scenario" UNVERIFIED - - 'Missing start marker.'
        return 1
    fi
    begin="$(marker_time "BEGIN_$scenario")"
    prompt "$message Press Enter immediately afterward; do not wait for a visible change. "
    mark "USER_CONFIRMED_$scenario"
    if [[ "$mode" == prime ]]; then observe_prime "$begin"; else observe; fi
    mark "END_$scenario"
    check_capture
    if ! wait_for_marker "END_$scenario"; then
        report_result "$scenario" UNVERIFIED - - 'Missing end marker.'
        return 1
    fi
    end="$(marker_time "END_$scenario")"
    if [[ "$mode" == startup ]]; then
        validate_startup "$scenario" "$begin" "$end"
    else
        validate_refresh "$scenario" "$begin" "$end"
    fi
}

kill_background() {
    adb shell am kill "$PACKAGE" || { echo "Failed to request process termination." >&2; exit 1; }
    countdown 1 'Waiting for process termination'
    local pid
    pid="$(adb shell pidof "$PACKAGE" || true)"
    adb get-state >/dev/null || { echo "ADB device disconnected." >&2; exit 1; }
    if [[ -n "$pid" ]]; then
        echo "Process still running ($pid). Active work may be preventing termination."
        return 1
    fi
}

report_setup_failure() {
    local scenario="$1" message="$2"
    SCENARIO_NUMBER=$((SCENARIO_NUMBER + 1))
    report_result "${scenario}_${SCENARIO_NUMBER}" SETUP_FAILED - - "$message"
}

# Retry the entire scenario, not just its final tap. Each record() invocation
# has a fresh marker pair and result row; failed attempts remain available.
retry_until_success() {
    local title="$1" action="$2" attempt=1
    echo
    echo "=== $title ==="
    until "$action"; do
        check_capture
        attempt=$((attempt + 1))
        status_message WARNING "Retrying $title (attempt $attempt)."
    done
}

active_session() {
    record PRIME_SESSION 'On the launcher, tap refresh once to prime the session.' prime || return 1
    record ACTIVE_SESSION 'Tap refresh again immediately (on the same widget).'
}

expired_session() {
    wait_for_session_termination
    record EXPIRED_SESSION 'Tap the same widget refresh button.'
}

cold_process() {
    prompt 'Return to the launcher and press Enter to kill the background process. '
    if ! kill_background; then
        report_setup_failure COLD_PROCESS 'Background process did not terminate.'
        return 1
    fi
    record COLD_PROCESS 'Tap the same widget refresh button.'
}

cold_app_startup() {
    prompt 'Return to the launcher and press Enter to kill the background process. '
    if ! kill_background; then
        report_setup_failure COLD_APP_STARTUP 'Background process did not terminate.'
        return 1
    fi
    record COLD_APP_STARTUP 'Open the app normally.' startup
}

mark SESSION_START
wait_for_marker SESSION_START
prompt 'Disable Battery Saver, open the release app normally, ensure a widget exists, and enable remote properties if testing HTTP. Return home and press Enter. '

retry_until_success '1/4: Active Glance session' active_session
retry_until_success '2/4: Expired Glance session' expired_session
retry_until_success '3/4: Cold process' cold_process
retry_until_success '4/4: Cold app startup with installed widget' cold_app_startup

mark SESSION_END
wait_for_marker SESSION_END
printf '\nMeasurement summary: %s/4 successful; %s failed attempts before success.\n' "$PASSED_SCENARIOS" "$FAILED_ATTEMPTS"
status_message SUCCESS 'All four scenarios validated.'
