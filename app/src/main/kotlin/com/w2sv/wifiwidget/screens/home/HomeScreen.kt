package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.wifiwidget.ui.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
internal fun HomeScreen() {
    Scaffold(topBar = { AppTopBar() }) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            Arrangement.SpaceBetween,
            Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.5f))
            Box(Modifier.weight(0.5f)) {
                PinWidgetButton()
            }
            Spacer(Modifier.weight(0.5f))
            Box(Modifier.weight(1f)){
                PropertiesConfigurationDialogInflationButton()
            }
        }
    }
}