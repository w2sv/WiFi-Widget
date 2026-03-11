package com.w2sv.wifiwidget.ui.screen.home.components.drawer

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.w2sv.androidutils.content.openUrl
import com.w2sv.androidutils.content.packagePlayStoreUrl
import com.w2sv.androidutils.content.startActivity
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.AppUrl
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.ThemeSelectionRow

fun navigationDrawerElements(): List<DrawerElement> =
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
                checked = { themeController.useAmoledBlackTheme() },
                onCheckedChange = { themeController.setUseAmoledBlackTheme(it) }
            )
        ),
        DrawerElement.Action(
            iconRes = R.drawable.ic_palette_24,
            labelRes = R.string.dynamic_colors,
            explanationRes = R.string.use_colors_derived_from_your_wallpaper,
            isVisible = { dynamicColorsSupported },
            type = DrawerElement.Action.Switch(
                checked = { themeController.useDynamicColors() },
                onCheckedChange = { themeController.setUseDynamicColors(it) }
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
        DrawerElement.Header(titleRes = R.string.support_the_app),
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

@Immutable
sealed interface DrawerElement {
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
        val isVisible: DrawerActionScope.() -> Boolean = { true },
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
        data class Clickable(val onClick: DrawerActionScope.() -> Unit) : Type

        @Immutable
        data class Switch(val checked: DrawerActionScope.() -> Boolean, val onCheckedChange: DrawerActionScope.(Boolean) -> Unit) : Type

        @Immutable
        data class Custom(val content: @Composable DrawerActionScope.() -> Unit) : Type
    }
}
