package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.material3.WithLocalContentColor

object IconHeadlineTokens {
    val bottomSpacing = 8.dp
}

@Composable
fun IconHeadline(
    @DrawableRes iconRes: Int? = null,
    @StringRes stringRes: Int,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.secondary,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        WithLocalContentColor(contentColor) {
            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(26.dp)
                )
            }
            Text(
                text = stringResource(id = stringRes),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp)
            )
        }
        trailingContent?.run {
            Spacer(Modifier.weight(1f))
            invoke()
        }
    }
}
