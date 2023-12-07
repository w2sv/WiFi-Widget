package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.common.utils.trigger
import com.w2sv.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationAccessPermissionState(
    private val preferencesRepository: PreferencesRepository,
    private val scope: CoroutineScope,
    private val context: Context,
) {
    val newlyGranted get() = _newlyGranted.asSharedFlow()
    private val _newlyGranted = MutableSharedFlow<Unit>()

    fun onGranted() {
        scope.launch {
            _newlyGranted.trigger()
        }
        if (backgroundLocationAccessGrantRequired) {
            _showBackgroundAccessRational.value = true
        }
    }

    val rationalTriggeringAction get() = _rationalTriggeringAction.asStateFlow()
    private val _rationalTriggeringAction: MutableStateFlow<LocationAccessPermissionRequiringAction?> =
        MutableStateFlow(null)

    fun setRationalTriggeringAction(value: LocationAccessPermissionRequiringAction?) {
        _rationalTriggeringAction.value = value
    }

    val rationalShown = preferencesRepository.locationAccessPermissionRationalShown.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly
    )

    fun onRationalShown() {
        scope.launch {
            preferencesRepository.locationAccessPermissionRationalShown.save(true)
        }
        setRequestLaunchingAction(rationalTriggeringAction.value)
        setRationalTriggeringAction(null)
    }

    val requestLaunchingAction get() = _requestLaunchingAction.asStateFlow()
    private val _requestLaunchingAction: MutableStateFlow<LocationAccessPermissionRequiringAction?> =
        MutableStateFlow(null)

    fun setRequestLaunchingAction(value: LocationAccessPermissionRequiringAction?) {
        _requestLaunchingAction.value = value
    }

    val requestLaunched = preferencesRepository.locationAccessPermissionRequested.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly
    )

    fun onRequestLaunched() {
        scope.launch {
            preferencesRepository.locationAccessPermissionRequested.save(true)
        }
    }

    val showBackgroundAccessRational get() = _showBackgroundAccessRational.asStateFlow()
    private val _showBackgroundAccessRational = MutableStateFlow(false)

    fun setShowBackgroundAccessRational(value: Boolean) {
        _showBackgroundAccessRational.value = value
    }

    private var backgroundAccessGranted = hasBackgroundLocationAccess(context)

    val launchBackgroundAccessPermissionRequest
        get() = _launchBackgroundLocationAccessPermissionRequest.asSharedFlow()
    private val _launchBackgroundLocationAccessPermissionRequest = MutableSharedFlow<Unit>()

    fun launchBackgroundAccessPermissionRequest() {
        scope.launch {
            _launchBackgroundLocationAccessPermissionRequest.trigger()
        }
    }

    /**
     * @return Boolean, representing whether access has been newly granted.
     */
    fun updateBackgroundAccessGranted(context: Context = this.context): Boolean {
        if (!backgroundAccessGranted && hasBackgroundLocationAccess(context)) {
            backgroundAccessGranted = true
            return true
        }
        return false
    }
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun hasBackgroundLocationAccess(context: Context): Boolean =
    !backgroundLocationAccessGrantRequired || context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
