package com.w2sv.wifiwidget.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.data.model.Theme
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.viewmodels.InAppThemeViewModel
import kotlinx.coroutines.launch

suspend fun DrawerState.closeDrawer() {
    animateTo(DrawerValue.Closed, defaultSpringSpec)
}

suspend fun DrawerState.openDrawer() {
    animateTo(DrawerValue.Open, defaultSpringSpec)
}

@Composable
fun NavigationDrawer(
    state: DrawerState,
    modifier: Modifier = Modifier,
    inAppThemeVM: InAppThemeViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            Content(
                closeDrawer = {
                    scope.launch {
                        state.closeDrawer()
                    }
                },
                themeSelection = {
                    ThemeSelection(
                        selectedTheme = inAppThemeVM.inAppTheme.collectAsState(Theme.SystemDefault).value,
                        onThemeSelected = { inAppThemeVM.saveInAppTheme(it) },
                        useDynamicTheme = inAppThemeVM.useDynamicTheme.collectAsState(false).value,
                        onToggleDynamicTheme = { inAppThemeVM.saveUseDynamicTheme(it) },
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(220.dp)
                    )
                }
            )
        },
        drawerState = state
    ) {
        content()
    }
}

@Composable
private fun ThemeSelection(
    useDynamicTheme: Boolean,
    onToggleDynamicTheme: (Boolean) -> Unit,
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                JostText(
                    text = stringResource(R.string.use_dynamic_theme),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = useDynamicTheme,
                    onCheckedChange = {
                        onToggleDynamicTheme(
                            it
                        )
                    }
                )
            }
        }
        ThemeSelectionRow(
            selected = selectedTheme,
            onSelected = onThemeSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        )
    }
}

@Preview
@Composable
fun ContentPrev() {
    AppTheme {
        Content(closeDrawer = { /*TODO*/ }, themeSelection = {})
    }
}

@Composable
private fun Content(closeDrawer: () -> Unit, themeSelection: @Composable () -> Unit) {
    val context = LocalContext.current

    val elements = remember {
        listOf(
            NavigationDrawerElement.SubHeader(R.string.theme),
            NavigationDrawerElement.Custom(themeSelection),
            NavigationDrawerElement.SubHeader(R.string.support),
            NavigationDrawerElement.Item(
                iconRes = R.drawable.ic_share_24,
                labelRes = R.string.share,
                onClick = {
                    ShareCompat.IntentBuilder(it)
                        .setType("text/plain")
                        .setText(context.getString(R.string.share_action_text))
                        .startChooser()
                }
            ),
            NavigationDrawerElement.Item(
                iconRes = R.drawable.ic_star_rate_24,
                labelRes = R.string.rate,
                onClick = {
                    try {
                        it.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(appPlayStoreUrl(it))
                            )
                                .setPackage("com.android.vending")
                        )
                    } catch (e: ActivityNotFoundException) {
                        it.showToast(context.getString(R.string.you_re_not_signed_into_the_play_store))
                    }
                }
            ),
            NavigationDrawerElement.SubHeader(R.string.legal),
            NavigationDrawerElement.Item(
                iconRes = R.drawable.ic_policy_24,
                labelRes = R.string.privacy_policy,
                onClick = {
                    it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget/blob/main/PRIVACY-POLICY.md")
                }
            ),
            NavigationDrawerElement.Item(
                iconRes = R.drawable.ic_copyright_24,
                labelRes = R.string.license,
                onClick = {
                    it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget/blob/main/LICENSE")
                }
            ),
            NavigationDrawerElement.SubHeader(R.string.about),
            NavigationDrawerElement.Item(
                iconRes = R.drawable.ic_mask_24,
                labelRes = R.string.creator,
                onClick = {
                    it.openUrlWithActivityNotFoundHandling("https://play.google.com/store/apps/dev?id=6884111703871536890")
                }
            ),
            NavigationDrawerElement.Item(
                iconRes = R.drawable.ic_github_24,
                labelRes = R.string.source,
                onClick = {
                    it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget")
                }
            )
        )
    }

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(modifier = Modifier.padding(vertical = 32.dp))
            Divider(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp)
            )
            elements.forEach {
                when (it) {
                    is NavigationDrawerElement.Item -> {
                        NavigationDrawerItem(item = it, closeDrawer = closeDrawer)
                    }

                    is NavigationDrawerElement.SubHeader -> {
                        NavigationDrawerSubHeader(properties = it)
                    }

                    is NavigationDrawerElement.Custom -> {
                        it.content()
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Image(
            painterResource(id = R.drawable.logo_foreground),
            null,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        VersionText(Modifier.padding(top = 26.dp))
    }
}

@Composable
private fun VersionText(modifier: Modifier = Modifier) {
    JostText(
        text = stringResource(id = R.string.version).format(BuildConfig.VERSION_NAME),
        modifier = modifier
    )
}

private sealed interface NavigationDrawerElement {
    @Stable
    data class Item(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int,
        val onClick: (Context) -> Unit
    ) : NavigationDrawerElement

    @Stable
    data class SubHeader(
        @StringRes val titleRes: Int
    ) : NavigationDrawerElement

    data class Custom(
        val content: @Composable () -> Unit
    ) : NavigationDrawerElement
}


@Composable
private fun NavigationDrawerItem(
    item: NavigationDrawerElement.Item,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                item.onClick(context)
                closeDrawer()
            }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(size = dimensionResource(id = R.dimen.size_icon)),
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        JostText(
            text = stringResource(id = item.labelRes),
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NavigationDrawerSubHeader(
    properties: NavigationDrawerElement.SubHeader,
    modifier: Modifier = Modifier
) {
    JostText(
        text = stringResource(id = properties.titleRes),
        modifier = modifier
            .padding(vertical = 4.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
}