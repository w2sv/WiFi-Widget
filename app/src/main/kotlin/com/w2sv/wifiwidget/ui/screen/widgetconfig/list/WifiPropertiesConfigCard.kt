package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.wifiwidget.ui.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.designsystem.Disclaimer
import com.w2sv.wifiwidget.ui.designsystem.DropdownMenuItemProperties
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListToken
import com.w2sv.wifiwidget.ui.designsystem.configlist.ReorderableCheckableList
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import com.w2sv.wifiwidget.ui.screen.widgetconfig.model.wifiPropertyConfigItems
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.snackbar.rememberSnackbarController
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WifiPropertiesConfigCard(
    config: WidgetConfig,
    updateConfig: UpdateWidgetConfig,
    showDialog: (WidgetConfigDialog) -> Unit
) {
    WidgetConfigSectionCard(
        IconHeader(
            iconRes = R.drawable.ic_wifi_24,
            stringRes = R.string.wifi_properties,
            trailingIcon = {
                MoreIconButtonWithDropdownMenu(
                    menuItems = remember {
                        persistentListOf(
                            DropdownMenuItemProperties(
                                R.string.restore_default_order,
                                onClick = { updateConfig { withDefaultPropertyOrder() } },
                                enabled = { !config.arePropertiesInDefaultOrder },
                                leadingIconRes = R.drawable.ic_restart_alt_24
                            )
                        )
                    }
                )
            }
        )
    ) {
        Disclaimer(
            text = stringResource(R.string.wifi_property_reordering_information),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 14.dp)
                .padding(bottom = 8.dp)
        )
        ReorderableCheckableList(
            elements = wifiPropertyConfigItems(
                config = config,
                updateConfig = updateConfig,
                showDialog = showDialog,
                locationAccess = LocalLocationAccessCapability.current,
                scope = rememberCoroutineScope(),
                snackbarController = rememberSnackbarController()
            ),
            onDrop = { fromIndex: Int, toIndex: Int ->
                updateConfig { withUpdatedPropertyPosition(fromIndex, toIndex) }
            }
        )
    }
}

@Composable
fun IpVersionsHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.displayed_versions),
        fontSize = ConfigListToken.FontSize.subSetting,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        WifiPropertiesConfigCard(
            WidgetConfig.default,
            {},
            {}
        )
    }
}
