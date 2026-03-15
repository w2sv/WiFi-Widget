package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.composed.core.extensions.thenIfNotNull
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.IconDefaults
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import com.w2sv.wifiwidget.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.wifiwidget.ui.util.ShakeController
import com.w2sv.wifiwidget.ui.util.alphaDecreased
import com.w2sv.wifiwidget.ui.util.contentDescription
import com.w2sv.wifiwidget.ui.util.offsetClip
import com.w2sv.wifiwidget.ui.util.shake
import kotlinx.collections.immutable.ImmutableList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

object CheckRowDefaults {

    val subPropertyLessStartPadding = 48.dp
}

object SubPropertyColumnDefaults {
    val fontSize = 14.sp
    val startPadding = 12.dp
}

/**
 * For alignment of primary check row click elements with sub property click elements
 */
private val primaryCheckRowModifier = Modifier.padding(end = 16.dp)

@Composable
fun CheckRowColumn(elements: ImmutableList<ConfigListElement.CheckRow>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        elements.forEach { data ->
            when (data.hasSubProperties) {
                false -> CheckRow(data = data, modifier = primaryCheckRowModifier)
                true -> CheckRowWithSubProperties(data = data)
            }
        }
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
                val itemModifier = Modifier
                    .longPressDraggableHandle(
                        onDragStarted = {
                            localHapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    .thenIf(isDragging) {
                        val shadowColor = MaterialTheme.colorScheme.secondary.alphaDecreased()
                        shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = shadowColor,
                            spotColor = shadowColor
                        )
                    }

                when (data.hasSubProperties) {
                    false -> {
                        CheckRow(
                            data = data,
                            modifier = primaryCheckRowModifier.then(itemModifier)
                        )
                    }

                    true -> {
                        CheckRowWithSubProperties(
                            data = data,
                            modifier = itemModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckRow(data: ConfigListElement.CheckRow, modifier: Modifier = Modifier) {
    CheckRowBase(
        data = data,
        modifier = modifier.padding(start = CheckRowDefaults.subPropertyLessStartPadding),
        labelColor = data.leadingIconAndLabelColor
    )
}

@Composable
private fun CheckRowWithSubProperties(data: ConfigListElement.CheckRow, modifier: Modifier = Modifier) {
    var expandSubProperties by rememberSaveable {
        mutableStateOf(!data.allowSubPropertyCollapsing)
    }
    // Collapse subProperties on unchecking
    LaunchedEffect(data, data.isChecked()) {
        when {
            !data.isChecked() -> expandSubProperties = false
            !data.allowSubPropertyCollapsing -> expandSubProperties = true
        }
    }

    Column(modifier = modifier) {
        CheckRowBase(
            data = data,
            leadingIcon = {
                if (!data.allowSubPropertyCollapsing) return@CheckRowBase

                IconButton(
                    onClick = { expandSubProperties = !expandSubProperties },
                    enabled = data.isChecked()
                ) {
                    Icon(
                        imageVector = if (expandSubProperties) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            },
            modifier = primaryCheckRowModifier.thenIf(!data.allowSubPropertyCollapsing) { padding(start = CheckRowDefaults.subPropertyLessStartPadding) },
            labelColor = data.leadingIconAndLabelColor
        )

        AnimatedVisibility(visible = expandSubProperties) {
            SubPropertyColumn(elements = requireNotNull(data.subPropertyColumnElements))
        }
    }
}

@Composable
private fun SubPropertyColumn(elements: ImmutableList<ConfigListElement>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(start = 24.dp) // Make background start at the indentation of CheckRowBase label
            .nestedContentBackground()
            .padding(start = SubPropertyColumnDefaults.startPadding)
    ) {
        elements.forEach { element ->
            when (element) {
                is ConfigListElement.CheckRow -> {
                    if (element.show()) {
                        CheckRowBase(
                            data = element,
                            fontSize = SubPropertyColumnDefaults.fontSize
                        )
                    }
                }

                is ConfigListElement.Custom -> {
                    element.content()
                }
            }
        }
    }
}

@Composable
private fun CheckRowBase(
    data: ConfigListElement.CheckRow,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val label = stringResource(id = data.property.labelRes)
    PropertyConfigurationRow(
        data.property.labelRes,
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
fun PropertyConfigurationRow(
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    shakeController: ShakeController? = null,
    @StringRes explanationRes: Int? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    endContent: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .thenIfNotNull(shakeController) { shake(it) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            leadingIcon?.invoke()
            Text(
                text = stringResource(id = labelRes),
                modifier = Modifier.weight(1.0f),
                fontSize = fontSize,
                color = labelColor
            )
            endContent()
        }
        explanationRes?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.onSurfaceVariantLowAlpha,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(end = 32.dp)
                    .offsetClip(dy = (-10).dp) // Shift explanation up a bit to increase its visual coherence with the main row
            )
        }
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
