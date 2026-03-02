package com.w2sv.wifiwidget.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.core.isLandscapeModeActive
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
import com.w2sv.wifiwidget.ui.designsystem.NavigationDrawerScreenTopAppBar
import com.w2sv.wifiwidget.ui.screen.home.drawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.screen.home.widget.WidgetCard
import com.w2sv.wifiwidget.ui.screen.home.wifistatus.WifiStatusCard
import com.w2sv.wifiwidget.ui.screen.home.wifistatus.model.WifiState
import com.w2sv.wifiwidget.ui.sharedstate.theme.ThemeController
import com.w2sv.wifiwidget.ui.util.ModifierReceivingComposable
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.ScreenPreviews
import com.w2sv.wifiwidget.ui.util.SnackbarBuilderFlow
import com.w2sv.wifiwidget.ui.util.rememberMovableContentOf
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HomeScreen(
    themeController: ThemeController,
    wifiState: WifiState,
    pinWidget: () -> Unit,
    snackbarBuilderFlow: SnackbarBuilderFlow,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val scope = rememberCoroutineScope()

    NavigationDrawer(state = drawerState, themeController = themeController) {
        Scaffold(
            topBar = { NavigationDrawerScreenTopAppBar { scope.launch { drawerState.open() } } },
            snackbarHost = { AppSnackbarHost(snackbarBuilderFlow) }
        ) { paddingValues ->
            val wifiStatusCard: ModifierReceivingComposable = rememberMovableContentOf {
                WifiStatusCard(wifiState = wifiState, modifier = it)
            }
            val widgetCard: ModifierReceivingComposable = rememberMovableContentOf {
                WidgetCard(pinWidget = pinWidget, modifier = it)
            }

            if (isLandscapeModeActive) {
                LandscapeMode(
                    paddingValues = paddingValues,
                    wifiStatusCard = wifiStatusCard,
                    widgetCard = widgetCard
                )
            } else {
                PortraitMode(
                    paddingValues = paddingValues,
                    wifiStatusCard = wifiStatusCard,
                    widgetCard = widgetCard
                )
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun Prev() {
    PreviewOf {
        HomeScreen(
            themeController = ThemeController(
                theme = { Theme.Default },
                setTheme = {},
                useAmoledBlackTheme = { true },
                setUseAmoledBlackTheme = {},
                useDynamicColors = { true },
                setUseDynamicColors = {}
            ),
            wifiState = WifiState.Disabled,
            pinWidget = {},
            snackbarBuilderFlow = emptyFlow()
        )
    }
}

@Composable
private fun LandscapeMode(
    paddingValues: PaddingValues,
    wifiStatusCard: ModifierReceivingComposable,
    widgetCard: ModifierReceivingComposable
) {
    Row(
        modifier = Modifier
            .padding(paddingValues)
            .padding(vertical = 16.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        wifiStatusCard(Modifier.fillMaxWidth(0.4f))
        widgetCard(Modifier.fillMaxWidth(0.7f))
    }
}

@Composable
private fun PortraitMode(
    paddingValues: PaddingValues,
    wifiStatusCard: ModifierReceivingComposable,
    widgetCard: ModifierReceivingComposable
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(bottom = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.15f))
        wifiStatusCard(Modifier.fillMaxWidth(0.8f))
        Spacer(Modifier.weight(0.15f))
        widgetCard(Modifier.fillMaxWidth(0.84f))
        Spacer(Modifier.weight(0.15f))
        CopyrightText()
    }
}

@Composable
private fun CopyrightText(modifier: Modifier = Modifier) {
    Text(
        text = remember { "© 2022 - ${Calendar.getInstance().get(Calendar.YEAR)} | W2SV" },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        modifier = modifier
    )
}
