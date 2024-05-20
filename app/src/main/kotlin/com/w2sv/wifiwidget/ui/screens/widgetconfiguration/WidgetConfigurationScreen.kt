package com.w2sv.wifiwidget.ui.screens.widgetconfiguration

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.WidgetConfigurationScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
import com.w2sv.wifiwidget.ui.designsystem.BackButtonHeaderWithDivider
import com.w2sv.wifiwidget.ui.designsystem.Easing
import com.w2sv.wifiwidget.ui.designsystem.HorizontalSlideTransitions
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column.WidgetConfigurationColumn
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column.rememberWidgetConfigurationCardProperties
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.ColorPickerDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.RefreshIntervalConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.WidgetConfigurationScreenDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.shared_viewmodels.WidgetViewModel
import com.w2sv.wifiwidget.ui.states.LocationAccessState
import com.w2sv.wifiwidget.ui.utils.activityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WidgetConfigurationScreenInvocationSource { App, Widget }

@Destination<RootGraph>(style = HorizontalSlideTransitions::class)
@Composable
fun WidgetConfigurationScreen(
    locationAccessState: LocationAccessState,
    navigator: DestinationsNavigator,
    invocationSource: WidgetConfigurationScreenInvocationSource = WidgetConfigurationScreenInvocationSource.Widget,
    widgetVM: WidgetViewModel = activityViewModel(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val onBack: () -> Unit = remember {
        {
            when (invocationSource) {
                WidgetConfigurationScreenInvocationSource.App -> {
                    widgetVM.configuration.reset()
                    navigator.popBackStack()
                }

                WidgetConfigurationScreenInvocationSource.Widget -> {
                    widgetVM.configuration.reset()
                    navigator.navigate(HomeScreenDestination) {
                        launchSingleTop = true
                        popUpTo(WidgetConfigurationScreenDestination) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    BackHandler(onBack = onBack)

    Scaffold(
        snackbarHost = {
            AppSnackbarHost()
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = widgetVM.configuration.statesDissimilar.collectAsState().value,
                enter = remember {
                    slideInHorizontally(
                        animationSpec = tween(easing = Easing.anticipate),
                        initialOffsetX = { it / 2 }
                    ) + fadeIn()
                },
                exit = remember {
                    slideOutHorizontally(
                        animationSpec = tween(easing = Easing.anticipate),
                        targetOffsetX = { it / 2 }
                    ) + fadeOut()
                },
            ) {
                ConfigurationProcedureFABRow(
                    onResetButtonClick = remember { { widgetVM.configuration.reset() } },
                    onApplyButtonClick = remember { { scope.launch { widgetVM.configuration.sync() } } },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding() + 16.dp)
        ) {
            BackButtonHeaderWithDivider(
                title = stringResource(id = R.string.widget_configuration),
                onBackButtonClick = onBack
            )

            var dialogData by rememberSaveable(stateSaver = WidgetConfigurationScreenDialog.nullableStateSaver) {
                mutableStateOf(null)
            }

            dialogData?.let {
                WidgetConfigurationScreenDialog(
                    dialog = it,
                    widgetConfiguration = widgetVM.configuration,
                    onDismissRequest = remember { { dialogData = null } }
                )
            }

            WidgetConfigurationColumn(
                cardProperties = rememberWidgetConfigurationCardProperties(
                    widgetConfiguration = widgetVM.configuration,
                    locationAccessState = locationAccessState,
                    showInfoDialog = remember {
                        {
                            dialogData = WidgetConfigurationScreenDialog.Info(it)
                        }
                    },
                    showCustomColorConfigurationDialog = remember {
                        {
                            dialogData = WidgetConfigurationScreenDialog.ColorPicker(it)
                        }
                    },
                    showRefreshIntervalConfigurationDialog = remember {
                        {
                            dialogData =
                                WidgetConfigurationScreenDialog.RefreshIntervalConfiguration
                        }
                    }
                )
            )
        }
    }
}

@Composable
private fun ConfigurationProcedureFABRow(
    onResetButtonClick: () -> Unit,
    onApplyButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        ConfigurationProcedureFAB(
            text = stringResource(R.string.reset),
            onClick = onResetButtonClick,
            icon = {
                Icon(
                    painter = painterResource(id = com.w2sv.core.common.R.drawable.ic_refresh_24),
                    contentDescription = null
                )
            }
        )
        ConfigurationProcedureFAB(
            text = stringResource(id = R.string.apply),
            onClick = onApplyButtonClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
private fun ConfigurationProcedureFAB(
    text: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(onClick = onClick, modifier = modifier.padding(bottom = 12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Text(text = text)
        }
    }
}

@Composable
private fun WidgetConfigurationScreenDialog(
    dialog: WidgetConfigurationScreenDialog,
    widgetConfiguration: ReversibleWidgetConfiguration,
    onDismissRequest: () -> Unit
) {
    when (dialog) {
        is WidgetConfigurationScreenDialog.Info -> {
            PropertyInfoDialog(
                data = dialog.data,
                onDismissRequest = onDismissRequest
            )
        }

        is WidgetConfigurationScreenDialog.ColorPicker -> {
            ColorPickerDialog(
                properties = dialog.data,
                applyColor = remember {
                    {
                        widgetConfiguration.coloringConfig.update {
                            it.copy(
                                custom = dialog.data.createCustomColoringData(
                                    it.custom
                                )
                            )
                        }
                    }
                },
                onDismissRequest = onDismissRequest,
            )
        }

        is WidgetConfigurationScreenDialog.RefreshIntervalConfiguration -> {
            RefreshIntervalConfigurationDialog(
                interval = widgetConfiguration.refreshInterval.collectAsState().value,
                setInterval = remember {
                    { widgetConfiguration.refreshInterval.value = it }
                },
                onDismissRequest = onDismissRequest
            )
        }
    }
}