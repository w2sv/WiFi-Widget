package com.w2sv.widget.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.provideContent
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.GlanceStateDefinition
import com.w2sv.core.common.R
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.ui.util.stringResource

class WifiWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*>?
        get() = super.stateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }
}

@Composable
private fun Content() {
    val context = LocalContext.current
    Scaffold(titleBar = { TitleBar() }) {

    }
}

@Composable
private fun TitleBar(bottomBarElement: WidgetBottomBarElement) {
    TitleBar(
        startIcon = ImageProvider(R.drawable.logo_foreground),
        title = stringResource(R.string.app_name),
        actions = {
            buildList {

            }
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_refresh_24),
                contentDescription = "",
                onClick = actionSendBroadcast(WifiWidgetProvider.getRefreshDataIntent(context))
            )
        }
    )
}

@Composable
private fun TitleBarActionButton(button: TitleBarActionButton) {
    CircleIconButton(
        imageProvider = ImageProvider(button.icon),
        contentDescription = stringResource(button.contentDescription),
        onClick = button.action
    )
}

private data class TitleBarActionButton(
    @param:DrawableRes val icon: Int,
    @param:StringRes val contentDescription: Int,
    val action: Action
)

private fun WidgetBottomBarElement.actionButtons(): List<TitleBarActionButton> =
    buildList {
        if (refreshButton) {
            add()
        }
    }

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 200)
@Preview(widthDp = 300, heightDp = 200)
@Composable
private fun Prev() {
    Content()
}
