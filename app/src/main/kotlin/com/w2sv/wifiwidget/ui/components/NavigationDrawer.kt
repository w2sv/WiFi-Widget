package com.w2sv.wifiwidget.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.ui.PreferencesDataStoreBackedUnconfirmedStatesViewModel
import com.w2sv.common.data.repositories.PreferencesRepository
import com.w2sv.common.extensions.openUrlWithActivityNotFoundHandling
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.components.ThemeSelectionDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationDrawerViewModel @Inject constructor(preferencesRepository: PreferencesRepository) :
    PreferencesDataStoreBackedUnconfirmedStatesViewModel<PreferencesRepository>(
        preferencesRepository
    ) {

    val unconfirmedInAppTheme = makeUnconfirmedEnumValuedStateFlow(
        appliedFlow = preferencesRepository.inAppTheme,
        preferencesKey = PreferencesRepository.Key.IN_APP_THEME,
        onStateSynced = {}
    )
}


//@Preview
//@Composable
//private fun Prev() {
//    AppTheme {
//        NavigationDrawer(initialValue = DrawerValue.Open) { _, _, _ ->
//        }
//    }
//}

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
    navigationDrawerVM: NavigationDrawerViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val theme by navigationDrawerVM.unconfirmedInAppTheme.collectAsState()
    val themeRequiringUpdate by navigationDrawerVM.unconfirmedInAppTheme.statesDissimilar.collectAsState()

    var showThemeSelectionDialog by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                ThemeSelectionDialog(
                    onDismissRequest = {
                        scope.launch {
                            navigationDrawerVM.unconfirmedInAppTheme.reset()
                        }
                        value = false
                    },
                    selectedTheme = { theme },
                    onThemeSelected = { navigationDrawerVM.unconfirmedInAppTheme.value = it },
                    applyButtonEnabled = { themeRequiringUpdate },
                    onApplyButtonClick = {
                        scope.launch {
                            navigationDrawerVM.unconfirmedInAppTheme.sync()
                            context.showToast(context.getString(R.string.updated_theme))
                        }
                        value = false
                    }
                )
            }
        }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            Content(
                closeDrawer = {
                    scope.launch {
                        state.closeDrawer()
                    }
                },
                onItemThemePressed = {
                    // show dialog after delay for display of navigationDrawer close animation
                    scope.launchDelayed(250L) {
                        showThemeSelectionDialog = true
                    }
                }
            )
        },
        drawerState = state
    ) {
        content()
    }
}

@Composable
private fun Content(closeDrawer: () -> Unit, onItemThemePressed: () -> Unit) {
    val context = LocalContext.current

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(modifier = Modifier.padding(vertical = 32.dp))
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
                            .setText(context.getString(R.string.share_action_text))
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
                                    Uri.parse(appPlayStoreUrl(it))
                                )
                                    .setPackage("com.android.vending")
                            )
                        } catch (e: ActivityNotFoundException) {
                            it.showToast(context.getString(R.string.you_re_not_signed_into_the_play_store))
                        }
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_policy_24,
                        R.string.privacy_policy
                    ) {
                        it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget/blob/main/PRIVACY-POLICY.md")
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_github_24,
                        R.string.source
                    ) {
                        it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget")
                    }
                )
            }
                .forEach {
                    NavigationDrawerItem(item = it, closeDrawer = closeDrawer)
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

@Stable
private data class NavigationDrawerItem(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val onClick: (Context) -> Unit
)

@Composable
private fun NavigationDrawerItem(
    item: NavigationDrawerItem,
    closeDrawer: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                item.onClick(context)
                closeDrawer()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(size = dimensionResource(id = R.dimen.size_icon)),
            painter = painterResource(id = item.icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        JostText(
            text = stringResource(id = item.label),
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}