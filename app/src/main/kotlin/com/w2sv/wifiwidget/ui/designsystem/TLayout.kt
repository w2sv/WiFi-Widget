package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListToken
import com.w2sv.wifiwidget.ui.theme.AppTheme

typealias BoxScopeComposable = @Composable BoxScope.() -> Unit

@Immutable
data class Margins(val start: Dp = 0.dp, val top: Dp = 0.dp, val end: Dp = 0.dp, val bottom: Dp = 0.dp) {
    companion object {
        val empty = Margins()
    }
}

/**
 * End-anchoring applied to the below slot.
 */
enum class BelowEndAnchoring {
    LabelEnd,
    ParentEnd;

    companion object {
        val Default get() = LabelEnd
    }
}

@Composable
fun TLayout(
    label: BoxScopeComposable,
    modifier: Modifier = Modifier,
    leading: BoxScopeComposable? = null,
    trailing: BoxScopeComposable? = null,
    below: BoxScopeComposable? = null,
    labelMargins: Margins = Margins(start = 16.dp),
    belowMargins: Margins = Margins(top = 2.dp),
    belowEndAnchoring: BelowEndAnchoring = BelowEndAnchoring.Default
) {
    ConstraintLayout(modifier = modifier) {
        val (leadingRef, labelRef, belowRef, trailingRef) = createRefs()

        val hasLeading = leading != null
        val hasTrailing = trailing != null
        val hasBelow = below != null

        leading?.let {
            Box(
                modifier = Modifier.constrainAs(leadingRef) {
                    start.linkTo(parent.start)
                    centerVerticallyTo(labelRef)
                },
                content = it
            )
        }

        Box(
            modifier = Modifier.constrainAs(labelRef) {
                linkTo(
                    top = parent.top,
                    bottom = if (hasBelow) belowRef.top else parent.bottom
                )
                linkTo(
                    start = if (hasLeading) leadingRef.end else parent.start,
                    end = if (hasTrailing) trailingRef.start else parent.end,
                    startMargin = labelMargins.start
                )
                width = Dimension.fillToConstraints
            },
            content = label
        )

        trailing?.let {
            Box(
                modifier = Modifier.constrainAs(trailingRef) {
                    start.linkTo(labelRef.end)
                    end.linkTo(parent.end)
                    centerVerticallyTo(labelRef)
                },
                content = it
            )
        }

        below?.let {
            Box(
                modifier = Modifier.constrainAs(belowRef) {
                    top.linkTo(labelRef.bottom, margin = belowMargins.top)
                    linkTo(
                        start = labelRef.start,
                        end = when (belowEndAnchoring) {
                            BelowEndAnchoring.LabelEnd -> labelRef.end
                            BelowEndAnchoring.ParentEnd -> parent.end
                        }
                    )
                    width = Dimension.fillToConstraints
                },
                content = it
            )
        }
    }
}

@Preview
@Composable
private fun Complete() {
    AppTheme {
        Surface {
            TLayout(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(R.string.dynamic_colors),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leading = { Icon(painterResource(R.drawable.ic_palette_24), contentDescription = null) },
                trailing = {
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                },
                below = {
                    Text(
                        text = stringResource(R.string.use_colors_derived_from_your_wallpaper),
                        style = ConfigListToken.TextStyle.explanation
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun BelowEndAnchoredToParent() {
    AppTheme {
        Surface {
            TLayout(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(R.string.dynamic_colors),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leading = { Icon(painterResource(R.drawable.ic_palette_24), contentDescription = null) },
                trailing = {
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                },
                below = {
                    Text(
                        text = stringResource(R.string.use_colors_derived_from_your_wallpaper),
                        style = ConfigListToken.TextStyle.explanation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red)
                    )
                },
                belowEndAnchoring = BelowEndAnchoring.ParentEnd
            )
        }
    }
}

@Preview
@Composable
private fun WithoutTrailing() {
    AppTheme {
        Surface {
            TLayout(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(R.string.dynamic_colors),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leading = { Icon(painterResource(R.drawable.ic_palette_24), contentDescription = null) },
                below = {
                    Text(
                        text = stringResource(R.string.use_colors_derived_from_your_wallpaper),
                        style = ConfigListToken.TextStyle.explanation
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun WithoutLeading() {
    AppTheme {
        Surface {
            TLayout(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(R.string.dynamic_colors),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                trailing = {
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                },
                below = {
                    Text(
                        text = stringResource(R.string.use_colors_derived_from_your_wallpaper),
                        style = ConfigListToken.TextStyle.explanation
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun WithoutLeadingAndBelow() {
    AppTheme {
        Surface {
            TLayout(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = stringResource(R.string.dynamic_colors),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                trailing = {
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                }
            )
        }
    }
}
