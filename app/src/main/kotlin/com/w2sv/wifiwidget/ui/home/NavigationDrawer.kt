package com.w2sv.wifiwidget.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.runtime.setValue
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
import com.w2sv.androidutils.extensions.openUrl
import com.w2sv.androidutils.extensions.playStoreUrl
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun NavigationDrawerPrev() {
    WifiWidgetTheme {
        StatefulNavigationDrawer(initialValue = DrawerValue.Open) {
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatefulNavigationDrawer(
    modifier: Modifier = Modifier,
    initialValue: DrawerValue = DrawerValue.Closed,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    content: @Composable (DrawerState) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = initialValue)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showThemeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val theme by viewModel.inAppThemeState.collectAsState()
    val themeRequiringUpdate by viewModel.inAppThemeState.requiringUpdate.collectAsState()

    if (showThemeDialog) {
        ThemeDialog(
            onDismissRequest = {
                viewModel.inAppThemeState.reset()
                showThemeDialog = false
            },
            theme = { theme },
            onThemeSelected = { viewModel.inAppThemeState.value = it },
            applyButtonEnabled = { themeRequiringUpdate },
            onApplyButtonPress = {
                viewModel.inAppThemeState.apply()
                showThemeDialog = false
                context.showToast("Updated Theme")
            }
        )
    }

    val closeDrawer: () -> Unit = {
        scope.launch {
            drawerState.close()
        }
    }

    BackHandler(drawerState.isOpen, closeDrawer)

    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            NavigationDrawerContent(
                closeDrawer = closeDrawer,
                onItemThemePressed = { showThemeDialog = true }
            )
        },
        drawerState = drawerState
    ) {
        content(drawerState)
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
                            it.showToast("You're not signed into the Play Store \uD83E\uDD14")
                        }
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_github_24,
                        R.string.code
                    ) {
                        it
                            .openUrl("https://github.com/w2sv/WiFi-Widget")
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

@Preview
@Composable
fun ThemeDialogPrev() {
    WifiWidgetTheme {
        ThemeDialog(
            onDismissRequest = { /*TODO*/ },
            theme = { 1 },
            onThemeSelected = {},
            applyButtonEnabled = { true },
            onApplyButtonPress = {}
        )
    }
}

@Composable
private fun ThemeDialog(
    onDismissRequest: () -> Unit,
    theme: () -> Int,
    onThemeSelected: (Int) -> Unit,
    applyButtonEnabled: () -> Boolean,
    onApplyButtonPress: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { JostText(text = stringResource(id = R.string.theme)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_nightlight_24),
                contentDescription = "@null",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        confirmButton = {
            DialogButton(onClick = { onApplyButtonPress() }, enabled = applyButtonEnabled()) {
                JostText(text = stringResource(id = R.string.apply))
            }
        },
        dismissButton = {
            DialogButton(onClick = onDismissRequest) {
                JostText(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            ThemeSelectionRow(selected = theme, onSelected = onThemeSelected)
        }
    )
}