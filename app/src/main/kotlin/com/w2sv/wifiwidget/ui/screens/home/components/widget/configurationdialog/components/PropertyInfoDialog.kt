package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoIcon
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.InfoDialogData

@Composable
fun PropertyInfoDialog(
    data: InfoDialogData,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context: Context = LocalContext.current
    InfoDialog(
        modifier = modifier,
        title = data.title,
        text = data.description,
        learnMoreButton = data.learnMoreUrl?.let {
            {
                ElevatedButton(
                    onClick = {
                        context.openUrlWithActivityNotFoundHandling(it)
                    },
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.learn_more),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun InfoDialog(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    learnMoreButton: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.close))
            }
        },
        icon = { InfoIcon() },
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                Modifier
                    .sizeIn(maxHeight = 360.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                Arrangement.Center,
                Alignment.CenterHorizontally,
            ) {
                Text(text = text, textAlign = TextAlign.Center)
                learnMoreButton?.invoke()
            }
        },
    )
}