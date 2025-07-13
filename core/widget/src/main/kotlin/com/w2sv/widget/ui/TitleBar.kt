package com.w2sv.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import com.w2sv.core.common.R
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.widget.ui.util.stringResource
import kotlin.collections.forEach

@Composable
internal fun TitleBar(elements: List<WidgetBottomBarElement>) {
    TitleBar(
        startIcon = ImageProvider(R.drawable.logo_foreground),
        title = stringResource(R.string.app_name),
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.padding(end = 8.dp)) {
                elements.forEach {
                    TitleBarActionButton(
                        button = it,
                        modifier = GlanceModifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun TitleBarActionButton(button: WidgetBottomBarElement, modifier: GlanceModifier) {
    CircleIconButton(
        modifier = modifier,
        imageProvider = ImageProvider(button.icon),
        contentDescription = stringResource(button.widgetContentDescription),
        onClick = object : Action {}  // TODO
    )
}
