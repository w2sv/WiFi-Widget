package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states

import androidx.compose.runtime.Stable
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import slimber.log.i

@Stable
class LocationAccessPermissionState(
    private val preferencesRepository: PreferencesRepository,
    private val scope: CoroutineScope,
) {
    val isGranted get() = _isGranted.asStateFlow()
    private val _isGranted = MutableStateFlow(false)

    fun setIsGranted(value: Boolean) {
        _isGranted.value = value
        i { "Set isGranted=$value" }
    }

    // ===================
    // Request triggering
    // ===================

    val requestTrigger get() = _requestTrigger.asSharedFlow()
    private val _requestTrigger =
        MutableSharedFlow<LocationAccessPermissionRequestTrigger>()

    private var previousRequestTrigger: LocationAccessPermissionRequestTrigger? = null

    fun launchRequest(trigger: LocationAccessPermissionRequestTrigger) {
        scope.launch {
            _requestTrigger.emit(trigger)
            previousRequestTrigger = trigger
        }
    }

    val requestLaunchedBefore = preferencesRepository.locationAccessPermissionRequested.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly
    )

    fun onRequestResult(granted: Boolean) {
        i { "onRequestResult | granted=$granted" }
        if (!requestLaunchedBefore.value) {
            scope.launch {
                preferencesRepository.locationAccessPermissionRequested.save(true)
            }
        }
        if (granted) {
            previousRequestTrigger?.let {
                scope.launch {
                    _grantTrigger.emit(it)
                    i { "Emitted grantTrigger=$it" }
                }
            }
            backgroundAccessState?.showRational(true)
        }
    }

    val grantTrigger get() = _grantTrigger.asSharedFlow()
    private val _grantTrigger = MutableSharedFlow<LocationAccessPermissionRequestTrigger>()

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
        launchRequest(LocationAccessPermissionRequestTrigger.InitialAppLaunch)
    }

    val backgroundAccessState =
        if (backgroundLocationAccessGrantRequired)
            BackgroundLocationAccessPermissionState(scope)
        else
            null
}
