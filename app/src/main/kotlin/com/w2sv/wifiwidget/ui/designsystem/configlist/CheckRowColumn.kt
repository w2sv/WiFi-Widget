package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.IconDefaults
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.util.alphaDecreased
import com.w2sv.wifiwidget.ui.util.contentDescription
import kotlinx.collections.immutable.ImmutableList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

object CheckRowDefaults {
    val endPadding = 16.dp
}

object SubSettingsColumnDefaults {
    val fontSize = 14.sp
    val startPadding = 12.dp
}

@Composable
fun CheckRowColumn(elements: ImmutableList<ConfigListElement.CheckRow>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        elements.forEach { data -> PropertyCheckRow(data = data, startPaddingIfToggleButtonAbsent = 24.dp) }
    }
}

@Composable
fun DragAndDroppableCheckRowColumn(
    elements: ImmutableList<ConfigListElement.CheckRow>,
    modifier: Modifier = Modifier,
    onDrop: (fromIndex: Int, toIndex: Int) -> Unit
) {
    val localHapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onDrop(from.index, to.index)
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.heightIn(
            max = 2_000.dp // Necessary due to nesting inside scrollable column. Chosen arbitrarily.
        ),
        userScrollEnabled = false
    ) {
        items(elements, key = { it.property.labelRes }) { data ->
            ReorderableItem(reorderableLazyListState, key = data.property.labelRes) { isDragging ->
                PropertyCheckRow(
                    data = data,
                    startPaddingIfToggleButtonAbsent = 48.dp,
                    modifier = Modifier
                        .longPressDraggableHandle(
                            onDragStarted = { localHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) }
                        )
                        .thenIf(isDragging) { dragShadow() }
                )
            }
        }
    }
}

@Composable
private fun Modifier.dragShadow(): Modifier {
    val color = MaterialTheme.colorScheme.secondary.alphaDecreased()
    return shadow(
        elevation = 1.dp,
        shape = RoundedCornerShape(32.dp),
        ambientColor = color,
        spotColor = color
    )
}

@Composable
private fun PropertyCheckRow(data: ConfigListElement.CheckRow, startPaddingIfToggleButtonAbsent: Dp, modifier: Modifier = Modifier) {
    when (data.hasSubSettings) {
        false -> {
            CheckRow(
                data = data,
                modifier = modifier.padding(start = startPaddingIfToggleButtonAbsent, end = CheckRowDefaults.endPadding)
            )
        }

        true -> {
            CheckRowWithSubProperties(
                data = data,
                modifier = modifier,
                startPaddingOnHiddenToggleButton = startPaddingIfToggleButtonAbsent
            )
        }
    }
}

@Composable
fun CheckRow(
    data: ConfigListElement.CheckRow,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    labelColor: Color = data.leadingIconAndLabelColor,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val label = stringResource(id = data.property.labelRes)
    ConfigRow(
        labelRes = data.property.labelRes,
        modifier = modifier.then(data.modifier),
        fontSize = fontSize,
        labelColor = labelColor,
        shakeController = data.shakeController,
        explanationRes = data.explanation,
        leadingIcon = leadingIcon
    ) {
        data.showInfoDialog?.let {
            InfoIconButton(
                onClick = it,
                contentDescription = stringResource(id = R.string.info_icon_cd, label)
            )
        }
        Checkbox(
            checked = data.isChecked(),
            onCheckedChange = { data.onCheckedChange(it) },
            modifier = Modifier.contentDescription(stringResource(id = R.string.set_unset, label))
        )
    }
}

@Composable
private fun InfoIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        InfoIcon(
            contentDescription = contentDescription,
            modifier = Modifier.size(IconDefaults.SizeBig),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
