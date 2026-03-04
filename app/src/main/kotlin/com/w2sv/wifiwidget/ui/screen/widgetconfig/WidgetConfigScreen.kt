package com.w2sv.wifiwidget.ui.screen.widgetconfig

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
import com.w2sv.wifiwidget.ui.designsystem.BackButtonHeaderWithBottomDivider
import com.w2sv.wifiwidget.ui.designsystem.Easing
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.UpdateWidgetConfig
import com.w2sv.wifiwidget.ui.screen.widgetconfig.list.WidgetConfigList
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.ScreenPreviews
import com.w2sv.wifiwidget.ui.util.SnackbarBuilderFlow
import com.w2sv.wifiwidget.ui.util.resourceIdTestTag
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun WidgetConfigScreen(
    config: WifiWidgetConfig,
    updateConfig: UpdateWidgetConfig,
    configIsDirty: Boolean,
    revertConfig: () -> Unit,
    commitChanges: () -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit,
    onBackButtonClick: () -> Unit,
    snackbarBuilderFlow: SnackbarBuilderFlow
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarBuilderFlow) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = configIsDirty,
                enter = slideInHorizontally(
                    animationSpec = tween(easing = Easing.anticipate),
                    initialOffsetX = { it / 2 }
                ) + fadeIn(),
                exit = slideOutHorizontally(
                    animationSpec = tween(easing = Easing.anticipate),
                    targetOffsetX = { it / 2 }
                ) + fadeOut()
            ) {
                ConfigurationProcedureFABRow(
                    onResetButtonClick = revertConfig,
                    onApplyButtonClick = commitChanges,
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
            BackButtonHeaderWithBottomDivider(
                title = stringResource(id = R.string.widget_configuration),
                onBackButtonClick = onBackButtonClick
            )

            WidgetConfigList(
                config = config,
                updateConfig = updateConfig,
                showDialog = showDialog,
                modifier = Modifier.resourceIdTestTag("widgetConfigurationColumn")
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun Prev() {
    PreviewOf {
        WidgetConfigScreen(
            config = WifiWidgetConfig.default,
            updateConfig = {},
            configIsDirty = true,
            revertConfig = {},
            commitChanges = {},
            showDialog = {},
            onBackButtonClick = {},
            snackbarBuilderFlow = emptyFlow()
        )
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
