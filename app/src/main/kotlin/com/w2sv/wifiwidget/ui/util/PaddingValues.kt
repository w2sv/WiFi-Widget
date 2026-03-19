package com.w2sv.wifiwidget.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// TODO: composed

fun PaddingValues.add(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): PaddingValues =
    PaddingValues(
        start = calculateStartPadding(LayoutDirection.Ltr) + start,
        top = calculateTopPadding() + top,
        end = calculateEndPadding(LayoutDirection.Ltr) + end,
        bottom = calculateBottomPadding() + bottom
    )

@Stable
fun paddingValues(
    bottom: Dp = 0.dp,
    top: Dp = 0.dp,
    horizontal: Dp = 0.dp
): PaddingValues =
    PaddingValues(
        start = horizontal,
        top = top,
        end = horizontal,
        bottom = bottom
    )
