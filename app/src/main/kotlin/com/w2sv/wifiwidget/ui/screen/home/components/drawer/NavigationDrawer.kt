package com.w2sv.wifiwidget.ui.screen.home.components.drawer

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.w2sv.composed.core.extensions.thenIfNotNull
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.ui.sharedstate.theme.ThemeController
import com.w2sv.wifiwidget.ui.sharedstate.theme.previewThemeController
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.add

@Composable
fun NavigationDrawer(
    state: DrawerState,
    themeController: ThemeController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            NavigationDrawerSheet(
                drawerState = state,
                themeController = themeController
            )
        },
        drawerState = state,
        content = content
    )
}

@Composable
private fun NavigationDrawerSheet(
    drawerState: DrawerState,
    themeController: ThemeController,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = modifier,
        windowInsets = WindowInsets()
    ) {
        val elements = remember { navigationDrawerElements() }
        val actionScope = rememberDrawerActionScope(themeController)

        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentPadding = WindowInsets.systemBarsIgnoringVisibility.asPaddingValues().add(top = 16.dp)
        ) {
            item {
                Header(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
            items(elements, key = { it.hashCode() }, contentType = { it::class }) { element ->
                NavigationDrawerElement(element, actionScope)
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Prev() {
    PreviewOf {
        NavigationDrawerSheet(
            drawerState = rememberDrawerState(DrawerValue.Open),
            themeController = previewThemeController()
        )
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painterResource(id = R.drawable.logo_foreground),
            null,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(text = stringResource(id = R.string.version).format(BuildConfig.VERSION_NAME))
    }
}

@Composable
private fun NavigationDrawerElement(element: DrawerElement, actionScope: DrawerActionScope) {
    when (element) {
        is DrawerElement.Action -> {
            AnimatedVisibility(visible = element.isVisible(actionScope)) {
                Action(
                    action = element,
                    scope = actionScope,
                    modifier = element.configureModifier(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                )
            }
        }

        is DrawerElement.Header -> {
            GroupHeader(
                titleRes = element.titleRes,
                modifier = Modifier.padding(top = 20.dp)
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
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .thenIfNotNull(action.type.asClickableOrNull) { clickable ->
                clickable(onClick = { clickable.onClick(scope) })
            }
    ) {
        val (iconRef, labelRef, explanationRef, actionRef) = createRefs()
        val hasAction = action.type.asClickableOrNull == null

        Icon(
            modifier = Modifier
                .size(size = 28.dp)
                .constrainAs(iconRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                },
            painter = painterResource(id = action.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(id = action.labelRes),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.constrainAs(labelRef) {
                start.linkTo(iconRef.end, margin = 16.dp)
                end.linkTo(if (hasAction) actionRef.start else parent.end)
                width = Dimension.fillToConstraints
                centerVerticallyTo(iconRef)
            }
        )

        action.explanationRes?.let {
            Text(
                text = stringResource(id = it),
                color = MaterialTheme.colorScheme.onSurfaceVariantLowAlpha,
                fontSize = 14.sp,
                modifier = Modifier.constrainAs(explanationRef) {
                    top.linkTo(labelRef.bottom, margin = 2.dp)
                    centerHorizontallyTo(labelRef)
                    width = Dimension.fillToConstraints
                }
            )
        }

        val actionModifier = Modifier.constrainAs(actionRef) {
            start.linkTo(labelRef.end)
            end.linkTo(parent.end)
            centerVerticallyTo(labelRef)
        }

        when (val type = action.type) {
            is DrawerElement.Action.Custom -> {
                type.content(scope, actionModifier)
            }

            is DrawerElement.Action.Switch -> {
                Switch(
                    checked = type.checked(scope),
                    onCheckedChange = { type.onCheckedChange(scope, it) },
                    modifier = actionModifier
                )
            }

            else -> Unit
        }
    }
}
