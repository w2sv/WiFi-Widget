package com.w2sv.wifiwidget.ui.designsystem.theme

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R

private val defaultTypography = Typography()
private val jost = Font(R.font.jost).toFontFamily()

val WiFiWidgetTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = jost),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = jost),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = jost),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = jost),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = jost),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = jost),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = jost),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = jost),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = jost),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = jost),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = jost),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = jost),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = jost),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = jost),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = jost)
)

val Typography.explanation
    @ReadOnlyComposable
    @Composable
    get() = bodyMedium.copy(fontSize = 13.sp, color = colorScheme.onSurfaceVariantLowAlpha)
