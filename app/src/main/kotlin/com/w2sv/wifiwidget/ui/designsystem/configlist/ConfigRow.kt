package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.util.PreviewOf

@Stable
data class ConfigRowScope(
    val constraintLayoutScope: ConstraintLayoutScope,
    val beneathRef: ConstrainedLayoutReference,
    val labelRef: ConstrainedLayoutReference
)

@Composable
fun ConfigRow(
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    leading: @Composable BoxScope.() -> Unit = {},
    beneath: (@Composable ConfigRowScope.() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (leadingRef, labelRef, trailingRef, beneathRef) = createRefs()

        Box(
            modifier = Modifier.constrainAs(leadingRef) {
                start.linkTo(parent.start)
                centerVerticallyTo(labelRef)
            },
            content = leading
        )

        Text(
            text = stringResource(id = labelRes),
            fontSize = fontSize,
            color = labelColor,
            modifier = Modifier.constrainAs(labelRef) {
                linkTo(leadingRef.end, trailingRef.start)
                linkTo(parent.top, if (beneath == null) parent.bottom else beneathRef.top)
                width = Dimension.fillToConstraints
            }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.constrainAs(trailingRef) {
                linkTo(labelRef.end, parent.end)
                centerVerticallyTo(labelRef)
            },
            content = trailing
        )

        beneath?.let {
            val scope = remember(this) {
                ConfigRowScope(
                    constraintLayoutScope = this,
                    beneathRef = beneathRef,
                    labelRef = labelRef
                )
            }
            it.invoke(scope)
        }
    }
}

@Preview
@Composable
private fun WithExpl() {
    PreviewOf {
        Surface {
            ConfigRow(
                labelRes = R.string.interval,
                leading = { SubSettingsToggleButton(expand = false, onClick = {}) },
                trailing = { Checkbox(checked = true, onCheckedChange = {}) },
                beneath = {
                    ExplanationOrSubSettings(ConfigItem.Beneath.Explanation(R.string.interval), { true })
                }
            )
        }
    }
}

@Preview
@Composable
private fun WithLeading() {
    PreviewOf {
        Surface {
            ConfigRow(
                labelRes = R.string.interval,
                leading = { SubSettingsToggleButton(expand = false, onClick = {}) },
                trailing = { Checkbox(checked = true, onCheckedChange = {}) }
            )
        }
    }
}

@Preview
@Composable
private fun Base() {
    PreviewOf {
        Surface {
            ConfigRow(
                labelRes = R.string.interval,
                trailing = { Checkbox(checked = true, onCheckedChange = {}) }
            )
        }
    }
}
