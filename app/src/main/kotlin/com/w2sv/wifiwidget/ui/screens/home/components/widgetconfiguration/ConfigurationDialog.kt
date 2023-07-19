package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.diagonalGradient
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ConfigColumn
import com.w2sv.wifiwidget.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Preview
@Composable
private fun WidgetConfigurationDialogPrev() {
    AppTheme {
        WidgetConfigurationDialog(closeDialog = {})
    }
}

@Composable
fun WidgetConfigurationDialog(
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    widgetConfigurationVM: WidgetConfigurationViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val onDismissRequest: () -> Unit = {
        widgetConfigurationVM.onDismissWidgetConfigurationDialog()
        closeDialog()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    // gradient background
                    .background(
                        diagonalGradient(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                    .padding(vertical = 16.dp)
            ) {
                Icon(
                    painterResource(id = com.w2sv.widget.R.drawable.ic_settings_24),
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                JostText(
                    text = stringResource(id = com.w2sv.common.R.string.configure_widget),
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Medium
                )
                ConfigColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(260.dp, 420.dp)
                )
                ButtonRow(
                    onCancel = {
                        onDismissRequest()
                    },
                    onApply = {
                        scope.launch {
                            widgetConfigurationVM.nonAppliedWidgetConfiguration.sync()
                            WidgetProvider.triggerDataRefresh(context)
                            context.showToast(R.string.updated_widget_configuration)
                            closeDialog()
                        }
                    },
                    applyButtonEnabled = widgetConfigurationVM.nonAppliedWidgetConfiguration.statesDissimilar.collectAsState().value,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}