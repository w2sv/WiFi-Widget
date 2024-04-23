package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import kotlin.time.Duration.Companion.minutes

@Composable
fun RefreshIntervalConfigurationDialog(
    intervalMinutes: Int,
    setInterval: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var duration by remember(intervalMinutes) {
        mutableStateOf(intervalMinutes.minutes)
    }

    ConfigurationDialog(
        onDismissRequest = onDismissRequest,
        onApplyButtonPress = { setInterval(duration.inWholeMinutes.toInt()) },
        applyButtonEnabled = duration.inWholeMinutes.toInt() != intervalMinutes,
        title = stringResource(R.string.refresh_interval),
        modifier = modifier
    ) {

    }
}