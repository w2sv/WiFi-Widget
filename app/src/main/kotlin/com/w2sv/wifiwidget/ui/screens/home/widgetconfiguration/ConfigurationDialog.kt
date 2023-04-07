package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.ui.screens.home.HomeActivity
import com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn.ConfigColumn
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme
import com.w2sv.wifiwidget.ui.shared.diagonalGradient

@Preview
@Composable
private fun WidgetConfigurationDialogPrev() {
    WifiWidgetTheme {
        WidgetConfigurationDialog()
    }
}

@Composable
fun WidgetConfigurationDialog(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = viewModel()
) {
    Dialog(onDismissRequest = { viewModel.onDismissWidgetConfigurationDialog() }) {
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
                    contentDescription = "@null",
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
                ButtonRow(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}