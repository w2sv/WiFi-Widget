package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.wifiwidget.ui.designsystem.nestedContentBackground
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CheckRowWithSubProperties(
    data: ConfigListElement.CheckRow,
    startPaddingOnHiddenToggleButton: Dp,
    modifier: Modifier = Modifier
) {
    val state = rememberExpandableListState(data)
    val showToggleButton = data.allowSubSettingCollapsing

    Column(
        modifier = modifier.padding(
            start = if (showToggleButton) 0.dp else startPaddingOnHiddenToggleButton,
            end = CheckRowDefaults.endPadding
        )
    ) {
        CheckRow(
            data = data,
            leadingIcon = {
                if (showToggleButton) {
                    SubSettingsToggleButton(
                        expand = state.isExpanded,
                        onClick = state::toggle,
                        isEnabled = data.isChecked()
                    )
                }
            }
        )

        AnimatedVisibility(visible = state.isExpanded) {
            SubSettings(
                elements = requireNotNull(data.subSettings),
                modifier = Modifier.thenIf(showToggleButton) { padding(start = 42.dp) })
        }
    }
}

@Stable
private class ExpandableListState(
    initialExpanded: Boolean,
    private val allowCollapse: Boolean
) {
    var isExpanded by mutableStateOf(initialExpanded)
        private set

    /** Toggle the expansion manually */
    fun toggle() {
        if (allowCollapse) {
            isExpanded = !isExpanded
        }
    }

    /** Forcefully set expansion */
    fun expand(expand: Boolean) {
        isExpanded = expand
    }
}

@Composable
private fun rememberExpandableListState(data: ConfigListElement.CheckRow): ExpandableListState {
    val state = rememberSaveable(
        saver = Saver(
            save = { it.isExpanded },
            restore = { ExpandableListState(it, data.allowSubSettingCollapsing) }
        )
    ) {
        ExpandableListState(
            initialExpanded = !data.allowSubSettingCollapsing,
            allowCollapse = data.allowSubSettingCollapsing
        )
    }
    val checked = data.isChecked()

    // Update automatically when data changes
    LaunchedEffect(data, checked) {
        when {
            !checked -> state.expand(false)
            !data.allowSubSettingCollapsing -> state.expand(true)
        }
    }

    return state
}

@Composable
private fun SubSettingsToggleButton(expand: Boolean, onClick: () -> Unit, isEnabled: Boolean) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled
    ) {
        Icon(
            imageVector = if (expand) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }
}

@Composable
private fun SubSettings(elements: ImmutableList<ConfigListElement>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .nestedContentBackground()
            .padding(start = SubSettingsColumnDefaults.startPadding)
    ) {
        elements.forEach { element ->
            when (element) {
                is ConfigListElement.CheckRow -> {
                    if (element.show()) {
                        CheckRow(
                            data = element,
                            fontSize = SubSettingsColumnDefaults.fontSize
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
