#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./release.sh patch
#   ./release.sh minor
#   ./release.sh major
#
# This script:
# - fetches tags from origin
# - determines the next version from the latest semver tag
# - updates VERSION_NAME in gradle.properties
# - increments VERSION_CODE in gradle.properties

GRADLE_PROPERTIES_FILE="gradle.properties"
RELEASE_NOTES_FILE="app/src/main/play/release-notes/en-US/production.txt"
VERSION_NAME_KEY="version"
VERSION_CODE_KEY="versionCode"
MAX_RELEASE_NOTES_LENGTH=500

usage() {
  echo "Usage: $0 {major|minor|patch}"
  exit 1
}

ensure_clean_format() {
  local version="$1"
  if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: latest tag '$version' is not a valid semver tag of form X.Y.Z" >&2
    exit 1
  fi
}

ensure_git_repo_clean() {
  if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Error: git working tree is not clean. Commit or stash changes first." >&2
    exit 1
  fi
}

edit_release_notes() {
  echo "Opening release notes: $RELEASE_NOTES_FILE"
  echo "Write your release notes (max ${MAX_RELEASE_NOTES_LENGTH} chars), save, and exit."
  echo

  "${EDITOR:-nano}" "$RELEASE_NOTES_FILE"

  # Remove empty lines
  RELEASE_NOTES="$(sed '/^\s*$/d' "$RELEASE_NOTES_FILE")"

  if [[ -z "$RELEASE_NOTES" ]]; then
    echo "Error: release notes are empty. Aborting."
    exit 1
  fi

  # Count characters (portable)
  local length
  length="$(printf "%s" "$RELEASE_NOTES" | wc -c | tr -d ' ')"

  if (( length > MAX_RELEASE_NOTES_LENGTH )); then
    echo "Error: release notes too long (${length}/${MAX_RELEASE_NOTES_LENGTH} chars)."
    echo "Please shorten them."
    exit 1
  fi

  echo "Release notes length OK (${length}/${MAX_RELEASE_NOTES_LENGTH})"
}

get_latest_tag() {
  git tag --list \
    | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' \
    | sort -V \
    | tail -n 1
}

increment_version() {
  local version="$1"
  local release_type="$2"

  local major minor patch
  IFS='.' read -r major minor patch <<< "$version"

  case "$release_type" in
    major)
      major=$((major + 1))
      minor=0
      patch=0
      ;;
    minor)
      minor=$((minor + 1))
      patch=0
      ;;
    patch)
      patch=$((patch + 1))
      ;;
    *)
      usage
      ;;
  esac

  echo "${major}.${minor}.${patch}"
}

read_property() {
  local key="$1"
  local file="$2"

  local value
  value="$(grep -E "^${key}=" "$file" | head -n 1 | cut -d'=' -f2- || true)"

  if [[ -z "$value" ]]; then
    echo "Error: could not find property '${key}' in ${file}" >&2
    exit 1
  fi

  echo "$value"
}

update_property() {
  local key="$1"
  local new_value="$2"
  local file="$3"

  if grep -qE "^${key}=" "$file"; then
    sed -i "s/^${key}=.*/${key}=${new_value}/" "$file"
  else
    echo "Error: property '${key}' not found in ${file}" >&2
    exit 1
  fi
}

pre_push_check() {
  echo
  echo "=========== Diff ==========="

  git --no-pager diff --cached
  echo

  echo "Proceed with commit, tag and push?"
  echo

  read -r -p "Type 'y' to continue or anything else to abort " response
  if [[ "$response" != "y" ]]; then
      echo "Aborting release."
      exit 1
  fi

  echo
}

run() {
  echo "+ $*"
  "$@"
}

main() {
  local release_type="${1:-}"
  [[ -n "$release_type" ]] || usage

  # Run checks
  ensure_git_repo_clean

  echo "Run test build"
  ./gradlew build

  # Determine last tag
  echo "Fetching tags from origin..."
  git fetch --tags origin

  local latest_tag
  latest_tag="$(get_latest_tag)"

  if [[ -z "$latest_tag" ]]; then
    echo "No existing semver tags found. Starting from 0.0.1"
    latest_tag="0.0.1"
  fi

  ensure_clean_format "$latest_tag"
  echo "Latest tag: $latest_tag"

  # Determine new version
  local new_version
  new_version="$(increment_version "$latest_tag" "$release_type")"

  # Determine new versionCode
  local current_version_code
  current_version_code="$(read_property "$VERSION_CODE_KEY" "$GRADLE_PROPERTIES_FILE")"

  if [[ ! "$current_version_code" =~ ^[0-9]+$ ]]; then
    echo "Error: ${VERSION_CODE_KEY} must be numeric, got '${current_version_code}'" >&2
    exit 1
  fi

  local new_version_code=$((current_version_code + 1))

  # Write version & versionCode to gradle.properties
  update_property "$VERSION_NAME_KEY" "$new_version" "$GRADLE_PROPERTIES_FILE"
  update_property "$VERSION_CODE_KEY" "$new_version_code" "$GRADLE_PROPERTIES_FILE"

  echo "Updated ${GRADLE_PROPERTIES_FILE}:"
  echo "  ${VERSION_NAME_KEY}=${new_version}"
  echo "  ${VERSION_CODE_KEY}=${new_version_code}"

  # Edit release notes
  edit_release_notes

  pre_push_check

  # Create commit, tag, and push
  run git add .
  run git commit -m "chore: prepare ${new_version}"
  run git tag "$new_version"
  run git push
  run git push origin "$new_version"

  # Build & publish bundle
  make build-aab
  make publish-bundle
}

main "$@"
