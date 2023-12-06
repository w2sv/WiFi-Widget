package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationAccessPermissionState(
    private val preferencesRepository: PreferencesRepository,
    private val _launchBackgroundLocationAccessPermissionRequest: MutableSharedFlow<Unit>,
    private val scope: CoroutineScope,
    private val context: Context,
) {
    val newlyGranted get() = _newlyGranted.asSharedFlow()
    private val _newlyGranted = MutableSharedFlow<Unit>()

    fun onGranted() {
        scope.launch {
            _newlyGranted.emit(Unit)
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

    val rationalShown: Boolean
        get() = preferencesRepository.locationAccessPermissionRationalShown.getValueSynchronously()

    fun onRationalShown() {
        scope.launch {
            preferencesRepository.locationAccessPermissionRationalShown.save(true)
        }
        val triggeringAction = rationalTriggeringAction.value
        _rationalTriggeringAction.value = null
        _requestLaunchingAction.value = triggeringAction
    }

    val requestLaunchingAction get() = _requestLaunchingAction.asStateFlow()
    private val _requestLaunchingAction: MutableStateFlow<LocationAccessPermissionRequiringAction?> =
        MutableStateFlow(null)

    fun setRequestLaunchingAction(value: LocationAccessPermissionRequiringAction?) {
        _requestLaunchingAction.value = value
    }

    val requestLaunched: Boolean
        get() = preferencesRepository.locationAccessPermissionRequested.getValueSynchronously()

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

    fun launchBackgroundAccessPermissionRequest() {
        scope.launch {
            _launchBackgroundLocationAccessPermissionRequest.emit(Unit)
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
