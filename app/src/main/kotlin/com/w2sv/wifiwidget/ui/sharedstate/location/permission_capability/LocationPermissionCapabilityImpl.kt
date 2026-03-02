package com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.w2sv.composed.permissions.extensions.isLaunchingSuppressed
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarAction
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGrant
import com.w2sv.wifiwidget.ui.util.SnackbarBuilder

@Stable
class LocationPermissionCapabilityImpl(
    private val foregroundPermissionsState: MultiplePermissionsState,
    private val backgroundPermissionState: PermissionState?,
    private val requestLaunchedBefore: () -> Boolean,
    private val saveRequestLaunchedBefore: () -> Unit,
    rationalAlreadyShown: Boolean,
    private val saveRationalShown: () -> Unit,
    private val showSnackbar: (SnackbarBuilder) -> Unit,
    private val openAppSettings: () -> Unit
): LocationPermissionCapability {
    override val foregroundPermissionsGranted: Boolean by foregroundPermissionsState::allPermissionsGranted
    override val isBackgroundPermissionMissing: Boolean
        get() = backgroundPermissionState?.status?.isGranted == false

    // ========= Foreground Rational =========

    override var showForegroundRational by mutableStateOf(!rationalAlreadyShown)
        private set

    override fun onForegroundRationalProceed() {
        saveRationalShown()
        showForegroundRational = false
        launchForegroundPermission()
    }

    // ========= Background Rational =========

    override var showBackgroundRational by mutableStateOf(false)
        private set

    override fun maybeShowBackgroundRational() {
        if (isBackgroundPermissionMissing) {
            showBackgroundRational = true
        }
    }

    override fun launchBackgroundPermission() {
        backgroundPermissionState?.launchPermissionRequest()
        showBackgroundRational = false
    }

    override fun dismissBackgroundRational() {
        showBackgroundRational = false
    }

    // ========= Grant Actions =========

    private var pendingOnGrantAction by mutableStateOf<OnLocationAccessGrant?>(null)

    override fun requestPermission(onGrant: OnLocationAccessGrant?) {
        pendingOnGrantAction = onGrant

        when {
            foregroundPermissionsGranted -> return
            isLaunchingSuppressed -> showSettingsSnackbar()
            else -> launchForegroundPermission()
        }
    }

    override fun consumeOnGrantAction(): OnLocationAccessGrant? =
        pendingOnGrantAction.also { pendingOnGrantAction = null }

    // ========= Internal Helpers =========

    private fun launchForegroundPermission() {
        if (!requestLaunchedBefore()) {
            saveRequestLaunchedBefore()
        }
        foregroundPermissionsState.launchMultiplePermissionRequest()
    }

    private val isLaunchingSuppressed: Boolean
        get() = foregroundPermissionsState.isLaunchingSuppressed(requestLaunchedBefore())

    private fun showSettingsSnackbar() {
        showSnackbar {
            AppSnackbarVisuals(
                msg = getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                kind = SnackbarKind.Warning,
                action = SnackbarAction(
                    label = getString(R.string.go_to_app_settings),
                    callback = openAppSettings
                )
            )
        }
    }
}
