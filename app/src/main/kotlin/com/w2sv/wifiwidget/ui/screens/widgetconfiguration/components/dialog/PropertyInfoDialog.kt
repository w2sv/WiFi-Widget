package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.w2sv.composed.rememberStyledTextResource
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.DialogButton
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.InfoDialogData

@Composable
fun PropertyInfoDialog(
    data: InfoDialogData,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    InfoDialog(
        modifier = modifier,
        title = stringResource(data.titleRes),
        text = rememberStyledTextResource(data.descriptionRes),
        learnMoreButton = data.learnMoreUrl?.let { learnMoreUrl ->
            {
                Text(
                    text = buildAnnotatedString {
                        withLink(LinkAnnotation.Url(learnMoreUrl)) {
                            append(stringResource(R.string.learn_more))
                        }
                    },
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                )
            }
        },
        onDismissRequest = onDismissRequest
    )
}

@Composable
private fun InfoDialog(
    title: String,
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    learnMoreButton: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit
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
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                Modifier
                    .sizeIn(maxHeight = 360.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Text(text = text)
                learnMoreButton?.invoke()
            }
        }
    )
}
