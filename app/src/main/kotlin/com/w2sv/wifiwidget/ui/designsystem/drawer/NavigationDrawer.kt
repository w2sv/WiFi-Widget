package com.w2sv.wifiwidget.ui.designsystem.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.Theme
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared_viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.utils.SystemBarsIgnoringVisibilityPaddedColumn
import com.w2sv.wifiwidget.ui.utils.activityViewModel

@Stable
data class NavigationDrawerItemConfiguration(
    val theme: () -> Theme,
    val setTheme: (Theme) -> Unit,
    val useAmoledBlackTheme: () -> Boolean,
    val setUseAmoledBlackTheme: (Boolean) -> Unit,
    val useDynamicColors: () -> Boolean,
    val setUseDynamicColors: (Boolean) -> Unit,
)

@Composable
private fun rememberNavigationDrawerItemConfiguration(appVM: AppViewModel = activityViewModel()): NavigationDrawerItemConfiguration {
    val theme by appVM.theme.collectAsStateWithLifecycle()
    val useAmoledBlackTheme by appVM.useAmoledBlackTheme.collectAsStateWithLifecycle()
    val useDynamicColors by appVM.useDynamicColors.collectAsStateWithLifecycle()

    return remember {
        NavigationDrawerItemConfiguration(
            theme = { theme },
            setTheme = appVM::saveTheme,
            useAmoledBlackTheme = { useAmoledBlackTheme },
            setUseAmoledBlackTheme = appVM::saveUseAmoledBlackTheme,
            useDynamicColors = { useDynamicColors },
            setUseDynamicColors = appVM::saveUseDynamicColors
        )
    }
}

@Composable
fun NavigationDrawer(
    state: DrawerState,
    modifier: Modifier = Modifier,
    itemConfiguration: NavigationDrawerItemConfiguration = rememberNavigationDrawerItemConfiguration(),
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            NavigationDrawerSheet(
                itemConfiguration = itemConfiguration
            )
        },
        drawerState = state,
        content = content
    )
}

@Composable
private fun NavigationDrawerSheet(
    itemConfiguration: NavigationDrawerItemConfiguration,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier,
        windowInsets = zeroInsets
    ) {
        SystemBarsIgnoringVisibilityPaddedColumn(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            )
            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
            NavigationDrawerSheetItemColumn(
                itemConfiguration = itemConfiguration,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }
    }
}

private val zeroInsets = WindowInsets(0, 0, 0, 0)
private val horizontalPadding = 24.dp

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painterResource(id = R.drawable.logo_foreground),
            null,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(id = R.string.version).format(BuildConfig.VERSION_NAME),
        )
    }
}