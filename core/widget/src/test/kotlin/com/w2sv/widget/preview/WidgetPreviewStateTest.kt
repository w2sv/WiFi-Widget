package com.w2sv.widget.preview

import android.content.Context
import androidx.glance.appwidget.SizeMode
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.widget.R as WidgetR
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetColoring
import com.w2sv.domain.widget.WidgetColoringStrategy
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.widget.ui.WifiGlanceWidget
import com.w2sv.widget.ui.actions.WidgetAction
import com.w2sv.widget.ui.state.WidgetWifiState
import com.w2sv.widget.ui.theme.resolve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.xmlpull.v1.XmlPullParser

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetPreviewStateTest {
    @Test fun `static launcher fallback is available at the standard footprint`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preview = requireNotNull(context.getDrawable(WidgetR.drawable.widget_preview_fallback))
        val providerInfo = context.resources.getXml(WidgetR.xml.widget_info)
        providerInfo.use {
            while (it.eventType != XmlPullParser.START_TAG) it.next()
            assertEquals(
                WidgetR.drawable.widget_preview_fallback,
                it.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "previewImage", 0)
            )
        }

        assertTrue(preview.intrinsicWidth >= 250)
        assertTrue(preview.intrinsicHeight >= 140)
        assertTrue(kotlin.math.abs(preview.intrinsicWidth.toFloat() / preview.intrinsicHeight - 250f / 140f) < 0.02f)
    }

    @Test fun `launcher preview uses default configuration and resolved colors`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val widget = WifiGlanceWidget()
        val state = widgetPreviewState(context)
        val rows = (state.wifiState as WidgetWifiState.Connected).rows

        assertEquals(SizeMode.Responsive(launcherPreviewSizes), widget.previewSizeMode)
        assertEquals(WidgetConfig.default.appearance.coloring.resolve(context), state.colors)
        assertTrue(rows.isNotEmpty())
        assertEquals(WidgetConfig.default.enabledProperties.map { it.protoId.toLong() * 1_000 }, rows.map { it.id })
        assertTrue(state.utilities.showRefreshTime)
        assertEquals(WidgetAction.entries, state.utilities.actions)
        assertEquals(WidgetConfig.default.appearance.fontSize, state.fontSize)
        assertEquals(WidgetConfig.default.appearance.propertyValueAlignment, state.valueAlignment)
        assertEquals(255, android.graphics.Color.alpha(state.backgroundColor))
        assertTrue(state.renderTime.normal.contains(state.renderTime.compact))
        assertTrue(state.renderTime.compact.isNotBlank())
    }

    @Test fun `pin preview follows configured selection order and appearance`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val customColors = WidgetColors(0xFF112233.toInt(), 0xFF445566.toInt(), 0xFF778899.toInt())
        val config = WidgetConfig.default
            .withUpdatedPropertyEnablement(WifiProperty.SSID, true)
            .withUpdatedPropertyEnablement(WifiProperty.Frequency, false)
            .withUpdatedPropertyPosition(0, 2)
            .copy(
                appearance = WidgetConfig.default.appearance.copy(
                    coloring = WidgetColoring(custom = WidgetColoringStrategy.Custom(customColors), useCustom = true),
                    backgroundOpacity = 0.5f,
                    fontSize = FontSize.Large,
                    propertyValueAlignment = WifiPropertyValueAlignment.Left
                ),
                utilities = WidgetUtility.entries.associateWith { it == WidgetUtility.RefreshButton }
            )
        val state = widgetPreviewState(context, config)
        val rows = (state.wifiState as WidgetWifiState.Connected).rows

        assertEquals(config.enabledProperties.map { it.protoId.toLong() * 1_000 }, rows.map { it.id })
        assertEquals(listOf(WidgetAction.Refresh), state.utilities.actions)
        assertFalse(state.utilities.showRefreshTime)
        assertTrue(rows.any { it.presentation.value == "Coffee Shop Wi-Fi" })
        assertEquals(customColors, state.colors)
        assertEquals(128, android.graphics.Color.alpha(state.backgroundColor))
        assertEquals(FontSize.Large, state.fontSize)
        assertEquals(WifiPropertyValueAlignment.Left, state.valueAlignment)
    }
}
