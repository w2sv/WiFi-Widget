package com.w2sv.wifiwidget.ui.screen.home.components.navdrawer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.util.useDarkTheme

@Stable
class DrawerActionScope(val context: Context, val themeController: ThemeController, val useDarkTheme: Boolean)

@Composable
fun rememberDrawerActionScope(themeController: ThemeController, context: Context = LocalContext.current): DrawerActionScope {
    val useDarkTheme = useDarkTheme(themeController.settings.theme)
    return remember(context, useDarkTheme, themeController) {
        DrawerActionScope(
            context = context,
            themeController = themeController,
            useDarkTheme = useDarkTheme
        )
    }
}
