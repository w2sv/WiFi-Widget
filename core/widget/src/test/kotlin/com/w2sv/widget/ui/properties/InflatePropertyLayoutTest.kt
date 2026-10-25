package com.w2sv.widget.ui.properties

import android.content.Intent
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.widget.R
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class InflatePropertyLayoutTest {
    private val colors = WidgetColors(background = 0, primary = 1, secondary = 2)

    @Test
    fun `Battery Saver placeholder explicitly clears a previous clipboard action`() {
        val views = mockk<RemoteViews>(relaxed = true)
        val valid = WifiPropertyPresentation(SubscriptableText("IP", "Public"), "84.1.2.3")
        val placeholder = WifiPropertyPresentation(
            label = valid.label,
            value = "Unavailable while Battery Saver is on",
            resolutionError = WifiPropertyResolutionError.BatterySaverRestricted
        )

        views.inflatePropertyLayout(valid, colors, FontSize.Medium)
        views.inflatePropertyLayout(placeholder, colors, FontSize.Medium)
        views.inflatePropertyLayout(valid.copy(value = "84.1.2.4"), colors, FontSize.Medium)

        verifyOrder {
            views.setOnClickFillInIntent(
                R.id.wifi_property_layout,
                match {
                    CopyPropertyToClipboardActivity.Property.fromIntent(it)?.value ==
                        "84.1.2.3"
                }
            )
            views.setOnClickFillInIntent(R.id.wifi_property_layout, null)
            views.setOnClickFillInIntent(
                R.id.wifi_property_layout,
                match {
                    CopyPropertyToClipboardActivity.Property.fromIntent(it)?.value ==
                        "84.1.2.4"
                }
            )
        }
    }

    @Test
    fun `all resolution errors clear widget clipboard actions`() {
        val views = mockk<RemoteViews>(relaxed = true)
        val presentation = WifiPropertyPresentation(SubscriptableText("SSID"), "Unavailable")

        WifiPropertyResolutionError.entries.forEach { error ->
            views.inflatePropertyLayout(
                presentation.copy(resolutionError = error),
                colors,
                FontSize.Medium
            )
        }

        verify(exactly = WifiPropertyResolutionError.entries.size) {
            views.setOnClickFillInIntent(R.id.wifi_property_layout, null)
        }
    }

    @Test
    fun `reapplying a placeholder RemoteViews updates the displayed text`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val valid = WifiPropertyPresentation(SubscriptableText("IP", "Public"), "84.1.2.3")
        val placeholder = valid.copy(
            value = "Unavailable while Battery Saver is on",
            resolutionError = WifiPropertyResolutionError.BatterySaverRestricted
        )
        val originalViews = RemoteViews(context.packageName, R.layout.wifi_property_right_aligned).apply {
            inflatePropertyLayout(valid, colors, FontSize.Medium)
        }
        val replacementViews = RemoteViews(context.packageName, R.layout.wifi_property_right_aligned).apply {
            inflatePropertyLayout(placeholder, colors, FontSize.Medium)
        }

        val row = originalViews.apply(context, null)
        replacementViews.reapply(context, row)

        assertEquals(placeholder.value, row.findViewById<TextView>(R.id.property_value_tv).text.toString())
    }

    @Test
    fun `IP clipboard labels retain subscripts and value enumeration`() {
        val first = WifiPropertyPresentation(SubscriptableText("IP", "SiteLocal 1"), "192.168.1.2")
        val second = WifiPropertyPresentation(SubscriptableText("IP", "SiteLocal 2"), "fe80::2")

        assertEquals(CopyPropertyToClipboardActivity.Property("IP SiteLocal 1", "192.168.1.2"), argsFor(first))
        assertEquals(CopyPropertyToClipboardActivity.Property("IP SiteLocal 2", "fe80::2"), argsFor(second))
    }

    @Test
    fun `row changes do not shift or retain clipboard arguments`() {
        val ssid = WifiPropertyPresentation(SubscriptableText("SSID"), "Home")
        val publicIp = WifiPropertyPresentation(SubscriptableText("IP", "Public"), "84.1.2.3")
        val isp = WifiPropertyPresentation(SubscriptableText("ISP"), "Example ISP")

        assertEquals(listOf("Home", "84.1.2.3", "Example ISP"), listOf(ssid, publicIp, isp).map { argsFor(it).value })
        assertEquals(
            listOf("Example ISP", "Home", "84.1.2.4"),
            listOf(isp, ssid, publicIp.copy(value = "84.1.2.4")).map {
                argsFor(it).value
            }
        )
        assertEquals(listOf("84.1.2.4", "Home"), listOf(publicIp.copy(value = "84.1.2.4"), ssid).map { argsFor(it).value })
    }

    private fun argsFor(presentation: WifiPropertyPresentation): CopyPropertyToClipboardActivity.Property {
        val views = mockk<RemoteViews>(relaxed = true)
        val fillInIntent = slot<Intent>()

        views.inflatePropertyLayout(
            presentation,
            colors,
            FontSize.Medium
        )

        verify(exactly = 1) { views.setOnClickFillInIntent(R.id.wifi_property_layout, capture(fillInIntent)) }
        return checkNotNull(CopyPropertyToClipboardActivity.Property.fromIntent(fillInIntent.captured))
    }
}
