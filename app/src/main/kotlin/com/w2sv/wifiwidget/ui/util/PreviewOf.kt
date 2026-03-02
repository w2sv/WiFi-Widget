package com.w2sv.wifiwidget.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.wifiwidget.ui.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.LocalUseDarkTheme
import com.w2sv.wifiwidget.ui.navigation.LocalNavigator
import com.w2sv.wifiwidget.ui.navigation.PreviewNavigator
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.PreviewLocationAccessCapability
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Composable
fun PreviewOf(
    useDarkTheme: Boolean = false,
    useAmoledBlackTheme: Boolean = false,
    useDynamicColors: Boolean = false,
    content: @Composable () -> Unit
) {
    check(LocalInspectionMode.current) { "Calling preview composable outside of preview" }

    AppTheme(
        useDarkTheme = useDarkTheme,
        useAmoledBlackTheme = useAmoledBlackTheme,
        useDynamicColors = useDynamicColors
    ) {
        CompositionLocalProvider(
            LocalNavigator provides PreviewNavigator(),
            LocalLocationAccessCapability provides PreviewLocationAccessCapability(),
            LocalUseDarkTheme provides false,
            content = content
        )
    }
}

@Preview(name = "Phone", device = PHONE, showSystemUi = true)
@Preview(
    name = "Phone - Landscape",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true
)
@Preview(
    name = "Tablet",
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
    showSystemUi = true
)
@Preview(name = "Tablet - Landscape", device = TABLET, showSystemUi = true)
annotation class ScreenPreviews
