package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.ui.AppTopBar
import com.w2sv.wifiwidget.ui.JostText
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
internal fun HomeScreen() {
    val coroutineScope = rememberCoroutineScope()

    NavigationDrawer { drawerState ->
        Scaffold(topBar = {
            AppTopBar {
                coroutineScope.launch {
                    drawerState.open()
                }
            }
        }) { paddingValues ->
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
                Box(Modifier.weight(1f)) {
                    PropertiesConfigurationDialogInflationButton()
                }
                CopyrightText(modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

@Composable
fun CopyrightText(modifier: Modifier) {
    JostText(
        text = "© 2022 - ${Calendar.getInstance().get(Calendar.YEAR)} | W2SV",
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 16.sp,
        modifier = modifier
    )
}