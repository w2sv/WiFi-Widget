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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.composed.nullableListSaver
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
import com.w2sv.wifiwidget.ui.designsystem.BackButtonHeaderWithDivider
import com.w2sv.wifiwidget.ui.designsystem.HorizontalSlideTransitions
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column.WidgetPropertyConfigurationColumn
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.ColorPickerDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.ColorPickerProperties
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.RefreshIntervalConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.CustomWidgetColor
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.shared_viewmodels.WidgetViewModel
import com.w2sv.wifiwidget.ui.states.LocationAccessState
import com.w2sv.wifiwidget.ui.utils.Easing
import com.w2sv.wifiwidget.ui.utils.activityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
private sealed interface WidgetConfigurationScreenDialog {
    @Immutable
    data class Info(val data: InfoDialogData) : WidgetConfigurationScreenDialog {
        companion object {
            const val SAVER_LABEL = "Info"
        }
    }

    @Immutable
    data class ColorPicker(val properties: ColorPickerProperties) : WidgetConfigurationScreenDialog {
        companion object {
            const val SAVER_LABEL = "ColorPicker"
        }
    }

    @Immutable
    data object RefreshIntervalConfiguration : WidgetConfigurationScreenDialog {
        const val SAVER_LABEL = "RefreshIntervalConfiguration"
    }

    companion object {
        val nullableStateSaver: Saver<WidgetConfigurationScreenDialog?, Any> = nullableListSaver(
            saveNonNull = {
                when (it) {
                    is Info -> {
                        listOf(
                            Info.SAVER_LABEL,
                            it.data.title,
                            it.data.description,
                            it.data.learnMoreUrl
                        )
                    }

                    is ColorPicker -> {
                        listOf(
                            ColorPicker.SAVER_LABEL,
                            it.properties.customWidgetColor,
                            it.properties.appliedColor.toArgb(),
                            it.properties.color.toArgb()
                        )
                    }

                    is RefreshIntervalConfiguration -> listOf(RefreshIntervalConfiguration.SAVER_LABEL)
                }
            },
            restoreNonNull = {
                when (it.first()) {
                    Info.SAVER_LABEL -> Info(
                        InfoDialogData(
                            title = it[1] as String,
                            description = it[2] as String,
                            learnMoreUrl = it[3] as String?
                        )
                    )

                    ColorPicker.SAVER_LABEL -> ColorPicker(
                        ColorPickerProperties(
                            customWidgetColor = it[1] as CustomWidgetColor,
                            appliedColor = Color(it[2] as Int),
                            initialColor = Color(it[3] as Int)
                        )
                    )

                    RefreshIntervalConfiguration.SAVER_LABEL -> RefreshIntervalConfiguration

                    else -> throw IllegalArgumentException("Invalid WidgetConfigurationScreenDialog type label")
                }
            }
        )
    }
}

@Destination<RootGraph>(style = HorizontalSlideTransitions::class)
@Composable
fun WidgetConfigurationScreen(
    locationAccessState: LocationAccessState,
    navigator: DestinationsNavigator,
    widgetVM: WidgetViewModel = activityViewModel(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val onBack: () -> Unit = remember {
        {
            widgetVM.configuration.reset()
            navigator.popBackStack()
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

            WidgetPropertyConfigurationColumn(
                widgetConfiguration = widgetVM.configuration,
                locationAccessState = locationAccessState,
                showPropertyInfoDialog = remember {
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
                        dialogData = WidgetConfigurationScreenDialog.RefreshIntervalConfiguration
                    }
                }
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
                properties = dialog.properties,
                applyColor = remember {
                    {
                        widgetConfiguration.coloringConfig.update {
                            it.copy(
                                custom = dialog.properties.createCustomColoringData(
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