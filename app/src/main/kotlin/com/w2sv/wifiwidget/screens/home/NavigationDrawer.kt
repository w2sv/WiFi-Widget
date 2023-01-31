package com.w2sv.wifiwidget.screens.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import com.w2sv.androidutils.extensions.goToWebpage
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme
import com.w2sv.wifiwidget.utils.playStoreLink
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun NavigationDrawerPreview() {
    WifiWidgetTheme {
        NavigationDrawer {
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(content: @Composable (DrawerState) -> Unit) {
    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val properties = NavigationDrawerItemProperties.get(LocalContext.current)

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.secondary) {
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
                        JostText(
                            text = "Version: ${BuildConfig.VERSION_NAME}",
                            modifier = Modifier.padding(top = 26.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Divider(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 12.dp)
                    )
                    properties.forEach {
                        NavigationDrawerItem(properties = it) {
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    }
                }
            }
        },
        drawerState = drawerState
    ) {
        content(drawerState)
    }
}

private data class NavigationDrawerItemProperties(
    @DrawableRes val icon: Int,
    val label: String,
    val callback: () -> Unit
) {
    companion object {
        fun get(context: Context): List<NavigationDrawerItemProperties> =
            listOf(
                NavigationDrawerItemProperties(R.drawable.ic_share_24, "Share") {
                    ShareCompat.IntentBuilder(context)
                        .setType("text/plain")
                        .setText("Check out WiFi Widget!\n${context.playStoreLink}")
                        .setChooserTitle("Choose an app")
                        .startChooser()
                },
                NavigationDrawerItemProperties(R.drawable.ic_star_rate_24, "Rate") {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context.playStoreLink)
                            )
                                .setPackage("com.android.vending")
                        )
                    } catch (e: ActivityNotFoundException) {
                        context.showToast("You're not signed into the Play Store \uD83E\uDD14")
                    }
                },
                NavigationDrawerItemProperties(R.drawable.ic_github_24, "Code") {
                    context
                        .goToWebpage("https://github.com/w2sv/WiFi-Widget")
                }
            )
    }
}

@Composable
private fun NavigationDrawerItem(
    properties: NavigationDrawerItemProperties,
    closeDrawer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                properties.callback()
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
            text = properties.label,
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}