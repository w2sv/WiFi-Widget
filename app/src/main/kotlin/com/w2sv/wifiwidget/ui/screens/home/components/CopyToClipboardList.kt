package com.w2sv.wifiwidget.ui.screens.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.data.model.WifiProperty

@Composable
fun WifiPropertiesColumn(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(WifiProperty.values()){
            Row {
                
            }
        }
    }
}