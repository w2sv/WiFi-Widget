package com.w2sv.wifiwidget.ui.screen.home.components.drawer

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.w2sv.common.AppUrl
import com.w2sv.composed.core.extensions.thenIfNotNull
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.RightAligned
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.sharedstate.theme.ThemeController
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.useDarkTheme

@Composable
internal fun NavigationDrawerSheetItemColumn(
    themeController: ThemeController,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val useDarkTheme = useDarkTheme(themeController.theme())

    Column(modifier = modifier) {
        remember {
            listOf(
                DrawerElement.Header(
                    titleRes = R.string.appearance,
                    modifier = Modifier
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_nightlight_24,
                    labelRes = R.string.theme,
                    type = DrawerElement.Action.Custom {
                        ThemeSelectionRow(
                            selected = themeController.theme(),
                            onSelected = themeController.setTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        )
                    }
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_contrast_24,
                    labelRes = R.string.amoled_black,
                    explanationRes = R.string.amoled_black_explanation,
                    isVisible = { useDarkTheme },
                    type = DrawerElement.Action.Switch(
                        checked = themeController.useAmoledBlackTheme,
                        onCheckedChange = themeController.setUseAmoledBlackTheme
                    )
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_palette_24,
                    labelRes = R.string.dynamic_colors,
                    explanationRes = R.string.use_colors_derived_from_your_wallpaper,
                    isVisible = { dynamicColorsSupported },
                    type = DrawerElement.Action.Switch(
                        checked = themeController.useDynamicColors,
                        onCheckedChange = themeController.setUseDynamicColors
                    )
                ),
                DrawerElement.Header(
                    titleRes = R.string.legal
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_policy_24,
                    labelRes = R.string.privacy_policy,
                    type = DrawerElement.Action.Clickable {
                        context.openUrl(AppUrl.PRIVACY_POLICY)
                    }
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_copyright_24,
                    labelRes = R.string.license,
                    type = DrawerElement.Action.Clickable {
                        context.openUrl(AppUrl.LICENSE)
                    }
                ),
                DrawerElement.Header(
                    titleRes = R.string.support_the_app
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_star_rate_24,
                    labelRes = R.string.rate,
                    explanationRes = R.string.rate_the_app_in_the_playstore,
                    type = DrawerElement.Action.Clickable {
                        context.startActivity(
                            intent = Intent(
                                Intent.ACTION_VIEW,
                                context.packagePlayStoreUrl.toUri()
                            )
                                .setPackage("com.android.vending"),
                            onActivityNotFoundException = {
                                it.showToast(context.getString(R.string.you_re_not_signed_into_the_play_store))
                            }
                        )
                    }
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_share_24,
                    labelRes = R.string.share,
                    explanationRes = R.string.share_explanation,
                    type = DrawerElement.Action.Clickable {
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(context.getString(R.string.share_action_text, AppUrl.PLAY_STORE_ENTRY))
                            .startChooser()
                    }
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_bug_report_24,
                    labelRes = R.string.report_a_bug_request_a_feature,
                    explanationRes = R.string.report_a_bug_explanation,
                    type = DrawerElement.Action.Clickable {
                        context.openUrl(AppUrl.CREATE_ISSUE)
                    }
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_donate_24,
                    labelRes = R.string.support_development,
                    explanationRes = R.string.buy_me_a_coffee_as_a_sign_of_gratitude,
                    type = DrawerElement.Action.Clickable {
                        context.openUrl(AppUrl.DONATE)
                    }
                ),
                DrawerElement.Header(
                    titleRes = R.string.more
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_developer_24,
                    labelRes = R.string.developer,
                    explanationRes = R.string.check_out_my_other_apps,
                    type = DrawerElement.Action.Clickable {
                        context.openUrl(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE)
                    }
                ),
                DrawerElement.Action(
                    iconRes = R.drawable.ic_github_24,
                    labelRes = R.string.source,
                    explanationRes = R.string.examine_the_app_s_source_code_on_github,
                    type = DrawerElement.Action.Clickable {
                        context.openUrl(AppUrl.GITHUB_REPOSITORY)
                    }
                )
            )
        }
            .forEach { element ->
                when (element) {
                    is DrawerElement.Action -> {
                        AnimatedVisibility(visible = element.isVisible()) {
                            Item(
                                action = element,
                                modifier = element.modifier
                            )
                        }
                    }

                    is DrawerElement.Header -> {
                        SubHeader(
                            titleRes = element.titleRes,
                            modifier = element.modifier
                        )
                    }
                }
            }
    }
}

@Immutable
private sealed interface DrawerElement {
    val modifier: Modifier

    @Immutable
    data class Header(
        @StringRes val titleRes: Int,
        override val modifier: Modifier = Modifier
            .padding(top = 20.dp, bottom = 4.dp)
    ) : DrawerElement

    @Immutable
    data class Action(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int,
        @StringRes val explanationRes: Int? = null,
        val isVisible: () -> Boolean = { true },
        override val modifier: Modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        val type: Type
    ) : DrawerElement {

        @Immutable
        sealed interface Type {

            val clickableOrNull
                get() = this as? Clickable
        }

        @Immutable
        data class Clickable(val onClick: () -> Unit) : Type

        @Immutable
        data class Switch(val checked: () -> Boolean, val onCheckedChange: (Boolean) -> Unit) : Type

        @Immutable
        data class Custom(val content: @Composable RowScope.() -> Unit) : Type
    }
}

@Composable
private fun SubHeader(@StringRes titleRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = titleRes),
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Item(action: DrawerElement.Action, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .thenIfNotNull(action.type.clickableOrNull) {
                clickable {
                    it.onClick()
                }
            }
    ) {
        MainItemRow(action = action, modifier = Modifier.fillMaxWidth())
        action.explanationRes?.let {
            Text(
                text = stringResource(id = it),
                color = MaterialTheme.colorScheme.onSurfaceVariantLowAlpha,
                modifier = Modifier.padding(start = iconSize + labelStartPadding),
                fontSize = 14.sp
            )
        }
    }
}

private val iconSize = 28.dp
private val labelStartPadding = 16.dp

@Composable
private fun MainItemRow(action: DrawerElement.Action, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(size = iconSize),
            painter = painterResource(id = action.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(id = action.labelRes),
            modifier = Modifier.padding(start = labelStartPadding),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )

        when (val type = action.type) {
            is DrawerElement.Action.Custom -> {
                type.content(this)
            }

            is DrawerElement.Action.Switch -> {
                RightAligned {
                    Switch(checked = type.checked(), onCheckedChange = type.onCheckedChange)
                }
            }

            else -> Unit
        }
    }
}
