package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.util.alphaDecreased

@Stable
interface DropdownMenuScope : ColumnScope {
    fun collapseMenu()
}

@Composable
fun MoreIconButtonWithDropdownMenu(
    modifier: Modifier = Modifier,
    iconTint: Color = LocalContentColor.current,
    dropdownMenuContent: @Composable (DropdownMenuScope.() -> Unit)
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, stringResource(R.string.show_dropdown_menu), tint = iconTint)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.border(
                width = Dp.Hairline,
                color = MaterialTheme.colorScheme.secondary.alphaDecreased(),
                shape = MaterialTheme.shapes.extraSmall
            ),
            offset = DpOffset((-8).dp, 0.dp)
        ) {
            val scope = remember {
                object : DropdownMenuScope, ColumnScope by this {
                    override fun collapseMenu() {
                        expanded = false
                    }
                }
            }
            with(scope) { dropdownMenuContent() }
        }
    }
}
