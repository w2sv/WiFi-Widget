package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTopBar
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
internal fun HomeScreen() {
    val coroutineScope = rememberCoroutineScope()

    NavigationDrawer { drawerState ->
        Scaffold(topBar = {
            WifiWidgetTopBar {
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
                    PropertySelectionDialogInflationButton()
                }
                CopyrightText(modifier = Modifier.padding(bottom = dimensionResource(R.dimen.margin_minimal)))
            }
        }
    }
}

@Composable
private fun CopyrightText(modifier: Modifier) {
    JostText(
        text = "© 2022 - ${Calendar.getInstance().get(Calendar.YEAR)} | W2SV",
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 16.sp,
        modifier = modifier
    )
}