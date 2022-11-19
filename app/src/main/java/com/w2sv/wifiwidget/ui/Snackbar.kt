package com.w2sv.wifiwidget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R

@Composable
fun AppSnackbar(snackbarData: SnackbarData) {
    Snackbar(
        modifier = Modifier
            .padding(horizontal = 50.dp),
        shape = RoundedCornerShape(30.dp),
        backgroundColor = colorResource(
            id = R.color.mischka_dark
        )
    ) {
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text(text = snackbarData.message, color = Color.White)
        }
    }
}