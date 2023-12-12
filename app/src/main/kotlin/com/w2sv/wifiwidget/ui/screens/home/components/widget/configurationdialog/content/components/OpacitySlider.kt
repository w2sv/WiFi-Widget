package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.w2sv.wifiwidget.R
import kotlin.math.roundToInt

@Composable
fun OpacitySliderWithLabel(
    getOpacity: () -> Float,
    onOpacityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label by remember {
        derivedStateOf { "${(getOpacity() * 100).roundToInt()}%" }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        val context = LocalContext.current
        Slider(
            value = getOpacity(),
            onValueChange = onOpacityChanged,
            modifier = Modifier
                .semantics {
                    contentDescription = context.getString(
                        R.string.opacity_slider_cd,
                    )
                },
            steps = 9,
        )
    }
}
