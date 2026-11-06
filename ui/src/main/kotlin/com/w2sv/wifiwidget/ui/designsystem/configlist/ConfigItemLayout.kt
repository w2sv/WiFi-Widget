package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import com.w2sv.composed.animation.ShakeController
import com.w2sv.composed.animation.shakenBy
import com.w2sv.composed.ui.thenIfNotNull
import com.w2sv.core.resources.R
import com.w2sv.kotlinutils.makeIf
import com.w2sv.wifiwidget.ui.designsystem.BelowEndAnchoring
import com.w2sv.wifiwidget.ui.designsystem.IconDefaults
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.Margins
import com.w2sv.wifiwidget.ui.designsystem.NestedSettingsTokens
import com.w2sv.wifiwidget.ui.designsystem.SupportingText
import com.w2sv.wifiwidget.ui.designsystem.TLayout
import com.w2sv.wifiwidget.ui.util.VerticalAnimatedVisibility
import com.w2sv.wifiwidget.ui.util.orAlphaDecreasedIf
import kotlinx.coroutines.launch

@Composable
fun ConfigItemLayout(
    item: ConfigItem,
    modifier: Modifier = Modifier,
    expandButtonModifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    padStartIfNoExpandButton: Boolean = false,
    allowCollapsing: Boolean = true,
    shakeOnRejectedChange: Boolean = false,
    expandedContent: (@Composable () -> Unit)? = null
) {
    val shakeController = makeIf(shakeOnRejectedChange) {
        remember { ShakeController(decay = 0f, frequencyHz = 4f) }
    }
    val scope = rememberCoroutineScope()
    val hasExpandedContent = expandedContent != null
    val expandableListState = makeIf(hasExpandedContent) {
        rememberExpandableListState(
            isPropertyEnabled = item.isChecked(),
            allowCollapsing = allowCollapsing
        )
    }

    TLayout(
        central = {
            Text(
                text = stringResource(id = item.property.labelRes),
                fontSize = fontSize,
                color = colorScheme.onBackground.orAlphaDecreasedIf(!item.isChecked())
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .thenIfNotNull(shakeController) { shakenBy(it) },
        below = when {
            hasExpandedContent -> {
                {
                    VerticalAnimatedVisibility(visible = expandableListState?.isExpanded == true) {
                        expandedContent.invoke()
                    }
                }
            }

            item.supportingTextRes != null -> {
                { SupportingText(text = stringResource(item.supportingTextRes)) }
            }

            else -> null
        },
        belowEndAnchoring = if (hasExpandedContent) BelowEndAnchoring.ParentEnd else BelowEndAnchoring.CentralEnd,
        belowMargins = if (hasExpandedContent) {
            Margins(
                top = NestedSettingsTokens.topMargin,
                start = NestedSettingsTokens.containerStartOffset
            )
        } else {
            Margins.empty
        },
        leading = {
            expandableListState?.run {
                if (allowCollapsing) {
                    ExpandCollapseButton(
                        expand = isExpanded,
                        propertyLabel = stringResource(item.property.labelRes),
                        onClick = ::toggle,
                        isEnabled = item.isChecked(),
                        modifier = expandButtonModifier
                    )
                }
            } ?: run {
                if (padStartIfNoExpandButton) {
                    Spacer(modifier = Modifier.width(ConfigListTokens.expandCollapseButtonWidth))
                }
            }
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckableTrailingContent(item) {
                    shakeController?.let { controller -> scope.launch { controller.shake() } }
                }
            }
        }
    )
}

@Composable
private fun RowScope.CheckableTrailingContent(item: ConfigItem, shake: () -> Unit) {
    val label = stringResource(id = item.property.labelRes)

    item.showInfoDialog?.let {
        InfoIconButton(
            onClick = it,
            contentDescription = stringResource(id = R.string.info_icon_cd, label)
        )
    }
    Checkbox(
        checked = item.isChecked(),
        onCheckedChange = { if (!item.trySetChecked(it)) shake() },
        modifier = Modifier.semantics { contentDescription = label }
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
