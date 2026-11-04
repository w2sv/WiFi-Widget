package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp
import com.w2sv.composed.ui.platform.isPortraitModeActive

internal object WidgetConfigListDimens {
    val sectionSpacing = 24.dp
    val wifiHeaderToItemsSpacing = 4.dp
    val sectionCardInnerPadding = PaddingValues(vertical = 12.dp)
    val screenContentPadding: PaddingValues
        @Composable
        @ReadOnlyComposable
        get() {
            val horizontalPadding = if (isPortraitModeActive) 16.dp else 126.dp
            return PaddingValues(
                bottom = if (isPortraitModeActive) 140.dp else 90.dp, // for FABs
                top = sectionSpacing,
                start = horizontalPadding,
                end = horizontalPadding
            )
        }
}
