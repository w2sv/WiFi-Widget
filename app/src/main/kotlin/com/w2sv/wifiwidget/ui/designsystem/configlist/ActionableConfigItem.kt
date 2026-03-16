package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.extensions.thenIfNotNull
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.BelowEndAnchoring
import com.w2sv.wifiwidget.ui.designsystem.ExplanationText
import com.w2sv.wifiwidget.ui.designsystem.IconDefaults
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.Margins
import com.w2sv.wifiwidget.ui.designsystem.TLayout
import com.w2sv.wifiwidget.ui.util.VerticallyAnimatedVisibility
import com.w2sv.wifiwidget.ui.util.contentDescription
import com.w2sv.wifiwidget.ui.util.orAlphaDecreasedIf
import com.w2sv.wifiwidget.ui.util.shake

@Composable
fun ActionableConfigItem(
    item: ConfigItem.Actionable,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    padStartIfNoToggleButton: Boolean = false
) {
    val expandableListState = item.contentBeneath?.asSubSettingsOrNull?.run {
        rememberExpandableListState(
            isPropertyEnabled = item.isEnabled(),
            allowCollapsing = allowCollapsing
        )
    }
    val hasSubSettings = expandableListState != null

    TLayout(
        central = {
            Text(
                text = stringResource(id = item.property.labelRes),
                fontSize = fontSize,
                color = colorScheme.onBackground.orAlphaDecreasedIf(!item.isEnabled())
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .then(item.modifier)
            .thenIfNotNull(item.shakeController) { shake(it) },
        below = item.contentBeneath?.let { content ->
            {
                ContentBeneath(
                    content = content,
                    expandSubSettings = { expandableListState?.isExpanded == true }
                )
            }
        },
        belowEndAnchoring = if (hasSubSettings) BelowEndAnchoring.ParentEnd else BelowEndAnchoring.CentralEnd,
        belowMargins = if (hasSubSettings) {
            Margins(
                top = ConfigListToken.subSettingsTopMargin,
                start = (-8).dp // related to ConfigListToken.subSettingsStartPadding, but slightly smaller on purpose
            )
        } else {
            Margins.empty
        },
        leading = {
            expandableListState?.run {
                if (allowCollapsing) {
                    ExpandCollapseButton(
                        expand = isExpanded,
                        onClick = ::toggle,
                        isEnabled = item.isEnabled()
                    )
                }
            } ?: run {
                if (padStartIfNoToggleButton) {
                    Spacer(modifier = Modifier.width(ConfigListToken.expandCollapseButtonWidth))
                }
            }
        },
        trailing = { Trailing(item) }
    )
}

@Composable
private fun Trailing(item: ConfigItem.Actionable) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (item) {
            is ConfigItem.Checkable -> CheckableTrailingContent(item)
            is ConfigItem.WithCustomTrailing -> item.trailing(this)
        }
    }
}

@Composable
private fun RowScope.CheckableTrailingContent(item: ConfigItem.Checkable) {
    val label = stringResource(id = item.property.labelRes)

    item.showInfoDialog?.let {
        InfoIconButton(
            onClick = it,
            contentDescription = stringResource(id = R.string.info_icon_cd, label)
        )
    }
    Checkbox(
        checked = item.isChecked(),
        onCheckedChange = { item.onCheckedChange(it) },
        modifier = Modifier.contentDescription(
            stringResource(
                id = if (item.isChecked()) R.string.disable_arg else R.string.enable_arg,
                label
            )
        )
    )
}

@Composable
private fun ContentBeneath(content: ConfigItem.ContentBeneath, expandSubSettings: () -> Boolean) {
    when (content) {
        is ConfigItem.Explanation -> ExplanationText(text = stringResource(content.stringRes))

        is ConfigItem.SubSettings -> VerticallyAnimatedVisibility(visible = expandSubSettings()) {
            SubSettings(elements = content.elements, modifier = Modifier.padding(bottom = ConfigListToken.subSettingsBottomMargin))
        }
    }
}

@Composable
private fun InfoIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        InfoIcon(
            contentDescription = contentDescription,
            modifier = Modifier.size(IconDefaults.SizeBig),
            tint = colorScheme.onSurfaceVariant
        )
    }
}
