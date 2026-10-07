package com.w2sv.wifiwidget.ui.sharedstate.location.capability.permission

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.w2sv.augmentedpermissions.PermissionState
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarAction
import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGranted
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Stable
class LocationPermissionCapabilityImpl(
    private val foregroundPermissionsState: PermissionState,
    private val backgroundPermissionState: PermissionState?,
    private val rationalShown: () -> Boolean,
    private val saveRationalShown: () -> Unit,
    private val showSnackbar: (SnackbarBuilder) -> Unit,
    private val openAppSettings: () -> Unit
) : LocationPermissionCapability {
    override val foregroundPermissionsGranted: Boolean by foregroundPermissionsState::isGranted
    override val isBackgroundPermissionMissing: Boolean
        get() = backgroundPermissionState?.isGranted == false

    private val _grantEvents = MutableSharedFlow<OnLocationAccessGranted>()
    override val grantEvents: Flow<OnLocationAccessGranted> = _grantEvents.asSharedFlow()

    // ========= Foreground Rational =========

    override val showForegroundRational get() = !rationalShown()

    override fun onForegroundRationalProceed() {
        saveRationalShown()
        requestPermission(EnableLocationAccessRequiringProperties)
    }

    // ========= Background Rational =========

    override var showBackgroundRational by mutableStateOf(false)
        private set

    private fun maybeShowBackgroundRational() {
        if (isBackgroundPermissionMissing) {
            showBackgroundRational = true
        }
    }

    override fun launchBackgroundPermission() {
        dismissBackgroundRational()
        backgroundPermissionState?.launchRequest()
    }

    override fun dismissBackgroundRational() {
        showBackgroundRational = false
    }

    // ========= Grant Actions =========

    private var pendingOnGrantAction: OnLocationAccessGranted? = null
    override fun requestPermission(onGrant: OnLocationAccessGranted?) {
        pendingOnGrantAction = onGrant
        foregroundPermissionsState.launchRequest(::showSettingsSnackbar)
    }

    override suspend fun onPermissionGranted() {
        pendingOnGrantAction?.let {
            _grantEvents.emit(it)
        }
        pendingOnGrantAction = null
        maybeShowBackgroundRational()
    }

    // ========= Internal Helpers =========

    private fun showSettingsSnackbar() {
        showSnackbar {
            AppSnackbarVisuals(
                msg = getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                kind = Warning,
                action = SnackbarAction(
                    label = getString(R.string.go_to_app_settings),
                    callback = openAppSettings
                )
            )
        }
    }
}
