package com.w2sv.wifiwidget.ui.screen.home.components.navdrawer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.w2sv.androidutils.content.openUrl
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.AppUrl
import com.w2sv.wifiwidget.ui.LocalAppVersionName
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.util.PreviewOf
import java.util.Calendar

@Composable
fun NavDrawer(
    state: DrawerState,
    themeController: ThemeController,
    onLocationAccessClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            NavDrawerSheet(
                drawerState = state,
                themeController = themeController,
                onLocationAccessClick = onLocationAccessClick
            )
        },
        drawerState = state,
        content = content
    )
}

@Composable
private fun NavDrawerSheet(
    drawerState: DrawerState,
    themeController: ThemeController,
    onLocationAccessClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = modifier,
        windowInsets = WindowInsets()
    ) {
        val horizontalPadding = 20.dp

        LazyColumn(
            contentPadding = WindowInsets.systemBarsIgnoringVisibility.asPaddingValues() +
                PaddingValues(top = 16.dp, bottom = 8.dp, start = horizontalPadding, end = horizontalPadding)
        ) {
            item(key = "app-header", contentType = "app-header") {
                Header(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }
            item(key = "divider", contentType = "divider") {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
            navDrawerItems(context, themeController, onLocationAccessClick)
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Prev() {
    PreviewOf {
        NavDrawerSheet(
            drawerState = rememberDrawerState(DrawerValue.Open),
            themeController = ThemeController.Default,
            onLocationAccessClick = {}
        )
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExpressiveAppLogo(
            Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )

        Column {
            Spacer(Modifier.weight(0.4f))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(id = R.string.version).format(LocalAppVersionName.current),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .clickable(
                        onClick = { context.openUrl(AppUrl.RELEASE_OVERVIEW) },
                        onClickLabel = stringResource(R.string.open_release_overview_cd)
                    )
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "© 2022 - ${Calendar.getInstance().get(Calendar.YEAR)} · W2SV",
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { context.openUrl(AppUrl.GITHUB_PROFILE) },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private const val MORPH_DURATION_MILLIS = 2_500

@Composable
private fun ExpressiveAppLogo(modifier: Modifier = Modifier) {
    val morphs = remember {
        listOf(
            MaterialShapes.Cookie12Sided,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Cookie6Sided,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Cookie6Sided,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Cookie12Sided
        ).zipWithNext { start, end -> Morph(start, end) }
    }

    val phase by rememberInfiniteTransition(label = "Logo morph")
        .animateFloat(
            initialValue = 0f,
            targetValue = morphs.size.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = morphs.size * MORPH_DURATION_MILLIS,
                    easing = LinearEasing
                )
            ),
            label = "Morph phase"
        )

    val index = phase.toInt().coerceAtMost(morphs.lastIndex)
    val progress = FastOutSlowInEasing.transform(phase - index)

    val shape = GenericShape { size, _ ->
        addPath(morphs[index].toPath(progress))
        transform(Matrix().apply { scale(size.width, size.height) })
        translate(size.center - getBounds().center)
    }

    Box(
        modifier = modifier
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 8.dp,
                    spread = 0.dp,
                    offset = DpOffset(3.dp, 6.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_foreground),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.3f)
        )
    }
}
