package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.material3.SnackbarHostState
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.data.storage.PreferencesRepository
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LocationAccessPermissionUIState(
    private val preferencesRepository: PreferencesRepository,
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    context: Context
) {
    val newlyGranted = MutableSharedFlow<Boolean>()

    fun onGranted() {
        scope.launch {
            newlyGranted.emit(true)
        }
        if (backgroundLocationAccessGrantRequired) {
            showBackgroundAccessRational.value = true
        }
    }

    suspend fun onRequestLaunchingSuppressed(context: Context) {
        snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
            AppSnackbarVisuals(
                context.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                kind = SnackbarKind.Error,
                action = SnackbarAction(
                    label = context.getString(R.string.go_to_settings),
                    callback = {
                        goToAppSettings(context)
                    }
                )
            )
        )
    }

    val rationalTriggeringAction: MutableStateFlow<LocationAccessPermissionRequiringAction?> =
        MutableStateFlow(null)

    val rationalShown: Boolean
        get() = preferencesRepository.locationAccessPermissionRationalShown.getValueSynchronously()

    fun onRationalShown() {
        scope.launch {
            preferencesRepository.saveLocationAccessPermissionRationalShown(true)
        }
        val triggeringAction = rationalTriggeringAction.value
        rationalTriggeringAction.value = null
        requestLaunchingAction.value = triggeringAction
    }

    val requestLaunchingAction: MutableStateFlow<LocationAccessPermissionRequiringAction?> =
        MutableStateFlow(null)

    val requestLaunched: Boolean
        get() = preferencesRepository.locationAccessPermissionRequested.getValueSynchronously()

    fun onRequestLaunched() {
        scope.launch {
            preferencesRepository.saveLocationAccessPermissionRequestedAtLeastOnce(true)
        }
    }

    val showBackgroundAccessRational = MutableStateFlow(false)

    var backgroundLocationAccessGranted =
        hasBackgroundLocationAccess(context)
        private set

    fun updateBackgroundAccessGranted(context: Context) {
        if (!backgroundLocationAccessGranted && hasBackgroundLocationAccess(context)) {
            scope.launch {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        message = context.getString(R.string.your_ssid_bssid_can_now_be_reliably_retrieved_from_the_background),
                        kind = SnackbarKind.Success
                    )
                )
            }
            backgroundLocationAccessGranted = true
        }
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val backgroundLocationAccessGrantRequired: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun hasBackgroundLocationAccess(context: Context): Boolean =
    !backgroundLocationAccessGrantRequired || context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)