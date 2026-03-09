package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.theme.AppTheme

@Composable
fun NavigationDrawerScreenTopAppBar(modifier: Modifier = Modifier, onNavigationIconClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                maxLines = 1
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.open_navigation_drawer)
                )
            }
        },
        windowInsets = WindowInsets.statusBarsIgnoringVisibility // Apply status bar insets also if status bar hidden during immersive mode
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    )
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        NavigationDrawerScreenTopAppBar {}
    }
}
