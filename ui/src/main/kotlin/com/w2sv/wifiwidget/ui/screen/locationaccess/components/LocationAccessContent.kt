package com.w2sv.wifiwidget.ui.screen.locationaccess.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.w2sv.composed.ui.thenIf
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.AppUrl
import com.w2sv.wifiwidget.ui.designsystem.animation.EntranceState
import com.w2sv.wifiwidget.ui.designsystem.animation.entrance
import com.w2sv.wifiwidget.ui.screen.locationaccess.LocationAccessScreenTokens
import kotlinx.coroutines.launch

@Composable
internal fun LocationAccessContent(
    onboarding: Boolean,
    entrance: EntranceState,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val introductionIndex = if (onboarding) 1 else 0

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!compact || onboarding) {
            LocationAccessHeader(
                onboarding = onboarding,
                modifier = Modifier.thenIf(onboarding) { entrance(entrance, index = 0) },
                compact = compact
            )
        }
        Text(
            text = stringResource(R.string.location_access_intro),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .entrance(entrance, index = introductionIndex)
                .padding(top = if (compact) 12.dp else 24.dp, bottom = if (compact) 20.dp else 30.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 18.dp)) {
            LocationAccessExplanationRow(
                iconRes = R.drawable.ic_wifi_24,
                title = stringResource(R.string.location_wifi_identification),
                description = AnnotatedString(stringResource(R.string.location_wifi_identification_explanation)),
                modifier = Modifier.entrance(entrance, index = introductionIndex + 1)
            )
            LocationAccessExplanationRow(
                iconRes = R.drawable.ic_autorenew_24,
                title = stringResource(R.string.location_background_updates),
                description = AnnotatedString(stringResource(R.string.location_background_updates_explanation)),
                modifier = Modifier.entrance(entrance, index = introductionIndex + 2)
            )
            LocationAccessExplanationRow(
                iconRes = R.drawable.ic_privacy_24,
                title = stringResource(R.string.location_privacy),
                description = privacyText(),
                modifier = Modifier.entrance(entrance, index = introductionIndex + 3)
            )
        }
    }
}

@Composable
private fun LocationAccessHeader(
    onboarding: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!compact) {
            RotatingLocationIcon(modifier = Modifier.size(104.dp))
        }
        if (onboarding) {
            Text(
                text = stringResource(R.string.location_access),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun RotatingLocationIcon(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val tapRotation = remember { Animatable(0f) }
    val rotation = rememberInfiniteTransition(label = "Location icon rotation")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 45_000,
                    easing = LinearEasing
                )
            ),
            label = "Shape rotation"
        )

    Box(
        modifier = modifier
            .clearAndSetSemantics {}
            .clickable(interactionSource = null, indication = null) {
                scope.launch {
                    // Tap spin adapted from Voice's AboutFooter (Paul Woitaschek and contributors, GPLv3):
                    // https://github.com/PaulWoitaschek/Voice/blob/main/features/settings/src/main/kotlin/voice/features/settings/views/AboutFooter.kt
                    tapRotation.animateTo(
                        targetValue = tapRotation.value + 90f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
                    )
                }
            },
        contentAlignment = Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { rotationZ = rotation.value + tapRotation.value }
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialShapes.Cookie9Sided.toShape()
                )
        )

        Icon(
            painter = painterResource(R.drawable.ic_location_on_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxSize(0.52f)
        )
    }
}

@Composable
private fun privacyText(): AnnotatedString {
    val privacyExplanation = stringResource(R.string.location_privacy_explanation)
    val privacyLink = stringResource(R.string.read_our_privacy_policy)
    val linkColor = MaterialTheme.colorScheme.tertiary
    return remember(privacyExplanation, privacyLink, linkColor) {
        buildAnnotatedString {
            append(privacyExplanation)
            append("\n")
            withStyle(SpanStyle(color = linkColor, textDecoration = Underline)) {
                withLink(LinkAnnotation.Url(AppUrl.PRIVACY_POLICY)) {
                    append(privacyLink)
                }
            }
        }
    }
}

@Composable
private fun LocationAccessExplanationRow(
    @DrawableRes iconRes: Int,
    title: String,
    description: AnnotatedString,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(LocationAccessScreenTokens.explanationIconSize)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
