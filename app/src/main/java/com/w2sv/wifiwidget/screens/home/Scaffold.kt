package com.w2sv.wifiwidget.screens.home

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.AppSnackbar
import com.w2sv.wifiwidget.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.widget.utils.anyAppWidgetInUse

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetScaffold(
    context: Context = LocalContext.current,
    viewModel: HomeScreenViewModel = viewModel(),
    content: @Composable (PaddingValues) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = BottomSheetValue.Collapsed,
            animationSpec = spring(
                Spring.DampingRatioMediumBouncy,
                Spring.StiffnessMedium
            )
        )
    )

    BottomSheetScaffold(
        topBar = { TopBar() },
        scaffoldState = scaffoldState,
        snackbarHost = { snackbarHostState ->
            SnackbarHost(
                hostState = snackbarHostState,
                // lift in order to prevent overlapping with bottom sheet toggle button
                modifier = Modifier.padding(bottom = 50.dp)
            ) { snackbarData ->
                AppSnackbar(snackbarData = snackbarData)
            }
        },
        sheetContent = { BottomSheet(scaffoldState = scaffoldState) },
        sheetElevation = 0.dp,
        sheetBackgroundColor = Color.Transparent,
        sheetPeekHeight = 48.dp,
        contentColor = Color.White
    ) { paddingValues ->
        content(paddingValues)

        if (scaffoldState.bottomSheetState.isCollapsed && viewModel.syncWidgetProperties() && context.anyAppWidgetInUse()) {
            WifiWidgetProvider.refreshData(context)
            stringResource(R.string.updated_widget).let { text ->
                LaunchedEffect(key1 = text) {
                    scaffoldState.snackbarHostState.showSnackbar(text)
                }
            }
        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = colorResource(
                id = R.color.blue_chill_dark
            ),
            titleContentColor = Color.White
        )
    )
}
