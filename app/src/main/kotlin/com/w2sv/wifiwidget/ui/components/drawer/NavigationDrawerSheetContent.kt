package com.w2sv.wifiwidget.ui.components.drawer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText

@Composable
internal fun NavigationDrawerSheetContent(
    closeDrawer: () -> Unit,
    appearanceSection: @Composable (Modifier) -> Unit
) {
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.Start) {
        remember {
            listOf(
                SheetView.SubHeader(R.string.appearance),
                SheetView.Custom {
                    appearanceSection(
                        Modifier
                            .padding(top = 12.dp, bottom = 18.dp)
                    )
                },
                SheetView.SubHeader(R.string.support),
                SheetView.Item(
                    iconRes = R.drawable.ic_share_24,
                    labelRes = R.string.share,
                    onClick = {
                        ShareCompat.IntentBuilder(it)
                            .setType("text/plain")
                            .setText(context.getString(R.string.share_action_text))
                            .startChooser()
                    }
                ),
                SheetView.Item(
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
                SheetView.SubHeader(R.string.legal),
                SheetView.Item(
                    iconRes = R.drawable.ic_policy_24,
                    labelRes = R.string.privacy_policy,
                    onClick = {
                        it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget/blob/main/PRIVACY-POLICY.md")
                    }
                ),
                SheetView.Item(
                    iconRes = R.drawable.ic_copyright_24,
                    labelRes = R.string.license,
                    onClick = {
                        it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget/blob/main/LICENSE")
                    }
                ),
                SheetView.SubHeader(R.string.about),
                SheetView.Item(
                    iconRes = R.drawable.ic_mask_24,
                    labelRes = R.string.creator,
                    onClick = {
                        it.openUrlWithActivityNotFoundHandling("https://play.google.com/store/apps/dev?id=6884111703871536890")
                    }
                ),
                SheetView.Item(
                    iconRes = R.drawable.ic_github_24,
                    labelRes = R.string.source,
                    onClick = {
                        it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget")
                    }
                )
            )
        }
            .forEach {
                when (it) {
                    is SheetView.Item -> {
                        DrawerSheetItem(item = it, closeDrawer = closeDrawer)
                    }

                    is SheetView.SubHeader -> {
                        DrawerSheetSubHeader(titleRes = it.titleRes)
                    }

                    is SheetView.Custom -> {
                        it.content()
                    }
                }
            }
    }
}

private sealed interface SheetView {
    @Stable
    data class Item(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int,
        val onClick: (Context) -> Unit
    ) : SheetView

    @Stable
    data class SubHeader(
        @StringRes val titleRes: Int
    ) : SheetView

    data class Custom(
        val content: @Composable () -> Unit
    ) : SheetView
}


@Composable
private fun ColumnScope.DrawerSheetSubHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier
) {
    JostText(
        text = stringResource(id = titleRes),
        modifier = modifier
            .padding(vertical = 4.dp)
            .align(Alignment.CenterHorizontally),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
}

@Composable
private fun DrawerSheetItem(
    item: SheetView.Item,
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
