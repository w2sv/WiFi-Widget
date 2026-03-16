package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.wifiwidget.ui.util.alphaDecreased
import kotlinx.collections.immutable.ImmutableList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun CheckRowColumn(
    elements: ImmutableList<ConfigItem.Checkable>,
    modifier: Modifier = Modifier,
    arrangement: Arrangement.Vertical = Arrangement.Top
) {
    Column(modifier = modifier.then(Modifier.padding(end = ConfigListToken.checkRowEndPadding)), verticalArrangement = arrangement) {
        elements.forEach { data ->
            CheckRow(
                checkable = data,
                modifier = Modifier.padding(start = ConfigListToken.startPaddingIfNoToggleButton)
            )
        }
    }
}

@Composable
fun DragAndDroppableCheckRowColumn(
    elements: ImmutableList<ConfigItem.Checkable>,
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
        modifier = modifier
            .then(Modifier.padding(end = ConfigListToken.checkRowEndPadding))
            .heightIn(
                max = 2_000.dp // Necessary due to nesting inside scrollable column. Chosen arbitrarily.
            ),
        userScrollEnabled = false
    ) {
        items(elements, key = { it.property.labelRes }) { data ->
            ReorderableItem(reorderableLazyListState, key = data.property.labelRes) { isDragging ->
                CheckRow(
                    checkable = data,
                    modifier = Modifier
                        .longPressDraggableHandle(
                            onDragStarted = { localHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) }
                        )
                        .thenIf(isDragging) { dragShadow() },
                    padStartIfNoToggleButton = true
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
