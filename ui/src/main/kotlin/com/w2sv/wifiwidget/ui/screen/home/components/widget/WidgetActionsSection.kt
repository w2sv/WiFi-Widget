package com.w2sv.wifiwidget.ui.screen.home.components.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.screen.home.HomeScreenTokens

@Composable
fun WidgetActionsSection(
    pinWidget: () -> Unit,
    toWidgetConfiguration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PinWidgetButton(
            onClick = pinWidget,
            modifier = Modifier.fillMaxWidth().heightIn(min = HomeScreenTokens.buttonHeight)
        )
        WidgetConfigurationButton(
            onClick = toWidgetConfiguration,
            modifier = Modifier.fillMaxWidth().heightIn(min = HomeScreenTokens.buttonHeight)
        )
    }
}

@Composable
private fun PinWidgetButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(painter = painterResource(R.drawable.ic_push_pin_24), contentDescription = null)
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = stringResource(R.string.pin))
    }
}

@Composable
private fun WidgetConfigurationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val secondary = MaterialTheme.colorScheme.secondary

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = secondary),
        border = BorderStroke(ButtonDefaults.outlinedButtonBorder().width, secondary)
    ) {
        Icon(imageVector = Icons.Rounded.Settings, contentDescription = null)
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(R.string.configure))
    }
}
