package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.composed.ui.thenIfNotNull

@Composable
fun ColorSwatch(
    brush: Brush,
    modifier: Modifier = Modifier,
    diameter: Dp = 44.dp,
    badgeOutlineColor: Color = MaterialTheme.colorScheme.surface,
    selected: Boolean = false,
    showCheckmark: Boolean = false,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    role: Role? = null
) {
    val colors = MaterialTheme.colorScheme
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 3.dp else 1.dp,
        animationSpec = tween(200),
        label = "ColorSwatchBorderWidth"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.outline,
        animationSpec = tween(200),
        label = "ColorSwatchBorderColor"
    )

    Box(modifier = modifier.size(diameter)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .thenIfNotNull(onClick) {
                    clickable(
                        onClickLabel = onClickLabel,
                        role = role,
                        onClick = onClick
                    )
                }
                .border(borderWidth, borderColor, CircleShape)
                .padding(borderWidth + 2.dp)
                .background(brush, CircleShape)
        )

        AnimatedVisibility(
            visible = selected && showCheckmark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp),
            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.7f, animationSpec = tween(180)),
            exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.7f, animationSpec = tween(120))
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(colors.primary, CircleShape)
                    .border(1.5.dp, badgeOutlineColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
