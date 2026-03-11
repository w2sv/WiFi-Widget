package com.w2sv.wifiwidget.ui.screen.home.components.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.ui.sharedstate.theme.ThemeController
import com.w2sv.wifiwidget.ui.sharedstate.theme.previewThemeController
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.add

@Composable
fun NavigationDrawer(
    state: DrawerState,
    themeController: ThemeController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            NavigationDrawerSheet(
                drawerState = state,
                themeController = themeController
            )
        },
        drawerState = state,
        content = content
    )
}

@Composable
private fun NavigationDrawerSheet(
    drawerState: DrawerState,
    themeController: ThemeController,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = modifier,
        windowInsets = WindowInsets()
    ) {
        val elements = remember { navigationDrawerElements() }
        val actionScope = rememberDrawerActionScope(themeController)

        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentPadding = WindowInsets.systemBarsIgnoringVisibility.asPaddingValues().add(top = 16.dp)
        ) {
            item {
                Header(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
            items(elements, key = { it.hashCode() }, contentType = { it::class }) { element ->
                NavigationDrawerElement(element, actionScope)
            }
        }
    }
}

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        NavigationDrawerSheet(drawerState = rememberDrawerState(DrawerValue.Open), themeController = previewThemeController())
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painterResource(id = R.drawable.logo_foreground),
            null,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(text = stringResource(id = R.string.version).format(BuildConfig.VERSION_NAME))
    }
}
