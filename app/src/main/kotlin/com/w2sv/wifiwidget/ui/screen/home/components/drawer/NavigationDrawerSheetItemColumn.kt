package com.w2sv.wifiwidget.ui.screen.home.components.drawer

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.core.extensions.thenIfNotNull
import com.w2sv.wifiwidget.ui.designsystem.RightAligned
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha

@Composable
fun NavigationDrawerElement(element: DrawerElement, actionScope: DrawerActionScope) {
    when (element) {
        is DrawerElement.Action -> {
            AnimatedVisibility(visible = element.isVisible(actionScope)) {
                Action(
                    action = element,
                    scope = actionScope,
                    modifier = element.modifier
                )
            }
        }

        is DrawerElement.Header -> {
            GroupHeader(
                titleRes = element.titleRes,
                modifier = element.modifier
            )
        }
    }
}

@Composable
private fun GroupHeader(@StringRes titleRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = titleRes),
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Action(
    action: DrawerElement.Action,
    scope: DrawerActionScope,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .thenIfNotNull(action.type.clickableOrNull) { clickable ->
                clickable(onClick = { clickable.onClick(scope) })
            }
    ) {
        MainItemRow(action = action, scope = scope, modifier = Modifier.fillMaxWidth())
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
private fun MainItemRow(
    action: DrawerElement.Action,
    scope: DrawerActionScope,
    modifier: Modifier = Modifier
) {
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
                type.content(scope)
            }

            is DrawerElement.Action.Switch -> {
                RightAligned {
                    Switch(checked = type.checked(scope), onCheckedChange = { type.onCheckedChange(scope, it) })
                }
            }

            else -> Unit
        }
    }
}
