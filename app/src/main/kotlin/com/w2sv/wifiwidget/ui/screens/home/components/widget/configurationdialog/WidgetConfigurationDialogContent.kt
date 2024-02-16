package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.components.IconHeaderProperties
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.AppearanceConfiguration
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyCheckRows
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

private val verticalSectionHeaderPadding = 18.dp

@Immutable
private data class Section(
    val iconHeaderProperties: IconHeaderProperties,
    val headerModifier: Modifier = Modifier.padding(vertical = verticalSectionHeaderPadding),
    val content: @Composable () -> Unit
)

@Composable
fun WidgetConfigurationDialogContent(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showPropertyInfoDialog: (InfoDialogData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState()),
    ) {
        val context: Context = LocalContext.current

        remember {
            persistentListOf(
                Section(
                    iconHeaderProperties = IconHeaderProperties(
                        iconRes = R.drawable.ic_palette_24,
                        stringRes = R.string.appearance
                    ),
                ) {
                    AppearanceConfiguration(
                        presetColoringData = widgetConfiguration.presetColoringData.collectAsStateWithLifecycle().value,
                        setPresetColoringData = {
                            widgetConfiguration.presetColoringData.value = it
                        },
                        customColoringData = widgetConfiguration.customColoringData.collectAsStateWithLifecycle().value,
                        setCustomColoringData = {
                            widgetConfiguration.customColoringData.value = it
                        },
                        coloring = widgetConfiguration.coloring.collectAsStateWithLifecycle().value,
                        setColoring = { widgetConfiguration.coloring.value = it },
                        opacity = widgetConfiguration.opacity.collectAsStateWithLifecycle().value,
                        onOpacityChanged = {
                            widgetConfiguration.opacity.value = it
                        }
                    )
                },
                Section(
                    IconHeaderProperties(
                        iconRes = R.drawable.ic_checklist_24,
                        stringRes = R.string.properties
                    )
                ) {
                    PropertyCheckRows(
                        dataList = rememberWidgetWifiPropertyCheckRowData(
                            widgetConfiguration = widgetConfiguration,
                            locationAccessState = locationAccessState
                        ),
                        showInfoDialog = showPropertyInfoDialog,
                    )
                },
                Section(
                    iconHeaderProperties = IconHeaderProperties(
                        iconRes = R.drawable.ic_bottom_row_24,
                        stringRes = R.string.bottom_row,
                    )
                ) {
                    PropertyCheckRows(
                        dataList = remember {
                            WidgetBottomRowElement.entries.map {
                                PropertyCheckRowData.fromMutableMap(
                                    property = it,
                                    isCheckedMap = widgetConfiguration.bottomRowMap
                                )
                            }
                                .toPersistentList()
                        }
                    )
                },
                Section(
                    iconHeaderProperties = IconHeaderProperties(
                        iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
                        stringRes = R.string.refreshing,
                    )
                ) {
                    PropertyCheckRows(
                        dataList = remember {
                            persistentListOf(
                                PropertyCheckRowData.fromMutableMap(
                                    property = WidgetRefreshingParameter.RefreshPeriodically,
                                    isCheckedMap = widgetConfiguration.refreshingParametersMap,
                                    infoDialogData = InfoDialogData(
                                        title = context.getString(WidgetRefreshingParameter.RefreshPeriodically.labelRes),
                                        description = context.getString(R.string.refresh_periodically_info)
                                    ),
                                    subPropertyCheckRowData = persistentListOf(
                                        PropertyCheckRowData.fromMutableMap(
                                            property = WidgetRefreshingParameter.RefreshOnLowBattery,
                                            isCheckedMap = widgetConfiguration.refreshingParametersMap
                                        )
                                    )
                                )
                            )
                        },
                        showInfoDialog = showPropertyInfoDialog,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            )
        }
            .forEach {
                Column(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    SectionHeader(
                        iconHeaderProperties = it.iconHeaderProperties,
                        modifier = it.headerModifier
                    )
                    it.content()
                }
            }
    }
}

@Composable
private fun SectionHeader(
    iconHeaderProperties: IconHeaderProperties,
    modifier: Modifier = Modifier,
) {
    IconHeader(
        properties = iconHeaderProperties,
        modifier = modifier.padding(horizontal = 16.dp),
    )
}
