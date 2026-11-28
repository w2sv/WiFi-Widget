package com.w2sv.wifiwidget.ui.screen.home.components.navdrawer

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.w2sv.androidutils.content.openUrl
import com.w2sv.androidutils.content.packagePlayStoreUrl
import com.w2sv.androidutils.content.startActivity
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.androidutils.widget.showToast
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.AppUrl
import com.w2sv.wifiwidget.ui.designsystem.BoxScopeComposable
import com.w2sv.wifiwidget.ui.designsystem.IconDefaults
import com.w2sv.wifiwidget.ui.designsystem.Margins
import com.w2sv.wifiwidget.ui.designsystem.TLayout
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.designsystem.theme.explanation
import com.w2sv.wifiwidget.ui.util.VerticalAnimatedVisibility
import com.w2sv.wifiwidget.ui.util.useDarkTheme

private object NavDrawerTokens {
    val actionPadding = PaddingValues(top = 12.dp)
    val groupHeaderPadding = PaddingValues(top = 20.dp)
}

internal fun LazyListScope.navDrawerItems(
    context: Context,
    themeController: ThemeController,
    onLocationAccessClick: () -> Unit
) {
    drawerHeader(R.string.appearance)
    item(key = R.string.theme, contentType = "action") {
        DrawerRow(
            iconRes = R.drawable.ic_nightlight_24,
            labelRes = R.string.theme,
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeSelectionRow(
                selected = themeController.settings.theme,
                onSelect = { theme -> themeController.updateSettings { it.copy(theme = theme) } },
                badgeOutlineColor = MaterialTheme.colorScheme.surfaceContainerLow,
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            )
        }
    }
    item(key = R.string.amoled_black, contentType = "action") {
        VerticalAnimatedVisibility(visible = useDarkTheme(themeController.settings.theme)) {
            DrawerSwitchRow(
                iconRes = R.drawable.ic_contrast_24,
                labelRes = R.string.amoled_black,
                explanationRes = R.string.amoled_black_explanation,
                checked = themeController.settings.useAmoledBlackTheme,
                onCheckedChange = { enabled -> themeController.updateSettings { it.copy(useAmoledBlackTheme = enabled) } }
            )
        }
    }
    item(key = R.string.dynamic_colors, contentType = "action") {
        VerticalAnimatedVisibility(visible = dynamicColorsSupported) {
            DrawerSwitchRow(
                iconRes = R.drawable.ic_palette_24,
                labelRes = R.string.dynamic_colors,
                explanationRes = R.string.use_colors_derived_from_your_wallpaper,
                checked = themeController.settings.useDynamicColors,
                onCheckedChange = { enabled -> themeController.updateSettings { it.copy(useDynamicColors = enabled) } }
            )
        }
    }

    drawerHeader(R.string.settings)
    item(key = R.string.location_access, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_location_on_24,
            labelRes = R.string.location_access,
            onClick = onLocationAccessClick
        )
    }

    drawerHeader(R.string.legal)
    item(key = R.string.privacy_policy, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_policy_24,
            labelRes = R.string.privacy_policy,
            onClick = { context.openUrl(AppUrl.PRIVACY_POLICY) },
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
    item(key = R.string.license, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_copyright_24,
            labelRes = R.string.license,
            onClick = { context.openUrl(AppUrl.LICENSE) }
        )
    }

    drawerHeader(R.string.support_the_app)
    item(key = R.string.rate, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_star_rate_24,
            labelRes = R.string.rate,
            explanationRes = R.string.rate_the_app_in_the_playstore,
            onClick = context::rateApp
        )
    }
    item(key = R.string.share, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_share_24,
            labelRes = R.string.share,
            explanationRes = R.string.share_explanation,
            onClick = context::shareApp
        )
    }
    item(key = R.string.report_a_bug_request_a_feature, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_bug_report_24,
            labelRes = R.string.report_a_bug_request_a_feature,
            explanationRes = R.string.report_a_bug_explanation,
            onClick = { context.openUrl(AppUrl.CREATE_ISSUE) }
        )
    }
    item(key = R.string.support_development, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_donate_24,
            labelRes = R.string.support_development,
            explanationRes = R.string.buy_me_a_coffee_as_a_sign_of_gratitude,
            onClick = { context.openUrl(AppUrl.DONATE) }
        )
    }

    drawerHeader(R.string.more)
    item(key = R.string.other_apps, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_apps_24,
            labelRes = R.string.other_apps,
            explanationRes = R.string.check_out_my_other_apps,
            onClick = { context.openUrl(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE) }
        )
    }
    item(key = R.string.source, contentType = "action") {
        DrawerActionRow(
            iconRes = R.drawable.ic_github_24,
            labelRes = R.string.source,
            explanationRes = R.string.examine_the_app_s_source_code_on_github,
            onClick = { context.openUrl(AppUrl.GITHUB_REPOSITORY) }
        )
    }
}

private fun LazyListScope.drawerHeader(@StringRes titleRes: Int) {
    item(key = titleRes, contentType = "header") {
        Text(
            text = stringResource(titleRes),
            modifier = Modifier
                .padding(NavDrawerTokens.groupHeaderPadding)
                .semantics { heading() },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DrawerActionRow(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes explanationRes: Int? = null
) {
    DrawerRow(
        iconRes = iconRes,
        labelRes = labelRes,
        explanationRes = explanationRes,
        modifier = modifier
            .fillMaxWidth()
            .padding(NavDrawerTokens.actionPadding)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun DrawerSwitchRow(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes explanationRes: Int? = null
) {
    DrawerRow(
        iconRes = iconRes,
        labelRes = labelRes,
        explanationRes = explanationRes,
        modifier = modifier
            .fillMaxWidth()
            .padding(NavDrawerTokens.actionPadding)
            .clip(MaterialTheme.shapes.large)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
    ) {
        Switch(checked = checked, onCheckedChange = null, modifier = Modifier.minimumInteractiveComponentSize())
    }
}

@Composable
private fun DrawerRow(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    @StringRes explanationRes: Int? = null,
    trailing: BoxScopeComposable? = null
) {
    TLayout(
        modifier = modifier,
        leading = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(IconDefaults.SizeBig)
            )
        },
        central = {
            Text(
                text = stringResource(labelRes),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        },
        below = explanationRes?.let { res ->
            {
                Text(text = stringResource(res), style = MaterialTheme.typography.explanation)
            }
        },
        trailing = trailing,
        centralMargins = Margins(start = 16.dp)
    )
}

private fun Context.rateApp() {
    startActivity(
        intent = Intent(Intent.ACTION_VIEW, packagePlayStoreUrl.toUri()).setPackage("com.android.vending"),
        onActivityNotFoundException = { it.showToast(getString(R.string.you_re_not_signed_into_the_play_store)) }
    )
}

private fun Context.shareApp() {
    ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setText(getString(R.string.share_action_text, AppUrl.PLAY_STORE_ENTRY))
        .startChooser()
}
