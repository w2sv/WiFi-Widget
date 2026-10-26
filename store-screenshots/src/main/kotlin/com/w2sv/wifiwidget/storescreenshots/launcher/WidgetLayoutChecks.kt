package com.w2sv.wifiwidget.storescreenshots.launcher

import androidx.test.uiautomator.By
import com.w2sv.core.resources.R as ResourcesR
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotDevice
import com.w2sv.wifiwidget.storescreenshots.scenario.ScreenshotScenario

/** Optional launcher layout diagnostics, separate from normal screenshot capture. */
internal class WidgetLayoutChecks(private val screenshotDevice: ScreenshotDevice) {
    private val device = screenshotDevice.ui
    private val appPackage = screenshotDevice.appPackage

    fun checkScenarioRowsFullyVisible(scenario: ScreenshotScenario) {
        val listBounds = checkNotNull(device.findObject(By.res(appPackage, "wifi_property_list_view"))) {
            "The widget property list is unavailable. ${screenshotDevice.hierarchy()}"
        }.visibleBounds
        val rows = device.findObjects(By.res(appPackage, "wifi_property_layout"))
            .sortedBy { it.visibleBounds.top }
        check(rows.size == scenario.properties.size) {
            "Expected ${scenario.properties.size} visible property rows, found ${rows.size}. ${screenshotDevice.hierarchy()}"
        }
        val normalRowHeight = rows.first().visibleBounds.height()
        check(
            rows.zip(scenario.properties).all { (row, resolution) ->
                val bounds = row.visibleBounds
                listBounds.contains(bounds) &&
                    (resolution.values.any { it.subValues.isNotEmpty() } || bounds.height() >= normalRowHeight - ROW_HEIGHT_TOLERANCE_PX)
            }
        ) {
            "A property row is clipped by the widget list: list=$listBounds, rows=${rows.map {
                it.visibleBounds
            }}. ${screenshotDevice.hierarchy()}"
        }

        val labels = device.findObjects(By.res(appPackage, "property_label_tv"))
            .sortedBy { it.visibleBounds.top }
        check(labels.size == scenario.properties.size) {
            "Expected ${scenario.properties.size} visible property labels, found ${labels.size}. ${screenshotDevice.hierarchy()}"
        }
        scenario.properties.forEachIndexed { index, resolution ->
            val property = resolution.property
            if (property is WifiProperty.IpProperty) {
                val resources = screenshotDevice.targetContext.resources
                val expectedLabel = resources.getString(ResourcesR.string.ip) + resources.getString(property.subscriptResId)
                check(labels[index].text == expectedLabel) {
                    "The IP label is incorrect for $property: expected=$expectedLabel, actual=${labels[index].text}. ${screenshotDevice.hierarchy()}"
                }
            }
        }
    }

    fun checkBottomUtilityButtonsVisible() {
        val widgetBounds = checkNotNull(device.findObject(By.res(appPackage, "widget_layout"))) {
            "The resized widget is no longer visible. ${screenshotDevice.hierarchy()}"
        }.visibleBounds
        val buttons = listOf("refresh_button", "go_to_wifi_settings_button", "go_to_widget_settings_button")
            .map { id ->
                checkNotNull(device.findObject(By.res(appPackage, id))) {
                    "The widget's $id is missing after resize. ${screenshotDevice.hierarchy()}"
                }.visibleBounds
            }
        check(buttons.all { widgetBounds.contains(it) && it.width() >= buttons.first().width() - GRID_SNAP_TOLERANCE_PX }) {
            "The resized widget's bottom utility buttons are clipped: $buttons, widget=$widgetBounds. ${screenshotDevice.hierarchy()}"
        }
    }

    private companion object {
        const val ROW_HEIGHT_TOLERANCE_PX = 2
        const val GRID_SNAP_TOLERANCE_PX = 24
    }
}
