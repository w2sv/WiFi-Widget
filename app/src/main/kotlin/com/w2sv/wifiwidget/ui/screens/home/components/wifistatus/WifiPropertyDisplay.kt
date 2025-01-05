package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import android.content.Context
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.WifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.HomeScreenCardBackground
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import com.w2sv.wifiwidget.ui.utils.rememberSnackbarEmitter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty
import slimber.log.i

@Composable
fun WifiPropertyDisplay(propertiesViewData: Flow<WifiProperty.ViewData>, modifier: Modifier = Modifier) {
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

    @JvmInline
    @Immutable
    value class Property(val property: WifiProperty.ViewData) : PropertyListElement

    @Immutable
    data object LoadingAnimation : PropertyListElement
}

@Composable
private fun rememberRefreshingViewDataList(viewDataFlow: Flow<WifiProperty.ViewData>): SnapshotStateList<PropertyListElement> {
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
                    viewDataList.removeAt(viewDataList.lastIndex)
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PropertyList(viewDataList: ImmutableList<PropertyListElement>, modifier: Modifier = Modifier) {
    val onPropertyRowClick = rememberOnPropertyRowClick()

    LazyColumn(
        modifier = modifier
            .nestedContentBackground(color = MaterialTheme.colorScheme.background)
            .semantics {
                testTagsAsResourceId = true
                testTag = "scrollableWifiPropertyList"
            },
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
                    viewData.property.ipPropertyOrNull?.nonEmptySubPropertyValuesOrNull?.let { subPropertyValues ->
                        SubPropertyValueRow(
                            values = subPropertyValues.toPersistentList(),
                            modifier = Modifier
                                .padding(horizontal = horizontalPadding)
                                .padding(bottom = 2.dp)
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
        modifier = modifier
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

private typealias OnPropertyRowClick = (WifiProperty.ViewData, CharSequence, CoroutineScope) -> Unit

@Composable
private fun rememberOnPropertyRowClick(): OnPropertyRowClick {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarEmitter = rememberSnackbarEmitter()

    return remember {
        { viewData, label, scope ->
            clipboardManager.setText(AnnotatedString(viewData.value))
            snackbarEmitter.dismissCurrentAndShow(scope) {
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

private val horizontalPadding = 8.dp

@Composable
private fun PropertyDisplayRow(
    viewData: WifiProperty.ViewData,
    onClick: OnPropertyRowClick,
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val context: Context = LocalContext.current

    val label = remember(viewData) {
        buildAnnotatedString {
            if (viewData is WifiProperty.ViewData.IPProperty) {
                append(context.getString(R.string.ip))
                withStyle(
                    SpanStyle(
                        baselineShift = BaselineShift.Subscript,
                        fontSize = 12.sp
                    )
                ) {
                    append(viewData.label)
                }
            } else {
                append(viewData.label)
            }
        }
    }

    Row(modifier = modifier.clickable { onClick(viewData, label, scope) }) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(0.35f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = viewData.value)
    }
}

@Composable
private fun SubPropertyValueRow(values: ImmutableList<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
    ) {
        values.forEach {
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
