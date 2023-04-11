package com.w2sv.wifiwidget.ui.shared

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.w2sv.wifiwidget.R

@Composable
fun InfoIconButton(onClick: () -> Unit, contentDescription: String) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = contentDescription,
            modifier = Modifier.size(
                dimensionResource(id = R.dimen.size_icon)
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}