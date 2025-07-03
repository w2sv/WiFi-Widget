package com.w2sv.wifiwidget.ui.designsystem

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.utils.alphaDecreased
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class DropdownMenuItemProperties(
    @all:StringRes val textRes: Int,
    val onClick: () -> Unit,
    val enabled: () -> Boolean,
    @all:DrawableRes val leadingIconRes: Int
)

@SuppressLint("ComposeMultipleContentEmitters")
@Composable
fun MoreIconButtonWithDropdownMenu(menuItems: ImmutableList<DropdownMenuItemProperties>, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    IconButton(onClick = { expanded = !expanded }, modifier = modifier) {
        Icon(Icons.Default.MoreVert, stringResource(R.string.show_dropdown_menu))
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
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = { Text(text = stringResource(item.textRes)) },
                onClick = item.onClick,
                enabled = item.enabled(),
                leadingIcon = {
                    Icon(painterResource(item.leadingIconRes), null)
                }
            )
        }
    }
}
