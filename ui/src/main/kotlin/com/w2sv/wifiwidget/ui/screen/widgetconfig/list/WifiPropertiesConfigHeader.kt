package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.wifiwidget.ui.designsystem.IconHeadline
import com.w2sv.wifiwidget.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.wifiwidget.ui.designsystem.SupportingTextWithInfoIcon
import com.w2sv.wifiwidget.ui.util.PreviewOf

@Preview(showBackground = true)
@Composable
private fun Prev() {
    PreviewOf {
        WifiPropertiesConfigHeader(
            WidgetConfig.default,
            {}
        )
    }
}

@Composable
fun WifiPropertiesConfigHeader(
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.testTag("wifi_properties_section")) {
        IconHeadline(iconRes = R.drawable.ic_wifi_24, stringRes = R.string.wifi_properties, modifier = Modifier.fillMaxWidth()) {
            MoreIconButtonWithDropdownMenu {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.move_disabled_properties_to_bottom)) },
                    onClick = {
                        updateConfig { withDisabledPropertiesAtEnd() }
                        collapseMenu()
                    },
                    enabled = !config.areDisabledPropertiesAtEnd,
                    leadingIcon = { Icon(painterResource(R.drawable.ic_move_down_24), null) }
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.restore_default_order)) },
                    onClick = {
                        updateConfig { withDefaultPropertyOrder() }
                        collapseMenu()
                    },
                    enabled = !config.arePropertiesInDefaultOrder,
                    leadingIcon = { Icon(painterResource(R.drawable.ic_restart_alt_24), null) }
                )
            }
        }
        SupportingTextWithInfoIcon(
            text = stringResource(R.string.wifi_property_reordering_information),
            iconTextSpacing = 14.dp,
            modifier = Modifier.padding(bottom = WidgetConfigListDimens.wifiHeaderToItemsSpacing)
        )
    }
}
