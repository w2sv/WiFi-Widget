package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.composed.core.extensions.thenIfNotNull
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.BelowEndAnchoring
import com.w2sv.wifiwidget.ui.designsystem.IconDefaults
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.Margins
import com.w2sv.wifiwidget.ui.theme.explanation
import com.w2sv.wifiwidget.ui.util.VerticallyAnimatedVisibility
import com.w2sv.wifiwidget.ui.util.contentDescription
import com.w2sv.wifiwidget.ui.util.orAlphaDecreasedIf
import com.w2sv.wifiwidget.ui.util.shake

@Composable
fun CheckableItem(
    checkable: ConfigItem.Checkable,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    padStartIfNoToggleButton: Boolean = false
) {
    val label = stringResource(id = checkable.property.labelRes)
    val expandableListState = checkable.contentBeneath?.asSubSettingsOrNull?.run {
        rememberExpandableListState(
            isPropertyEnabled = checkable.isChecked(),
            allowCollapsing = allowCollapsing
        )
    }
    val showToggleButton = expandableListState?.allowCollapsing == true

    ConfigLayout(
        labelRes = checkable.property.labelRes,
        modifier = modifier
            .then(checkable.modifier)
            .thenIf(padStartIfNoToggleButton && !showToggleButton) { padding(start = ConfigListToken.expandCollapseButtonWidth) }
            .thenIfNotNull(checkable.shakeController) { shake(it) },
        fontSize = fontSize,
        labelColor = colorScheme.onBackground.orAlphaDecreasedIf(!checkable.isChecked()),
        below = checkable.contentBeneath?.let { content ->
            {
                ExplanationOrSubSettings(
                    content = content,
                    expandSubSettings = { expandableListState?.isExpanded == true }
                )
            }
        },
        belowEndAnchoring = checkable.contentBeneath?.asSubSettingsOrNull?.let { BelowEndAnchoring.ParentEnd }
            ?: BelowEndAnchoring.LabelEnd,
        belowMargins = checkable.contentBeneath?.asSubSettingsOrNull?.let {
            Margins(
                top = ConfigListToken.subSettingsTopMargin,
                start = (-8).dp // related to ConfigListToken.subSettingsStartPadding, but slightly smaller on purpose
            )
        } ?: Margins.empty,
        leading = {
            expandableListState?.run {
                if (allowCollapsing) {
                    ExpandCollapseButton(
                        expand = isExpanded,
                        onClick = ::toggle,
                        isEnabled = checkable.isChecked()
                    )
                }
            }
        },
        trailing = {
            checkable.showInfoDialog?.let {
                InfoIconButton(
                    onClick = it,
                    contentDescription = stringResource(id = R.string.info_icon_cd, label)
                )
            }
            Checkbox(
                checked = checkable.isChecked(),
                onCheckedChange = { checkable.onCheckedChange(it) },
                modifier = Modifier.contentDescription(
                    stringResource(
                        id = if (checkable.isChecked()) R.string.disable_arg else R.string.enable_arg,
                        label
                    )
                )
            )
        }
    )
}

@Composable
private fun ExplanationOrSubSettings(content: ConfigItem.Beneath, expandSubSettings: () -> Boolean) {
    when (content) {
        is ConfigItem.Beneath.Explanation -> ExplanationText(
            stringRes = content.stringRes
        )

        is ConfigItem.Beneath.SubSettings -> VerticallyAnimatedVisibility(visible = expandSubSettings()) {
            SubSettings(elements = content.elements, modifier = Modifier.padding(bottom = ConfigListToken.subSettingsBottomMargin))
        }
    }
}

@Composable
private fun ExplanationText(@StringRes stringRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(stringRes),
        style = MaterialTheme.typography.explanation,
        modifier = modifier
    )
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
