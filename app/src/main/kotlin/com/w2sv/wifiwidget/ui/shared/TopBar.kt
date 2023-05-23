package com.w2sv.wifiwidget.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.wifiwidget.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiWidgetTopBar(modifier: Modifier = Modifier, onNavigationIconClick: () -> Unit) {
    TopAppBar(
        title = {
            JostText(
                stringResource(id = R.string.app_name),
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.margin_minimal))
            )
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.open_navigation_drawer),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.size_icon)),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    WifiWidgetTheme {
        WifiWidgetTopBar {}
    }
}