package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.HomeScreenCardBackground
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import com.w2sv.wifiwidget.ui.designsystem.showSnackbarAndDismissCurrentIfApplicable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import slimber.log.i

@Composable
fun WifiPropertyDisplay(
    propertiesViewData: Flow<WidgetWifiProperty.ViewData>,
    modifier: Modifier = Modifier,
) {
    val viewDataList = rememberRefreshingViewDataList(viewDataFlow = propertiesViewData)

    AnimatedVisibility(
        visible = viewDataList.isNotEmpty(),
        label = "",
        modifier = modifier.fillMaxWidth(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        AnimatedContent(viewDataList.firstOrNull(), label = "") {
            when (it) {
                is PropertyListElement.Property -> {
                    PropertyList(viewDataList = viewDataList.toImmutableList())
                }

                else -> {
                    PropertyLoadingView(modifier = Modifier.fillMaxSize())
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

@Immutable
private sealed interface PropertyListElement {
    @Immutable
    data class Property(val property: WidgetWifiProperty.ViewData) : PropertyListElement

    @Immutable
    data object LoadingAnimation : PropertyListElement
}

@Composable
private fun rememberRefreshingViewDataList(viewDataFlow: Flow<WidgetWifiProperty.ViewData>): SnapshotStateList<PropertyListElement> {
    val viewDataList = remember {
        mutableStateListOf<PropertyListElement>(PropertyListElement.LoadingAnimation)
    }

    LaunchedEffect(viewDataFlow) {
        i { "Collecting viewDataList" }

        var lastCollectedIndex = Int.MAX_VALUE

        viewDataFlow
            .onEmpty { viewDataList.clear() }
            .onCompletion { cause ->
                if (viewDataList.lastOrNull() == PropertyListElement.LoadingAnimation) {
                    viewDataList.removeLast()
                }
                if (cause == null && lastCollectedIndex < viewDataList.lastIndex) {
                    i { "Removing range ${lastCollectedIndex + 1} - ${viewDataList.size}" }
                    viewDataList.removeRange(lastCollectedIndex + 1, viewDataList.size)
                }
            }
            .collectIndexed { index, value ->
                val wrapped = PropertyListElement.Property(value)
                try {
                    viewDataList[index] = wrapped
                } catch (_: IndexOutOfBoundsException) {
                    viewDataList.add(wrapped)
                }

                lastCollectedIndex = index

                if (viewDataList.lastIndex < index + 1) {
                    viewDataList.add(PropertyListElement.LoadingAnimation)
                }
            }
    }

    return viewDataList
}

@Composable
private fun PropertyList(
    viewDataList: ImmutableList<PropertyListElement>,
    modifier: Modifier = Modifier
) {
    val onPropertyRowClick = rememberOnPropertyRowClick()

    LazyColumn(
        modifier = modifier
            .nestedContentBackground(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Header(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp,
                        bottom = 2.dp,
                        start = horizontalPadding,
                        end = horizontalPadding
                    )
            )
        }
        itemsIndexed(viewDataList) { i, viewData ->
            when (viewData) {
                is PropertyListElement.Property -> {
                    PropertyDisplayRow(
                        viewData = viewData.property,
                        onClick = onPropertyRowClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 26.dp)
                            .padding(horizontal = horizontalPadding)
                    )
                    (viewData.property as? WidgetWifiProperty.ViewData.IPProperty)?.prefixLengthText?.let {
                        PrefixLengthDisplayRow(
                            prefixLengthText = it,
                            modifier = Modifier
                                .padding(horizontal = horizontalPadding)
                                .fillMaxWidth()
                        )
                    }
                    if (i != viewDataList.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            color = HomeScreenCardBackground
                        )
                    }
                }

                is PropertyListElement.LoadingAnimation -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(id = R.string.properties),
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.click_to_copy_to_clipboard),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private typealias OnPropertyRowClick = (WidgetWifiProperty.ViewData, CharSequence) -> Unit

@Composable
private fun rememberOnPropertyRowClick(): OnPropertyRowClick {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
    val scope: CoroutineScope = rememberCoroutineScope()
    val context: Context = LocalContext.current

    return remember {
        { viewData, label ->
            clipboardManager.setText(AnnotatedString(viewData.value))
            scope.launch {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = buildAnnotatedString {
                            append("${context.getString(R.string.copied)} ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(label)
                            }
                            append(" ${context.getString(R.string.to_clipboard)}.")
                        },
                        kind = SnackbarKind.Success,
                    )
                )
            }
        }
    }
}

private val horizontalPadding = 12.dp

@Composable
private fun PropertyDisplayRow(
    viewData: WidgetWifiProperty.ViewData,
    onClick: OnPropertyRowClick,
    modifier: Modifier = Modifier
) {
    val context: Context = LocalContext.current

    val label = remember(viewData) {
        buildAnnotatedString {
            if (viewData is WidgetWifiProperty.ViewData.IPProperty) {
                append(context.getString(R.string.ip))
                withStyle(
                    SpanStyle(
                        baselineShift = BaselineShift.Subscript,
                        fontSize = 12.sp,
                    )
                ) {
                    append(viewData.label)
                }
            } else {
                append(viewData.label)
            }
        }
    }

    Row(
        modifier = modifier
            .clickable {
                onClick(viewData, label)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = viewData.value,
        )
    }
}

@Composable
private fun PrefixLengthDisplayRow(prefixLengthText: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = prefixLengthText,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
        )
    }
}
