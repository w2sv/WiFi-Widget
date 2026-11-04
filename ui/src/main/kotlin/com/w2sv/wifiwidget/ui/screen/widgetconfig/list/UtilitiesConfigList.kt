package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.wifiwidget.ui.designsystem.IconHeadline
import com.w2sv.wifiwidget.ui.designsystem.IconHeadlineTokens
import com.w2sv.wifiwidget.ui.designsystem.SegmentedListTokens
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItem
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigItemLayout
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListTokens
import com.w2sv.wifiwidget.ui.util.PreviewOf

@Preview(showBackground = true)
@Composable
private fun UtilitiesConfigListPrev() {
    PreviewOf {
        UtilitiesConfigList(isEnabled = { true }, update = { _, _ -> })
    }
}

@Composable
fun UtilitiesConfigList(
    isEnabled: (WidgetUtility) -> Boolean,
    update: (WidgetUtility, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        IconHeadline(
            iconRes = R.drawable.ic_build_24,
            stringRes = R.string.utilities,
            modifier = Modifier.padding(bottom = IconHeadlineTokens.bottomSpacing)
        )
        Column(verticalArrangement = Arrangement.spacedBy(SegmentedListTokens.itemSpacing)) {
            WidgetUtility.entries.forEachIndexed { index, element ->
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = WidgetUtility.entries.size),
                    colors = SegmentedListTokens.colors(),
                    elevation = ListItemDefaults.elevation(SegmentedListTokens.defaultElevation),
                    contentPadding = PaddingValues(
                        end = ConfigListTokens.checkRowEndPadding,
                        start = ConfigListTokens.startPaddingIfNoToggleButton,
                        bottom = 4.dp
                    )
                ) {
                    ConfigItemLayout(
                        item = ConfigItem(
                            property = element,
                            isChecked = { isEnabled(element) },
                            trySetChecked = {
                                update(element, it)
                                true
                            },
                            supportingTextRes = element.explanation
                        )
                    )
                }
            }
        }
    }
}
