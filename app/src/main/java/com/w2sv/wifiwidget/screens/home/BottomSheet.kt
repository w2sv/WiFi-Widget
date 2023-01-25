package com.w2sv.wifiwidget.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun BottomSheet(scaffoldState: BottomSheetScaffoldState) {
    val coroutineScope = rememberCoroutineScope()

    BackHandler(scaffoldState.bottomSheetState.isExpanded) {
        coroutineScope.launch { scaffoldState.bottomSheetState.collapse() }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ToggleButton(scaffoldState.bottomSheetState, coroutineScope)
        SheetContent(scaffoldState.snackbarHostState)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ToggleButton(sheetState: BottomSheetState, coroutineScope: CoroutineScope) {
    IconButton(
        onClick = {
            coroutineScope.launch {
                if (sheetState.isExpanded)
                    sheetState.collapse()
                else
                    sheetState.expand()
            }
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = if (sheetState.isExpanded)
                Icons.Filled.KeyboardArrowDown
            else
                Icons.Filled.KeyboardArrowUp,
            contentDescription = stringResource(id = R.string.configure_widget),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun SheetContent(snackbarHostState: SnackbarHostState) {
    Surface(
        modifier = Modifier.padding(horizontal = 40.dp),
        color = colorResource(id = R.color.mischka_dark),
        shape = RoundedCornerShape(40.dp, 40.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.sheet_title),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                style = TextStyle(
                    color = colorResource(
                        id = R.color.magenta
                    )
                )
            )
            WidgetPropertyColumn(snackbarHostState)
        }
    }
}

@Composable
private fun ColumnScope.WidgetPropertyColumn(
    snackbarHostState: SnackbarHostState,
    viewModel: HomeActivity.ViewModel = viewModel()
) {
    var showSnackbar by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 26.dp)
            .padding(top = 12.dp)
            .verticalScroll(rememberScrollState())
            .weight(1f, fill = false)
    ) {
        mapOf(
            R.string.ssid to "showSSID",
            R.string.ipv4 to "showIPv4",
            R.string.frequency to "showFrequency",
            R.string.gateway to "showGateway",
            R.string.netmask to "showSubnetMask",
            R.string.dns to "showDNS",
            R.string.dhcp to "showDHCP"
        )
            .run {
                asSequence().forEachIndexed { index, (stringId, preferenceKey) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = stringId),
                            modifier = Modifier.weight(1f, fill = true),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Checkbox(
                            checked = viewModel.widgetPropertyStates.getValue(preferenceKey),
                            onCheckedChange = {
                                if (unchecksEverything(
                                        it,
                                        preferenceKey,
                                        viewModel.widgetPropertyStates
                                    )
                                )
                                    showSnackbar = true
                                else
                                    viewModel.widgetPropertyStates[preferenceKey] = it
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    if (index != size - 1)
                        Divider(color = Color.DarkGray)
                }
            }
    }

    if (showSnackbar) {
        stringResource(id = R.string.uncheck_all_properties_toast).let {
            LaunchedEffect(key1 = it) {
                snackbarHostState.showSnackbar(it)
                showSnackbar = false
            }
        }
    }
}

private fun unchecksEverything(
    newValue: Boolean,
    preferenceKey: String,
    currentProperties: Map<String, Boolean>
): Boolean =
    !newValue && currentProperties.all { (k, v) -> k == preferenceKey || !v }