package com.w2sv.wifiwidget.ui.screen.home.components.wifistatus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.core.common.R
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.CardContainerColor
import com.w2sv.wifiwidget.ui.designsystem.SecondLevelElevatedCard
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.util.resourceIdTestTag
import com.w2sv.wifiwidget.ui.util.snackbar.rememberSnackbarController
import com.w2sv.wifiwidget.ui.util.toAnnotatedString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val horizontalPadding = 8.dp

@Composable
fun WifiPropertyList(viewData: ImmutableList<WifiPropertyViewData>, modifier: Modifier = Modifier) {
    val onPropertyRowClick = rememberOnPropertyRowClick()
    val lastIndex = viewData.lastIndex

    SecondLevelElevatedCard(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.resourceIdTestTag("wifiPropertyColumn"),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            itemsIndexed(viewData) { i, viewData ->
                val scope = rememberCoroutineScope()
                val renderedLabel = remember(viewData) {
                    viewData.label.toAnnotatedString(12.sp)
                }
                PropertyDisplay(
                    viewData = viewData,
                    label = renderedLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 26.dp)
                        .clickable(enabled = !viewData.isError) { onPropertyRowClick(renderedLabel, viewData.value, scope) }
                        .padding(horizontal = horizontalPadding)
                )
                if (i != lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = CardContainerColor
                    )
                }
            }
        }
    }
}

private typealias OnPropertyRowClick = (CharSequence, String, CoroutineScope) -> Unit

@Composable
private fun rememberOnPropertyRowClick(): OnPropertyRowClick {
    val clipboardManager = LocalClipboardManager.current
    val snackbarController = rememberSnackbarController()

    return remember {
        { label, value, scope ->
            clipboardManager.setText(AnnotatedString(value))
            scope.launch {
                snackbarController.showReplacing {
                    AppSnackbarVisuals(
                        msg = buildAnnotatedString {
                            append("${getString(R.string.copied)} ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(label)
                            }
                            append(" ${getString(R.string.to_clipboard)}.")
                        },
                        kind = SnackbarKind.Success
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyDisplay(
    viewData: WifiPropertyViewData,
    label: AnnotatedString,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth(0.4f)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column {
            Text(text = viewData.value, color = Color.Unspecified.takeUnless { viewData.isError } ?: MaterialTheme.colorScheme.error)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
                viewData.subValues.forEach {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}
