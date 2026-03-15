package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.core.extensions.thenIfNotNull
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.ShakeController
import com.w2sv.wifiwidget.ui.util.offsetClip
import com.w2sv.wifiwidget.ui.util.shake

@Composable
fun ConfigRow(
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    shakeController: ShakeController? = null,
    @StringRes explanationRes: Int? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    endContent: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .thenIfNotNull(shakeController) { shake(it) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            leadingIcon?.invoke()
            Text(
                text = stringResource(id = labelRes),
                modifier = Modifier.weight(1.0f),
                fontSize = fontSize,
                color = labelColor
            )
            endContent()
        }
        explanationRes?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.onSurfaceVariantLowAlpha,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(end = 32.dp)
                    .offsetClip(dy = (-10).dp) // Shift explanation up a bit to increase its visual coherence with the main row
            )
        }
    }
}
