package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.w2sv.composed.ui.thenIf
import com.w2sv.core.resources.R
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.wifiwidget.ui.designsystem.NestedSettingsTokens
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItem
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItemLayout
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListTokens
import com.w2sv.wifiwidget.ui.designsystem.configlist.SubSettings
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun IpSubSettings(elements: ImmutableList<ConfigItem>, modifier: Modifier = Modifier) {
    SubSettings(
        elements = elements,
        modifier = modifier,
        header = {
            if (elements.any { (it.property as IpSetting).isVersionSetting }) {
                Text(
                    text = stringResource(R.string.displayed_versions),
                    fontSize = NestedSettingsTokens.fontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = ConfigListTokens.itemSpacing)
                )
            }
        },
        itemContent = { item ->
            ConfigItemLayout(
                item = item,
                modifier = Modifier.thenIf((item.property as IpSetting).isVersionSetting) {
                    padding(start = NestedSettingsTokens.secondLevelStartPadding)
                },
                fontSize = NestedSettingsTokens.fontSize,
                shakeOnRejectedChange = true
            )
        }
    )
}
