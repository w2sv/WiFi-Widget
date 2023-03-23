package com.w2sv.wifiwidget.ui.screens.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.playStoreUrl
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.extensions.openUrlWithActivityNotFoundHandling
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        StatefulNavigationDrawer(initialValue = DrawerValue.Open) { _, _, _ ->
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatefulNavigationDrawer(
    modifier: Modifier = Modifier,
    initialValue: DrawerValue = DrawerValue.Closed,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    content: @Composable (openDrawer: () -> Unit, closeDrawer: () -> Unit, drawerOpen: () -> Boolean) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = initialValue)
    val scope = rememberCoroutineScope()
    val springSpec = FloatSpringSpec(Spring.DampingRatioMediumBouncy)

    val closeDrawer: () -> Unit = {
        scope.launch {
            drawerState.animateTo(
                DrawerValue.Closed,
                springSpec
            )
        }
    }

    val (showThemeDialog, setShowThemeDialog) = rememberSaveable {
        mutableStateOf(false)
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            NavigationDrawerContent(
                closeDrawer = closeDrawer,
                onItemThemePressed = {
                    // show dialog after delay for display of navigationDrawer close animation
                    scope.launchDelayed(250L) {
                        setShowThemeDialog(true)
                    }
                }
            )
        },
        drawerState = drawerState
    ) {
        content(
            openDrawer = {
                scope.launch {
                    drawerState.animateTo(DrawerValue.Open, springSpec)
                }
            },
            closeDrawer = closeDrawer,
            drawerOpen = { drawerState.isOpen }
        )
    }

    val theme by viewModel.inAppThemeState.collectAsState()
    val themeRequiringUpdate by viewModel.inAppThemeState.requiringUpdate.collectAsState()
    val context = LocalContext.current

    if (showThemeDialog) {
        ThemeSelectionDialog(
            onDismissRequest = {
                viewModel.inAppThemeState.reset()
                setShowThemeDialog(false)
            },
            selectedTheme = { theme },
            onThemeSelected = { viewModel.inAppThemeState.value = it },
            applyButtonEnabled = { themeRequiringUpdate },
            onApplyButtonPress = {
                viewModel.inAppThemeState.apply()
                setShowThemeDialog(false)
                context.showToast("Updated Theme")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawerContent(closeDrawer: () -> Unit, onItemThemePressed: () -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.padding(vertical = 32.dp)) {
                Image(
                    painterResource(id = R.drawable.logo_foreground),
                    null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                VersionText(Modifier.padding(top = 26.dp))
            }
            Divider(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp)
            )
            remember {
                listOf(
                    NavigationDrawerItem(
                        R.drawable.ic_nightlight_24,
                        R.string.theme
                    ) {
                        onItemThemePressed()
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_share_24,
                        R.string.share
                    ) {
                        ShareCompat.IntentBuilder(it)
                            .setType("text/plain")
                            .setText("Check out WiFi Widget!\n${it.playStoreUrl}")
                            .setChooserTitle("Choose an app")
                            .startChooser()
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_star_rate_24,
                        R.string.rate
                    ) {
                        try {
                            it.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(it.playStoreUrl)
                                )
                                    .setPackage("com.android.vending")
                            )
                        } catch (e: ActivityNotFoundException) {
                            it.showToast("You're not signed into the Play Store")
                        }
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_github_24,
                        R.string.code
                    ) {
                        it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget")
                    }
                )
            }
                .forEach {
                    NavigationDrawerItem(properties = it, closeDrawer = closeDrawer)
                }
        }
    }
}

@Composable
fun VersionText(modifier: Modifier = Modifier) {
    JostText(
        text = "Version: ${BuildConfig.VERSION_NAME}",
        modifier = modifier
    )
}

@Stable
private data class NavigationDrawerItem(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val callback: (Context) -> Unit
)

@Composable
private fun NavigationDrawerItem(
    properties: NavigationDrawerItem,
    closeDrawer: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                properties.callback(context)
                closeDrawer()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(size = dimensionResource(id = R.dimen.size_icon)),
            painter = painterResource(id = properties.icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        JostText(
            text = stringResource(id = properties.label),
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}