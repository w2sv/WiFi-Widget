package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.coroutines.mapState
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
) {
    val isGranted get() = _isGranted.asStateFlow()
    private val _isGranted = MutableStateFlow(false)
        .also {
            scope.collectFromFlow(it) { isGrantedCollected ->
                if (isGrantedCollected) {
                    requestTrigger.value?.let { nonNullRequestTrigger ->
                        _grantInducingTrigger.emit(nonNullRequestTrigger)
                        _requestTrigger.value = null
                        if (backgroundLocationAccessGrantRequired) {
                            _showBackgroundAccessRational.value = true
                        }
                    }
                }
            }
        }

    fun setIsGranted(value: Boolean) {
        _isGranted.value = value
    }

    val grantInducingTrigger get() = _grantInducingTrigger.asSharedFlow()
    private val _grantInducingTrigger = MutableSharedFlow<LocationAccessPermissionRequestTrigger>()

    // ===================
    // Request triggering
    // ===================

    val requestTrigger get() = _requestTrigger.asStateFlow()
    private val _requestTrigger = MutableStateFlow<LocationAccessPermissionRequestTrigger?>(null)

    fun setRequestTrigger(value: LocationAccessPermissionRequestTrigger?) {
        _requestTrigger.value = value
    }

    val requestLaunchedBefore = preferencesRepository.locationAccessPermissionRequested.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly
    )

    fun onRequestLaunched() {
        if (!requestLaunchedBefore.value) {
            preferencesRepository.locationAccessPermissionRequested.launchSave(true, scope)
        }
    }

    // ===================
    // Rational
    // ===================

    val showRational = preferencesRepository.locationAccessPermissionRationalShown
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly
        )
        .mapState {
            !it
        }

    fun onRationalShown() {
        scope.launch {
            preferencesRepository.locationAccessPermissionRationalShown.save(true)
        }
        setRequestTrigger(LocationAccessPermissionRequestTrigger.InitialAppEntry)
    }

    // ===================
    // Background access rational
    // ===================

    val showBackgroundAccessRational get() = _showBackgroundAccessRational.asStateFlow()
    private val _showBackgroundAccessRational = MutableStateFlow(false)

    fun dismissBackgroundAccessRational() {
        _showBackgroundAccessRational.value = false
    }

    // ===================
    // Background access requesting
    // ===================

    val launchBackgroundAccessRequest
        get() = _launchBackgroundAccessRequest.asSharedFlow()
    private val _launchBackgroundAccessRequest = MutableSharedFlow<Unit>()

    fun launchBackgroundAccessPermissionRequest() {
        scope.launch {
            _launchBackgroundAccessRequest.trigger()
        }
    }
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun hasBackgroundLocationAccess(context: Context): Boolean =
    !backgroundLocationAccessGrantRequired || context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
