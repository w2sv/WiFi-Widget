package com.w2sv.wifiwidget.ui.screens.home.components.widget

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.services.isLocationEnabled
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.HomeScreenCard
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.BackgroundLocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.WidgetConfigurationDialog
import com.w2sv.wifiwidget.ui.utils.FlowCollectionEffect
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.flow.Flow

@Composable
fun WidgetCard(
    locationAccessState: LocationAccessState,
    modifier: Modifier = Modifier,
    widgetVM: WidgetViewModel = viewModel(),
) {
    HomeScreenCard(
        modifier = modifier,
        content = {
            IconHeader(
                iconRes = R.drawable.ic_widgets_24,
                headerRes = R.string.widget,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                PinWidgetButton(
                    onClick = {
                        widgetVM.attemptWidgetPin()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(60.dp),
                )

                Spacer(modifier = Modifier.width(32.dp))

                WidgetConfigurationDialogButton(
                    onClick = {
                        widgetVM.setShowConfigurationDialog(true)
                    },
                    modifier = Modifier.size(32.dp),
                )
            }
        },
    )

    NewWidgetPinnedSnackbar(
        newWidgetPinned = widgetVM.newWidgetPinned,
        anyLocationAccessRequiringPropertyEnabled = { widgetVM.configuration.anyLocationAccessRequiringPropertyEnabled },
        backgroundAccessState = locationAccessState.backgroundAccessState
    )

    // Call configuration.onLocationAccessPermissionStatusChanged on new location access permission status
    FlowCollectionEffect(locationAccessState.newStatus) {
        widgetVM.configuration.onLocationAccessPermissionStatusChanged(it)
    }

    if (widgetVM.showConfigurationDialog.collectAsStateWithLifecycle().value) {
        WidgetConfigurationDialog(
            locationAccessState = locationAccessState,
            closeDialog = {
                widgetVM.setShowConfigurationDialog(false)
            },
        )
    }
}

/**
 * Shows Snackbar on collection from [newWidgetPinned].
 */
@Composable
private fun NewWidgetPinnedSnackbar(
    newWidgetPinned: Flow<Unit>,
    anyLocationAccessRequiringPropertyEnabled: () -> Boolean,
    backgroundAccessState: BackgroundLocationAccessState?,
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    FlowCollectionEffect(newWidgetPinned) {
        if (anyLocationAccessRequiringPropertyEnabled()) {
            when {
                // Warn about (B)SSID not being displayed if device GPS is disabled
                !context.isLocationEnabled -> snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
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
                                backgroundAccessState.launchRequest()
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
