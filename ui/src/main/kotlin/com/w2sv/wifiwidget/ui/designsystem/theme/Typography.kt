package com.w2sv.wifiwidget.ui.designsystem.theme

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.ui.R

private val jost = Font(R.font.jost).toFontFamily()

val WiFiWidgetTypography = Typography(fontFamily = jost)

val Typography.explanation
    @ReadOnlyComposable
    @Composable
    get() = bodyMedium.copy(fontSize = 13.sp, color = colorScheme.onSurfaceVariantLowAlpha)
