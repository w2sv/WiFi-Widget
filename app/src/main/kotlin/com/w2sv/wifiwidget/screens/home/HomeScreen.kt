package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.AppTopBar
import com.w2sv.wifiwidget.widget.WifiWidgetProvider

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
internal fun HomeScreen() {
    Scaffold(topBar = { AppTopBar() }) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxHeight()
                .fillMaxWidth(),
            Arrangement.SpaceEvenly,
            Alignment.CenterHorizontally
        ) {
            PinWidgetButton()
            PropertiesConfigurationDialogInflationButton()
        }
    }
}

@Preview
@Composable
fun PropertiesConfigurationDialogInflationButton() {
    val viewModel: HomeActivity.ViewModel = viewModel()
    val context = LocalContext.current
    var triggerOnClickListener by rememberSaveable {
        mutableStateOf(false)
    }

    if (triggerOnClickListener)
        PropertiesConfigurationDialog {
            triggerOnClickListener = false
            if (viewModel.syncWidgetProperties()){
                WifiWidgetProvider.refreshData(context)
                context.showToast("Updated widget properties")
            }
        }

    IconButton(onClick = { triggerOnClickListener = true }) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Inflate widget properties selection dialog",
            modifier = Modifier.size(32.dp),
            tint = colorResource(id = R.color.blue_chill)
        )
    }
}