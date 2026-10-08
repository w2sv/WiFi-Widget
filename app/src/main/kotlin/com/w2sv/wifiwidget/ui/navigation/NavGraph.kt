package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.w2sv.composed.runtime.OnChange
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.screen.home.HomeScreenRoute
import com.w2sv.wifiwidget.ui.screen.widgetconfig.WidgetConfigScreenRoute
import slimber.log.i

@Composable
fun NavGraph(initialScreen: Screen, themeController: ThemeController) {
    val backStack = rememberNavBackStack(initialScreen)
    val navigator = remember(backStack) { NavigatorImpl(backStack) }

    OnChange(backStack.size) { i { "BackStack=${backStack.map { screen -> screen::class.java.simpleName }}" } }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            },
            entryProvider = entryProvider {
                entry<Screen.Home> { HomeScreenRoute(themeController) }
                entry<Screen.WidgetConfiguration> { WidgetConfigScreenRoute() }
            }
        )
    }
}
