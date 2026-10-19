package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ExpandCollapseButton(
    expand: Boolean,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled
    ) {
        Icon(
            imageVector = if (expand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }
}

@Composable
fun SubSettings(elements: ImmutableList<ConfigItem>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .nestedContentBackground()
            .padding(start = ConfigListToken.subSettingsStartPadding)
    ) {
        elements.forEach { element ->
            when (element) {
                is ConfigItem.Actionable -> {
                    if (element.show()) {
                        ActionableConfigItem(
                            item = element,
                            fontSize = ConfigListToken.FontSize.subSetting
                        )
                    }
                }

                is ConfigItem.Custom -> {
                    element.content()
                }
            }
        }
    }
}
