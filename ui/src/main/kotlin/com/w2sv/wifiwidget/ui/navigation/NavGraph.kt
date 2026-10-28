package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.screen.home.HomeRoute
import com.w2sv.wifiwidget.ui.screen.widgetconfig.WidgetConfigRoute

@Composable
fun NavGraph(themeController: ThemeController) {
    val navigator = rememberNavigator()
    val density = LocalDensity.current

    NavDisplay(
        backStack = navigator.backStack,
        onBack = navigator::popBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        transitionSpec = { xAxisEnterTransition togetherWith xAxisExitTransition(density) },
        popTransitionSpec = { zAxisEnterTransition togetherWith zAxisExitTransition },
        predictivePopTransitionSpec = { zAxisEnterTransition togetherWith zAxisExitTransition },
        entryProvider = entryProvider {
            entry<Screen.Home> {
                HomeRoute(
                    themeController = themeController,
                    toWidgetConfiguration = navigator::toWidgetConfiguration
                )
            }
            entry<Screen.WidgetConfiguration> { WidgetConfigRoute(navigateBack = navigator::popBackStack) }
        }
    )
}
