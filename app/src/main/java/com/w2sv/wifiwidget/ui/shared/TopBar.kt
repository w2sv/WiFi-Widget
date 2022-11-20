package com.w2sv.wifiwidget.ui.shared

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.wifiwidget.R

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar() {
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