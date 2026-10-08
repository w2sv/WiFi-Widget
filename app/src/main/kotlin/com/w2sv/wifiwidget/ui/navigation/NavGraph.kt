package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.screen.home.HomeRoute
import com.w2sv.wifiwidget.ui.screen.widgetconfig.WidgetConfigRoute

@Composable
fun NavGraph(themeController: ThemeController) {
    val navigator = rememberNavigator()

    NavDisplay(
        backStack = navigator.backStack,
        onBack = navigator::popBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = { swipeEdge ->
            val direction = if (swipeEdge == NavigationEvent.EDGE_RIGHT) -1 else 1

            EnterTransition.None togetherWith (
                scaleOut(
                    targetScale = 0.86f,
                    animationSpec = tween(350, easing = LinearEasing)
                ) + slideOutHorizontally(
                    targetOffsetX = { width -> direction * width / 4 },
                    animationSpec = tween(350, easing = LinearEasing)
                )
                )
        },
        entryProvider = entryProvider {
            entry<Screen.Home> {
                HomeRoute(
                    themeController = themeController,
                    toWidgetConfiguration = navigator::toWidgetConfiguration
                )
            }
            entry<Screen.WidgetConfiguration> {
                RoundedOnExit {
                    WidgetConfigRoute(navigateBack = navigator::popBackStack)
                }
            }
        }
    )
}
