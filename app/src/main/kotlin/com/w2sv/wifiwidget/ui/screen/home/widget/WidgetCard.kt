package com.w2sv.wifiwidget.ui.screen.home.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.navigation.LocalNavigator
import com.w2sv.wifiwidget.ui.navigation.Navigator

@Composable
fun WidgetCard(
    pinWidget: () -> Unit,
    modifier: Modifier = Modifier,
    navigator: Navigator = LocalNavigator.current
) {
    ElevatedIconHeaderCard(
        iconHeader = IconHeader(
            iconRes = R.drawable.ic_widgets_24,
            stringRes = R.string.widget
        ),
        modifier = modifier,
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                val buttonHeight = 60.dp
                PinWidgetButton(
                    onClick = pinWidget,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(buttonHeight)
                )
                WidgetConfigurationButton(
                    onClick = { navigator.toWidgetConfiguration() },
                    modifier = Modifier
                        .height(buttonHeight)
                        .weight(0.4f)
                )
            }
        }
    )
}

@Composable
private fun PinWidgetButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.pin),
            fontSize = 16.sp
        )
    }
}

@Composable
private fun WidgetConfigurationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null
            )
            Text(stringResource(R.string.configure))
        }
    }
}
