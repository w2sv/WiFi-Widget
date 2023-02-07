package com.w2sv.wifiwidget.ui.home.widgetconfiguration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme

@Composable
internal fun ConfigurationColumn(
    modifier: Modifier = Modifier,
    selectedThemeIndex: () -> Int,
    onSelectedThemeIndex: (Int) -> Unit,
    propertyChecked: (String) -> Boolean,
    onCheckedChange: (String, Boolean) -> Unit,
    onInfoButtonClick: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SubHeader("Theme", Modifier.padding(top = 12.dp, bottom = 22.dp))
        ThemeSelectionRow(
            modifier = Modifier.fillMaxWidth(),
            selected = selectedThemeIndex,
            onSelected = onSelectedThemeIndex
        )

        SubHeader("Displayed Properties", Modifier.padding(vertical = 22.dp))
        StatelessPropertyRows(
            propertyChecked = propertyChecked,
            onCheckedChange = onCheckedChange,
            onInfoButtonClick = onInfoButtonClick
        )
    }
}

@Preview
@Composable
private fun SubHeaderPrev() {
    WifiWidgetTheme {
        SubHeader(text = "Theme")
    }
}

@Composable
private fun SubHeader(text: String, modifier: Modifier = Modifier) {
    JostText(
        text = text,
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.inversePrimary
    )
}

@Composable
private fun ThemeSelectionRow(
    modifier: Modifier = Modifier,
    selected: () -> Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        remember {
            listOf(
                ThemeIndicatorProperties(label = "Light", color = Color.White),
                ThemeIndicatorProperties(label = "Device Default", color = Color.Gray),
                ThemeIndicatorProperties(label = "Dark", color = Color.Black)
            )
        }
            .forEachIndexed { index, properties ->
                ThemeIndicator(
                    properties = properties,
                    selected = index == selected(),
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                ) {
                    onSelected(index)
                }
            }
    }
}

private data class ThemeIndicatorProperties(val label: String, val color: Color)

@Composable
private fun ThemeIndicator(
    properties: ThemeIndicatorProperties,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        JostText(
            text = properties.label,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_minimal))
        )
        ElevatedButton(
            onClick,
            modifier = Modifier
                .size(36.dp),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            colors = ButtonDefaults.elevatedButtonColors(containerColor = properties.color),
            border = if (selected)
                BorderStroke(3.dp, colorResource(id = com.w2sv.common.R.color.dark_cyan))
            else
                null
        ) {}
    }
}

@Composable
private fun StatelessPropertyRows(
    propertyChecked: (String) -> Boolean,
    onCheckedChange: (String, Boolean) -> Unit,
    onInfoButtonClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 26.dp)
    ) {
        stringArrayResource(id = R.array.wifi_properties)
            .forEachIndexed { propertyIndex, property ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    JostText(
                        text = property,
                        modifier = Modifier.weight(1f, fill = true),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp
                    )
                    Checkbox(
                        checked = propertyChecked(property),
                        onCheckedChange = { onCheckedChange(property, it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    IconButton(onClick = {
                        onInfoButtonClick(propertyIndex)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Click to toggle the property info dialog",
                            modifier = Modifier.size(
                                dimensionResource(id = R.dimen.size_icon)
                            ),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
    }
}