package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class IconHeaderProperties(
    @DrawableRes val iconRes: Int,
    @StringRes val stringRes: Int
)

@Composable
fun IconHeader(
    properties: IconHeaderProperties,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Box(modifier = Modifier.weight(0.3f)) {
            Icon(
                painter = painterResource(id = properties.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp),
            )
        }
        Box(Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(id = properties.stringRes),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}
