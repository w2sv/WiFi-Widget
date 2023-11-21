package com.w2sv.wifiwidget.ui.components.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    state: DrawerState,
    modifier: Modifier = Modifier,
    inAppThemeVM: AppViewModel = viewModel(),
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            NavigationDrawerSheet {
                NavigationDrawerSheetContent(
                    closeDrawer = {
                        scope.launch {
                            state.close()
                        }
                    },
                    appearanceSection = { modifier ->
                        AppearanceSection(
                            selectedTheme = inAppThemeVM.inAppTheme.collectAsStateWithLifecycle(
                                Theme.SystemDefault
                            ).value,
                            onThemeSelected = { inAppThemeVM.saveInAppTheme(it) },
                            useDynamicColors = inAppThemeVM.useDynamicTheme.collectAsStateWithLifecycle(
                                false
                            ).value,
                            onToggleDynamicColors = { inAppThemeVM.saveUseDynamicTheme(it) },
                            modifier = modifier,
                        )
                    },
                )
            }
        },
        drawerState = state,
    ) {
        content()
    }
}
