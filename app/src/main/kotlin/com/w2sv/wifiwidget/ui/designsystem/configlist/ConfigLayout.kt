package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import com.w2sv.wifiwidget.ui.designsystem.BelowEndAnchoring
import com.w2sv.wifiwidget.ui.designsystem.BoxScopeComposable
import com.w2sv.wifiwidget.ui.designsystem.Margins
import com.w2sv.wifiwidget.ui.designsystem.TLayout

@Composable
fun ConfigLayout(
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    leading: BoxScopeComposable = {},
    below: BoxScopeComposable? = null,
    belowEndAnchoring: BelowEndAnchoring = BelowEndAnchoring.Default,
    belowMargins: Margins = Margins(),
    trailing: @Composable RowScope.() -> Unit
) {
    TLayout(
        leading = leading,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(
                text = stringResource(id = labelRes),
                fontSize = fontSize,
                color = labelColor
            )
        },
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = trailing
            )
        },
        below = below,
        belowEndAnchoring = belowEndAnchoring,
        labelMargins = Margins.empty,
        belowMargins = belowMargins
    )
}
