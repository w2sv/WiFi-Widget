package com.w2sv.wifiwidget.screens.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetScaffold(
    content: @Composable (PaddingValues) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            BottomSheetValue.Collapsed,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
        )
    )

    BottomSheetScaffold(
        topBar = { TopBar() },
        scaffoldState = scaffoldState,
        sheetContent = { BottomSheet(sheetState = scaffoldState.bottomSheetState) },
        sheetElevation = 0.dp,
        sheetBackgroundColor = Color.Transparent,
        sheetPeekHeight = 48.dp,
        contentColor = Color.White
    ) {
        content(it)
    }
}

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