package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppFontText
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

@Composable
fun WifiPropertiesDisplay(
    propertiesViewData: Flow<WidgetWifiProperty.ValueViewData>,
    modifier: Modifier = Modifier,
) {
    var viewData by remember {
        mutableStateOf(emptyList<WidgetWifiProperty.ValueViewData>())
    }

    LaunchedEffect(propertiesViewData) {
        viewData = propertiesViewData.toList()
    }

    AnimatedContent(targetState = viewData.isEmpty(), label = "", modifier = modifier) {
        if (it) {
            LoadingPlaceholder()
        } else {
            PropertiesList(viewData = viewData)
        }
    }
}

@Composable
private fun LoadingPlaceholder(modifier: Modifier = Modifier) {
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
        AppFontText(
            text = stringResource(R.string.getting_data),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PropertiesList(
    viewData: List<WidgetWifiProperty.ValueViewData>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            HeaderRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )
        }
        items(viewData) { viewData ->
            WifiPropertyDisplay(
                viewData = viewData,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 26.dp)
            )
            (viewData as? WidgetWifiProperty.ValueViewData.IPProperty)?.prefixLengthText?.let {
                PrefixLengthDisplay(
                    prefixLengthText = it,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HeaderRow(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        AppFontText(
            text = stringResource(id = R.string.properties),
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
        )
        AppFontText(
            text = stringResource(R.string.click_to_copy_to_clipboard),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun WifiPropertyDisplay(
    viewData: WidgetWifiProperty.ValueViewData,
    modifier: Modifier = Modifier,
    clipboardManager: ClipboardManager = LocalClipboardManager.current,
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    Row(
        modifier = modifier
            .clickable {
                clipboardManager.setText(AnnotatedString(viewData.value))
                scope.launch {
                    snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                        AppSnackbarVisuals(
                            message = context.getString(
                                R.string.copied_to_clipboard,
                                viewData.label
                            ),
                            kind = SnackbarKind.Success,
                        ),
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppFontText(
            text = buildAnnotatedString {
                if (viewData is WidgetWifiProperty.ValueViewData.IPProperty) {
                    append(stringResource(R.string.ip))
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
            },
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        AppFontText(
            text = viewData.value,
        )
    }
}

@Composable
private fun PrefixLengthDisplay(prefixLengthText: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        IPSubPropertyText(text = prefixLengthText)
    }
}

@Composable
private fun IPSubPropertyText(text: String) {
    AppFontText(
        text = text,
        modifier = Modifier
            .border(
                width = Dp.Hairline,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(vertical = 2.dp, horizontal = 8.dp),
        fontSize = 11.sp,
    )
}
