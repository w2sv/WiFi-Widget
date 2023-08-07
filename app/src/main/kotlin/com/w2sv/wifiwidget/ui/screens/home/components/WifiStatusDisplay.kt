package com.w2sv.wifiwidget.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.data.model.WifiStatus
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Preview
@Composable
private fun WifiStatusDisplayPrev() {
    AppTheme {
        WifiStatusDisplay(wifiStatus = WifiStatus.Connected)
    }
}

@Composable
fun WifiStatusDisplay(wifiStatus: WifiStatus, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Spacer(modifier = Modifier.height(12.dp))
        Icon(
            painter = painterResource(id = wifiStatus.iconRes),
            contentDescription = null,
            modifier = Modifier.size(42.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        JostText(text = stringResource(id = wifiStatus.labelRes))
    }
}