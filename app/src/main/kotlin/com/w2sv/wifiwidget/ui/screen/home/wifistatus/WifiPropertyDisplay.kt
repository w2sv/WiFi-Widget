package com.w2sv.wifiwidget.ui.screen.home.wifistatus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.CardContainerColor
import com.w2sv.wifiwidget.ui.designsystem.SecondLevelElevatedCard
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.util.resourceIdTestTag
import com.w2sv.wifiwidget.ui.util.snackbar.rememberSnackbarController
import com.w2sv.wifiwidget.ui.util.toAnnotatedString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val horizontalPadding = 8.dp
private const val LABEL_VALUE_COLUMN_SPLIT = 0.4f

// TODO test
@Composable
fun WifiPropertyDisplay(wifiPropertyViewData: ImmutableList<WifiPropertyViewData>, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = wifiPropertyViewData.isNotEmpty(),
        label = "",
        modifier = modifier.fillMaxWidth(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        AnimatedContent(wifiPropertyViewData.firstOrNull(), label = "") {
            when (it) {
                null -> {
                    PropertyLoadingView(modifier = Modifier.fillMaxSize())
                }

                else -> {
                    PropertyList(viewDataList = wifiPropertyViewData)
                }
            }
        }
    }
}

@Composable
private fun PropertyLoadingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.retrieving_properties),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PropertyList(viewDataList: ImmutableList<WifiPropertyViewData>, modifier: Modifier = Modifier) {
    val onPropertyRowClick = rememberOnPropertyRowClick()

    SecondLevelElevatedCard(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.resourceIdTestTag("wifiPropertyColumn"),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            itemsIndexed(viewDataList) { i, viewData ->
                PropertyDisplay(
                    wifiPropertyViewData = viewData,
                    subPropertyValues = viewData.subValues.toPersistentList(),
                    onClick = onPropertyRowClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 26.dp)
                        .padding(horizontal = horizontalPadding)
                )
                if (i != viewDataList.lastIndex) {
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

private typealias OnPropertyRowClick = (WifiPropertyViewData, CharSequence, CoroutineScope) -> Unit

@Composable
private fun rememberOnPropertyRowClick(): OnPropertyRowClick {
    val clipboardManager = LocalClipboardManager.current
    val snackbarController = rememberSnackbarController()

    return remember {
        { viewData, label, scope ->
            clipboardManager.setText(AnnotatedString(viewData.value))
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
    wifiPropertyViewData: WifiPropertyViewData,
    subPropertyValues: ImmutableList<String>?,
    onClick: OnPropertyRowClick,
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val label = remember(wifiPropertyViewData) {
        wifiPropertyViewData.label.toAnnotatedString(12.sp)
    }

    Row(modifier = modifier.clickable { onClick(wifiPropertyViewData, label, scope) }) {
        Column(modifier = Modifier.fillMaxWidth(LABEL_VALUE_COLUMN_SPLIT)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column {
            Text(text = wifiPropertyViewData.value)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
                subPropertyValues?.forEach {
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
