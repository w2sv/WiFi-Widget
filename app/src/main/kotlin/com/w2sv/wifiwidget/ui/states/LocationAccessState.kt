package com.w2sv.wifiwidget.ui.states

import android.Manifest
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.openAppSettings
import com.w2sv.composed.CollectFromFlow
import com.w2sv.composed.OnChange
import com.w2sv.composed.permissions.extensions.isLaunchingSuppressed
import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.kotlinutils.coroutines.mapState
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarAction
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.LocationAccessPermissionStatus
import com.w2sv.wifiwidget.ui.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i

@Composable
fun rememberLocationAccessState(
    appVM: AppViewModel = hiltViewModel(),
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    context: Context = LocalContext.current
): LocationAccessState =
    rememberLocationAccessState(
        requestLaunchedBefore = appVM.locationAccessPermissionRequested,
        saveRequestLaunchedBefore = appVM::saveLocationAccessPermissionRequested,
        rationalShown = appVM.locationAccessRationalShown,
        saveRationalShown = appVM::saveLocationAccessRationalShown,
        scope = scope,
        snackbarHostState = snackbarHostState,
        context = context
    )

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationAccessState(
    requestLaunchedBefore: DataStoreFlow<Boolean>,
    saveRequestLaunchedBefore: () -> Unit,
    rationalShown: DataStoreFlow<Boolean>,
    saveRationalShown: () -> Unit,
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    context: Context = LocalContext.current
): LocationAccessState {
    // Necessary evil
    val requestResult = remember {
        MutableSharedFlow<Boolean>()
    }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onPermissionsResult = {
            scope.launch {
                requestResult.emit(it.values.all { it })
            }
        }
    )

    val backgroundAccessPermissionState = if (backgroundLocationAccessGrantRequired) {
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        null
    }

    val state = remember(scope, snackbarHostState, context) {
        LocationAccessState(
            permissionsState = permissionState,
            backgroundAccessState = backgroundAccessPermissionState?.let {
                BackgroundLocationAccessState(
                    permissionState = it,
                    scope = scope
                )
            },
            requestLaunchedBefore = requestLaunchedBefore,
            saveRequestLaunched = saveRequestLaunchedBefore,
            rationalShown = rationalShown,
            saveRationalShown = saveRationalShown,
            snackbarHostState = snackbarHostState,
            scope = scope,
            context = context
        )
    }

    // Emit new status on state.isGranted change
    OnChange(state.isGranted) {
        i { "New location access permission grant status=$it" }
        state.emitNewStatus(it)
    }

    // Forward requestResult to state
    CollectFromFlow(requestResult) {
        state.onRequestResult(it)
    }

    return state
}

@OptIn(ExperimentalPermissionsApi::class)
@Stable
class LocationAccessState(
    permissionsState: MultiplePermissionsState,
    val backgroundAccessState: BackgroundLocationAccessState?,
    requestLaunchedBefore: DataStoreFlow<Boolean>,
    private val saveRequestLaunched: () -> Unit,
    rationalShown: DataStoreFlow<Boolean>,
    private val saveRationalShown: () -> Unit,
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val context: Context
) : MultiplePermissionsState by permissionsState {

    val isGranted: Boolean by ::allPermissionsGranted

    val newStatus get() = _newStatus.asSharedFlow()
    private val _newStatus =
        MutableSharedFlow<LocationAccessPermissionStatus>()

    internal fun emitNewStatus(granted: Boolean) {
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
                        i { "Emitted newStatus=$it" }
                    }
            )
        }
    }

    // ===================
    // Requesting
    // ===================

    private val requestLaunchedBefore = requestLaunchedBefore.stateIn(scope, SharingStarted.Eagerly)

    fun launchRequest(trigger: LocationAccessPermissionRequestTrigger) {
        if (isLaunchingSuppressed(requestLaunchedBefore.value)) {
            scope.launch {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                        kind = SnackbarKind.Warning,
                        action = SnackbarAction(
                            label = context.getString(R.string.go_to_app_settings),
                            callback = {
                                context.openAppSettings()
                            }
                        )
                    )
                )
            }
        } else {
            requestTrigger = trigger
            launchMultiplePermissionRequest()
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

    val showRational = rationalShown
        .stateIn(
            scope,
            SharingStarted.Eagerly
        )
        .mapState { !it }

    fun onRationalShown() {
        saveRationalShown()
        launchRequest(trigger = LocationAccessPermissionRequestTrigger.InitialAppLaunch)
    }
}
