package com.w2sv.wifiwidget.ui.home.configurationdialog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.Theme
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        ContentColumn(
            selectedTheme = { Theme.DeviceDefault },
            onSelectedTheme = {},
            opacity = { 1f },
            onOpacityChanged = {},
            propertyChecked = { true },
            onCheckedChange = { _, _ -> },
            onInfoButtonClick = {}
        )
    }
}

@Composable
internal fun ContentColumn(
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier,
    selectedTheme: () -> Theme,
    onSelectedTheme: (Theme) -> Unit,
    opacity: () -> Float,
    onOpacityChanged: (Float) -> Unit,
    propertyChecked: (String) -> Boolean,
    onCheckedChange: (String, Boolean) -> Unit,
    onInfoButtonClick: (Int) -> Unit
) {
    val showCustomColorSection: Boolean by viewModel.showCustomThemeSection.collectAsState(false)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            R.string.theme,
            R.drawable.ic_nightlight_24,
            Modifier.padding(top = 12.dp, bottom = 22.dp)
        )
        ThemeSelectionRow(
            modifier = Modifier.fillMaxWidth(),
            selected = selectedTheme,
            onSelected = onSelectedTheme
        )

        if (showCustomColorSection){
            SectionHeader(titleRes = R.string.custom_theme, iconRes = R.drawable.ic_nightlight_24)
        }

        SectionHeader(
            R.string.opacity,
            R.drawable.ic_opacity_24,
            Modifier.padding(vertical = 22.dp)
        )
        OpacitySliderWithValue(opacity = opacity, onOpacityChanged = onOpacityChanged)

        SectionHeader(
            R.string.properties,
            R.drawable.ic_checklist_24,
            Modifier.padding(vertical = 22.dp)
        )
        PropertyColumn(
            propertyChecked = propertyChecked,
            onCheckedChange = onCheckedChange,
            onInfoButtonClick = onInfoButtonClick
        )
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = "@null",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
            JostText(
                text = stringResource(id = titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.weight(0.6f))
    }
}