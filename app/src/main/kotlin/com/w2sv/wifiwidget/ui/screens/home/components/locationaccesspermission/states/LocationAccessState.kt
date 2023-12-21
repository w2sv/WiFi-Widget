package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.androidutils.datastorage.datastore.preferences.PersistedValue
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.di.MutableSharedSnackbarVisualsFlow
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionStatus
import com.w2sv.wifiwidget.ui.utils.FlowCollectionEffect
import com.w2sv.wifiwidget.ui.utils.SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT
import com.w2sv.wifiwidget.ui.utils.isLaunchingSuppressed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationAccessPermissionState(
    permissionRepository: PermissionRepository,
    saveLocationAccessPermissionRequestLaunched: () -> Unit,
    saveLocationAccessRationalShown: () -> Unit,
    mutableSharedSnackbarVisuals: MutableSharedSnackbarVisualsFlow,
    scope: CoroutineScope = rememberCoroutineScope(),
): LocationAccessState {

    // Necessary evil
    val requestResult = remember {
        MutableSharedFlow<Boolean>()
    }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
        onPermissionsResult = {
            scope.launch {
                requestResult.emit(it.values.all { it })
            }
        }
    )

    val backgroundAccessPermissionState = if (backgroundLocationAccessGrantRequired)
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    else
        null

    val state = remember(scope, permissionRepository, mutableSharedSnackbarVisuals) {
        LocationAccessState(
            permissionsState = permissionState,
            backgroundAccessState = backgroundAccessPermissionState?.let {
                BackgroundLocationAccessState(
                    permissionState = it,
                    scope = scope
                )
            },
            requestLaunchedBefore = permissionRepository.locationAccessPermissionRequested,
            saveRequestLaunched = saveLocationAccessPermissionRequestLaunched,
            rationalShown = permissionRepository.locationAccessPermissionRationalShown,
            saveRationalShown = saveLocationAccessRationalShown,
            mutableSharedSnackbarVisuals = mutableSharedSnackbarVisuals,
            scope = scope
        )
    }

    // Emit new status on state.isGranted change
    LaunchedEffect(state.isGranted) {
        i { "New location access permission grant status=${state.isGranted}" }
        state.emitNewStatus(state.isGranted)
    }

    // Forward requestResult to state
    FlowCollectionEffect(requestResult) {
        state.onRequestResult(it)
    }

    return state
}

@OptIn(ExperimentalPermissionsApi::class)
@Stable
class LocationAccessState(
    private val permissionsState: MultiplePermissionsState,
    val backgroundAccessState: BackgroundLocationAccessState?,
    requestLaunchedBefore: PersistedValue.UniTyped<Boolean>,
    private val saveRequestLaunched: () -> Unit,
    rationalShown: PersistedValue.UniTyped<Boolean>,
    private val saveRationalShown: () -> Unit,
    private val mutableSharedSnackbarVisuals: MutableSharedSnackbarVisualsFlow,
    private val scope: CoroutineScope
) {
    val isGranted: Boolean by permissionsState::allPermissionsGranted

    val newStatus get() = _newStatus.asSharedFlow()
    private val _newStatus =
        MutableSharedFlow<LocationAccessPermissionStatus>()

    fun emitNewStatus(granted: Boolean) {
        scope.launch {
            _newStatus.emit(
                when (granted) {
                    true -> LocationAccessPermissionStatus.Granted(
                        requestTrigger.also {
                            requestTrigger = null
                        }
                    )

                    false -> LocationAccessPermissionStatus.NotGranted
                }
                    .also {
                        i { "Emitted newStatus=${it}" }
                    }
            )
        }
    }

    // ===================
    // Requesting
    // ===================

    private val requestLaunchedBefore = requestLaunchedBefore.stateIn(scope, SharingStarted.Eagerly)

    fun launchRequest(trigger: LocationAccessPermissionRequestTrigger) {
        if (permissionsState.isLaunchingSuppressed(requestLaunchedBefore.value)) {
            scope.launch {
                mutableSharedSnackbarVisuals.emit {
                    AppSnackbarVisuals(
                        msg = it.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                        kind = SnackbarKind.Error,
                        action = SnackbarAction(
                            label = it.getString(R.string.go_to_settings),
                            callback = {
                                goToAppSettings(it)
                            },
                        ),
                    )
                }
            }
        } else {
            requestTrigger = trigger
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    private var requestTrigger: LocationAccessPermissionRequestTrigger? = null

    fun onRequestResult(granted: Boolean) {
        if (!requestLaunchedBefore.value) {
            saveRequestLaunched()
        }
        if (granted) {
            backgroundAccessState?.showRationalIfPermissionNotGranted()
        }
    }

    // ===================
    // Rational
    // ===================

    val showRational = rationalShown.stateIn(
        scope,
        SharingStarted.WhileSubscribed(
            SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT
        )
    )
        .mapState { !it }

    fun onRationalShown() {
        saveRationalShown()
        launchRequest(trigger = LocationAccessPermissionRequestTrigger.InitialAppLaunch)
    }
}
