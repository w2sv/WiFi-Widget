package com.w2sv.wifiwidget.ui.components.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.R

@Composable
internal fun NavigationDrawerSheet(content: @Composable () -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(vertical = 32.dp, horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Header()
            Divider(modifier = Modifier.padding(top = 18.dp, bottom = 16.dp))
            content()
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Image(
            painterResource(id = R.drawable.logo_foreground),
            null,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.height(22.dp))
        VersionText()
    }
}

@Composable
private fun VersionText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.version).format(BuildConfig.VERSION_NAME),
        modifier = modifier,
    )
}
