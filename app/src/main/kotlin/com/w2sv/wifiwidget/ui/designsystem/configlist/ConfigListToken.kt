package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha

object ConfigListToken {
    /** To be applied when item does not contain a checkbox, since they already come with plenty of padding. */
    val itemSpacing = 12.dp
    val checkRowEndPadding = 8.dp
    val startPaddingIfNoToggleButton = 28.dp
    val expandCollapseButtonWidth = 48.dp
    val startPaddingSecondLevelSubSettings = 24.dp
    val subSettingsStartPadding = 10.dp
    val subSettingsBottomMargin = 4.dp
    val subSettingsTopMargin = 8.dp

    object FontSize {
        val subSetting = 14.sp
    }

    object TextStyle {
        val explanation
            @ReadOnlyComposable
            @Composable
            get() = LocalTextStyle.current.copy(fontSize = 13.sp, color = colorScheme.onSurfaceVariantLowAlpha)
    }
}
