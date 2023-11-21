package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.WifiStatus
import com.w2sv.widget.utils.goToWifiSettingsIntent
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppFontText
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Preview
@Composable
private fun WifiStatusDisplayPrev() {
    AppTheme {
        WifiStatusDisplay(wifiStatus = WifiStatus.Connected)
    }
}

@Composable
fun WifiStatusDisplay(
    wifiStatus: WifiStatus,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable {
                context.startActivity(
                    goToWifiSettingsIntent,
                )
            }
            .semantics {
                contentDescription =
                    context.getString(R.string.go_to_wifi_settings_cd)
            },
    ) {
        Icon(
            painter = painterResource(id = wifiStatus.iconRes),
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        AppFontText(text = stringResource(id = wifiStatus.labelRes))
    }
}
