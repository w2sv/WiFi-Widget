package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states

import android.Manifest
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.androidutils.datastorage.datastore.DataStoreFlow
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarAction
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionStatus
import com.w2sv.wifiwidget.ui.utils.CollectFromFlow
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

    val state = remember(scope, permissionRepository, snackbarHostState, context) {
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
            snackbarHostState = snackbarHostState,
            scope = scope,
            context = context
        )
    }

    // Emit new status on state.isGranted change
    LaunchedEffect(state.isGranted) {
        i { "New location access permission grant status=${state.isGranted}" }
        state.emitNewStatus(state.isGranted)
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
    private val permissionsState: MultiplePermissionsState,
    val backgroundAccessState: BackgroundLocationAccessState?,
    requestLaunchedBefore: DataStoreFlow<Boolean>,
    private val saveRequestLaunched: () -> Unit,
    rationalShown: DataStoreFlow<Boolean>,
    private val saveRationalShown: () -> Unit,
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val context: Context
) {
    val isGranted: Boolean by permissionsState::allPermissionsGranted

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
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                        kind = SnackbarKind.Error,
                        action = SnackbarAction(
                            label = context.getString(R.string.go_to_settings),
                            callback = {
                                goToAppSettings(context)
                            },
                        ),
                    )
                )
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
