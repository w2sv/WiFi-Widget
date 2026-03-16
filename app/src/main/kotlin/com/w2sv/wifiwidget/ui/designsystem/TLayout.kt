package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.FloatRange
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
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutBaseScope
import androidx.constraintlayout.compose.Dimension
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.theme.explanation

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
    CentralEnd,
    ParentEnd;

    companion object {
        val Default get() = CentralEnd
    }
}

@Composable
fun TLayout(
    central: BoxScopeComposable,
    modifier: Modifier = Modifier,
    leading: BoxScopeComposable? = null,
    trailing: BoxScopeComposable? = null,
    below: BoxScopeComposable? = null,
    centralMargins: Margins = Margins.empty,
    belowMargins: Margins = Margins.empty,
    belowEndAnchoring: BelowEndAnchoring = BelowEndAnchoring.Default
) {
    ConstraintLayout(modifier = modifier) {
        val (leadingRef, centralRef, belowRef, trailingRef) = createRefs()

        val hasLeading = leading != null
        val hasTrailing = trailing != null

        val topRowTop = createTopBarrier(leadingRef, centralRef, trailingRef)
        val topRowBottom = createBottomBarrier(leadingRef, centralRef, trailingRef)

        leading?.let {
            Box(
                modifier = Modifier.constrainAs(leadingRef) {
                    start.linkTo(parent.start)
                    linkTo(top = topRowTop, bottom = topRowBottom)
                },
                content = it
            )
        }

        Box(
            modifier = Modifier.constrainAs(centralRef) {
                linkTo(
                    top = topRowTop,
                    bottom = topRowBottom,
                    start = if (hasLeading) leadingRef.end else parent.start,
                    end = if (hasTrailing) trailingRef.start else parent.end,
                    margins = centralMargins
                )
                width = Dimension.fillToConstraints
            },
            content = central
        )

        trailing?.let {
            Box(
                modifier = Modifier.constrainAs(trailingRef) {
                    linkTo(
                        top = topRowTop,
                        bottom = topRowBottom,
                        start = centralRef.end,
                        end = parent.end
                    )
                },
                content = it
            )
        }

        below?.let {
            Box(
                modifier = Modifier.constrainAs(belowRef) {
                    linkTo(
                        start = centralRef.start,
                        end = when (belowEndAnchoring) {
                            BelowEndAnchoring.CentralEnd -> centralRef.end
                            BelowEndAnchoring.ParentEnd -> parent.end
                        },
                        top = centralRef.bottom,
                        bottom = parent.bottom,
                        margins = belowMargins
                    )
                    width = Dimension.fillToConstraints
                },
                content = it
            )
        }
    }
}

private fun ConstrainScope.linkTo(
    start: ConstraintLayoutBaseScope.VerticalAnchor,
    top: ConstraintLayoutBaseScope.HorizontalAnchor,
    end: ConstraintLayoutBaseScope.VerticalAnchor,
    bottom: ConstraintLayoutBaseScope.HorizontalAnchor,
    margins: Margins,
    goneMargins: Margins = Margins.empty,
    @FloatRange(from = 0.0, to = 1.0) horizontalBias: Float = 0.5f,
    @FloatRange(from = 0.0, to = 1.0) verticalBias: Float = 0.5f
) {
    linkTo(
        start = start,
        end = end,
        startMargin = margins.start,
        endMargin = margins.end,
        startGoneMargin = goneMargins.start,
        endGoneMargin = goneMargins.end,
        bias = horizontalBias
    )
    linkTo(
        top = top,
        bottom = bottom,
        topMargin = margins.top,
        bottomMargin = margins.bottom,
        topGoneMargin = goneMargins.top,
        bottomGoneMargin = goneMargins.bottom,
        bias = verticalBias
    )
}

@Preview
@Composable
private fun Complete() {
    AppTheme {
        Surface {
            TLayout(
                modifier = Modifier.fillMaxWidth(),
                central = {
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
                        style = MaterialTheme.typography.explanation
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
                central = {
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
                        style = MaterialTheme.typography.explanation,
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
                central = {
                    Text(
                        text = stringResource(R.string.dynamic_colors),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leading = { Icon(painterResource(R.drawable.ic_palette_24), contentDescription = null) },
                below = {
                    Text(
                        text = stringResource(R.string.use_colors_derived_from_your_wallpaper),
                        style = MaterialTheme.typography.explanation
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
                central = {
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
                        style = MaterialTheme.typography.explanation
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
                central = {
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
