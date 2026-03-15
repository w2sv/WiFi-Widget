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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import kotlinx.collections.immutable.ImmutableList

object SubSettingsDefaults {
    val fontSize = 14.sp
    val startPadding = 10.dp
}

@Composable
fun SubSettingsToggleButton(
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
            .padding(start = SubSettingsDefaults.startPadding)
    ) {
        elements.forEach { element ->
            when (element) {
                is ConfigItem.Checkable -> {
                    if (element.show()) {
                        CheckRow(
                            checkable = element,
                            fontSize = SubSettingsDefaults.fontSize
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
