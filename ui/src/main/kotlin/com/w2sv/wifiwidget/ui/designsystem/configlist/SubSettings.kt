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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.designsystem.NestedSettingsTokens
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ExpandCollapseButton(
    expand: Boolean,
    propertyLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val actionDescription = stringResource(
        if (expand) R.string.collapse_settings_cd else R.string.expand_settings_cd,
        propertyLabel
    )
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.semantics { contentDescription = actionDescription }
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
