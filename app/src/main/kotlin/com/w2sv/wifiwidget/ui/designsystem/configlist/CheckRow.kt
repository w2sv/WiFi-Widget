package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Dimension
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.composed.core.extensions.thenIfNotNull
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.IconDefaults
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.VerticallyAnimatedVisibility
import com.w2sv.wifiwidget.ui.util.contentDescription
import com.w2sv.wifiwidget.ui.util.orAlphaDecreasedIf
import com.w2sv.wifiwidget.ui.util.shake

@Composable
fun CheckRow(
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

    ConfigRow(
        labelRes = checkable.property.labelRes,
        modifier = modifier
            .then(checkable.modifier)
            .thenIf(padStartIfNoToggleButton && !showToggleButton) { padding(start = 48.dp) }
            .thenIfNotNull(checkable.shakeController) { shake(it) },
        fontSize = fontSize,
        labelColor = colorScheme.onBackground.orAlphaDecreasedIf(!checkable.isChecked()),
        beneath = checkable.contentBeneath?.let { content ->
            {
                ExplanationOrSubSettings(
                    content = content,
                    expandSubSettings = { expandableListState?.isExpanded == true }
                )
            }
        },
        leading = {
            expandableListState?.run {
                if (allowCollapsing) {
                    SubSettingsToggleButton(
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
fun ConfigRowScope.ExplanationOrSubSettings(content: ConfigItem.Beneath, expandSubSettings: () -> Boolean) {
    with(constraintLayoutScope) {
        when (content) {
            is ConfigItem.Beneath.Explanation -> ExplanationText(
                stringRes = content.stringRes,
                modifier = Modifier.constrainAs(beneathRef) {
                    top.linkTo(labelRef.bottom)
                    centerHorizontallyTo(labelRef)
                    width = Dimension.fillToConstraints
                }
            )

            is ConfigItem.Beneath.SubSettings -> Box(
                modifier = Modifier.constrainAs(beneathRef) {
                    top.linkTo(labelRef.bottom, margin = 8.dp)
                    linkTo(labelRef.start, parent.end, startMargin = (-8).dp)
                    width = Dimension.fillToConstraints
                }
            ) {
                VerticallyAnimatedVisibility(visible = expandSubSettings()) {
                    SubSettings(elements = content.elements, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ExplanationText(@StringRes stringRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(stringRes),
        color = colorScheme.onSurfaceVariantLowAlpha,
        fontSize = 13.sp,
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
