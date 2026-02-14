package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.w2sv.composed.core.OnChange
import com.w2sv.wifiwidget.ui.screens.home.HomeScreen
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.WidgetConfigurationScreen
import slimber.log.i

@Composable
fun NavGraph(initialScreen: Screen) {
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
                ContentTransform(
                    slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                    slideOutHorizontally(targetOffsetX = { -it / 2 }) + fadeOut()
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    slideInHorizontally(initialOffsetX = { -it / 2 }) + fadeIn(),
                    slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut()
                )
            },
            entryProvider = entryProvider {
                entry<Screen.Home> { HomeScreen() }
                entry<Screen.WidgetConfiguration> { WidgetConfigurationScreen() }
            }
        )
    }
}
