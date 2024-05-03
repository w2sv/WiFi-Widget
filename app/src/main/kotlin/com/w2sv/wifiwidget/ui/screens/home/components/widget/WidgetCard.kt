package com.w2sv.wifiwidget.ui.screens.home.components.widget

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.generated.destinations.WidgetConfigurationScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.w2sv.androidutils.services.isLocationEnabledCompat
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.LocalNavHostController
import com.w2sv.wifiwidget.ui.activityViewModel
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.designsystem.LocalLocationManager
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarAction
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.HomeScreenCard
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.BackgroundLocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.utils.CollectLatestFromFlow
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.flow.Flow

@Composable
fun WidgetCard(
    locationAccessState: LocationAccessState,
    modifier: Modifier = Modifier,
    widgetVM: WidgetViewModel = activityViewModel(),
    navController: NavHostController = LocalNavHostController.current
) {
    HomeScreenCard(
        iconHeaderProperties = IconHeaderProperties(
            iconRes = R.drawable.ic_widgets_24,
            stringRes = R.string.widget,
        ),
        headerRowBottomSpacing = 32.dp,
        modifier = modifier,
        content = {
            val context = LocalContext.current
            Row(verticalAlignment = Alignment.CenterVertically) {
                PinWidgetButton(
                    onClick = remember {
                        {
                            widgetVM.attemptWidgetPin(context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(60.dp),
                )

                Spacer(modifier = Modifier.width(32.dp))

                WidgetConfigurationDialogButton(
                    onClick = remember {
                        {
                            navController.navigate(WidgetConfigurationScreenDestination())
                        }
                    },
                    modifier = Modifier.size(32.dp),
                )
            }
        },
    )

    ShowSnackbarOnWidgetPin(
        newWidgetPinned = widgetVM.widgetPinSuccessFlow,
        anyLocationAccessRequiringPropertyEnabled = { widgetVM.configuration.anyLocationAccessRequiringPropertyEnabled },
        backgroundAccessState = locationAccessState.backgroundAccessState,
    )
}

/**
 * Shows Snackbar on collection from [newWidgetPinned].
 */
@Composable
private fun ShowSnackbarOnWidgetPin(
    newWidgetPinned: Flow<Unit>,
    anyLocationAccessRequiringPropertyEnabled: () -> Boolean,
    backgroundAccessState: BackgroundLocationAccessState?
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val locationManager = LocalLocationManager.current

    CollectLatestFromFlow(newWidgetPinned) {
        if (anyLocationAccessRequiringPropertyEnabled()) {
            when {
                // Warn about (B)SSID not being displayed if device GPS is disabled
                !locationManager.isLocationEnabledCompat() -> snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.on_pin_widget_wo_gps_enabled),
                        kind = SnackbarKind.Error,
                    )
                )

                // Warn about (B)SSID not being reliably displayed if background location access not granted
                backgroundAccessState?.isGranted == false -> snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.on_pin_widget_wo_background_location_access_permission),
                        kind = SnackbarKind.Error,
                        action = SnackbarAction(
                            label = context.getString(R.string.grant),
                            callback = {
                                backgroundAccessState.launchPermissionRequest()
                            }
                        )
                    )
                )
            }
        }
        snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
            AppSnackbarVisuals(
                msg = context.getString(R.string.pinned_widget),
                kind = SnackbarKind.Success,
            )
        )
    }
}

@Composable
private fun PinWidgetButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.pin),
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun WidgetConfigurationDialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.inflate_the_widget_configuration_dialog),
            modifier = modifier,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
