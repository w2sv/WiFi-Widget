package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.w2sv.wifiwidget.ui.designsystem.NestedSettingsTokens
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ExpandCollapseButton(
    expand: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (expand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }
}

@Composable
fun SubSettings(
    elements: ImmutableList<ConfigItem>,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    itemContent: @Composable (ConfigItem) -> Unit = { item ->
        ConfigItemLayout(
            item = item,
            fontSize = NestedSettingsTokens.fontSize,
            shakeOnRejectedChange = true
        )
    }
) {
    Column(
        modifier = modifier
            .nestedContentBackground()
            .padding(start = NestedSettingsTokens.startPadding)
    ) {
        header?.invoke()
        elements.forEach { element ->
            if (element.show()) {
                key(element.property.labelRes) {
                    itemContent(element)
                }
            }
        }
    }
}
