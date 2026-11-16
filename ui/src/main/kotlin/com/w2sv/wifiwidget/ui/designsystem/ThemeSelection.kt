package com.w2sv.wifiwidget.ui.designsystem

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.core.resources.R
import com.w2sv.domain.preferences.Theme

@Preview
@Composable
private fun ThemeSelectionRowPrev() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ThemeSelectionRow(Theme.Default, {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ThemeSelectionRow(
    selected: Theme,
    onSelect: (Theme) -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 44.dp,
    badgeOutlineColor: Color = MaterialTheme.colorScheme.surface,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
) {
    val indicators = remember { themeIndicators() }
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        indicators.forEach { properties ->
            ThemeIndicator(
                properties = properties,
                isSelected = properties.theme == selected,
                onClick = { onSelect(properties.theme) },
                buttonSize = buttonSize,
                badgeOutlineColor = badgeOutlineColor
            )
        }
    }
}

private fun themeIndicators(): List<ThemeIndicator> =
    listOf(
        ThemeIndicator(
            theme = Theme.Light,
            labelRes = R.string.light,
            brush = SolidColor(Color.White)
        ),
        ThemeIndicator(
            theme = Theme.Default,
            labelRes = R.string.default_,
            brush = Brush.linearGradient(
                0.5f to Color.White,
                0.5f to Color.Black
            )
        ),
        ThemeIndicator(
            theme = Theme.Dark,
            labelRes = R.string.dark,
            brush = SolidColor(Color.Black)
        )
    )

@Immutable
private data class ThemeIndicator(val theme: Theme, @StringRes val labelRes: Int, val brush: Brush)

@Composable
private fun ThemeIndicator(
    properties: ThemeIndicator,
    isSelected: Boolean,
    buttonSize: Dp,
    badgeOutlineColor: Color,
    onClick: () -> Unit
) {
    val label = stringResource(id = properties.labelRes)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        ColorSwatch(
            brush = properties.brush,
            diameter = buttonSize,
            badgeOutlineColor = badgeOutlineColor,
            modifier = Modifier.semantics {
                contentDescription = label
                selected = isSelected
            },
            selected = isSelected,
            showCheckmark = true,
            onClick = onClick,
            onClickLabel = stringResource(R.string.theme_button_cd, label),
            role = Role.RadioButton
        )
    }
}
