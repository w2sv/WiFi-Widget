package com.w2sv.wifiwidget.ui.screen.home.components.navdrawer

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.core.common.R

@Composable
fun NavDrawerTopBar(
    onNavigationIconClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TopAppBarDefaults.topAppBarColors()

    // Reset Material 3's container color animation when the theme changes.
    key(colors.containerColor) {
        TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = {
                IconButton(onClick = onNavigationIconClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.open_navigation_drawer)
                    )
                }
            },
            colors = colors,
            // Apply status bar insets also if the status bar is hidden during immersive mode.
            windowInsets = WindowInsets.statusBarsIgnoringVisibility
                .only(WindowInsetsSides.Horizontal + Top)
        )
    }
}
