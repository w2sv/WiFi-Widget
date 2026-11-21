package com.w2sv.widget.ui.content

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasClickAction
import androidx.glance.testing.unit.hasTextEqualTo
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.preview.widgetPreviewState
import com.w2sv.widget.ui.state.WidgetPropertyRow
import com.w2sv.widget.ui.state.WidgetUtilities
import com.w2sv.widget.ui.state.WidgetWifiState
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiWidgetContentTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `preview content renders sample SSID and BSSID without an application entry point`() =
        runGlanceAppWidgetUnitTest(timeout = 10.seconds) {
            val rows = listOf(
                WidgetPropertyRow(WifiProperty.SSID, WifiPropertyPresentation(SubscriptableText("SSID"), "Coffee Shop Wi-Fi")),
                WidgetPropertyRow(WifiProperty.BSSID, WifiPropertyPresentation(SubscriptableText("BSSID"), "02:00:00:12:34:56"))
            )
            val state = widgetPreviewState(context).copy(
                wifiState = WidgetWifiState.Connected(WifiStatus.Connected, rows),
                utilities = WidgetUtilities(showRefreshTime = false, actions = emptyList())
            )
            setContext(context)
            provideComposable { WifiWidgetContent(state) }

            onNode(hasTextEqualTo("SSID")).assertExists()
            onNode(hasTextEqualTo("BSSID")).assertExists()
            onAllNodes(hasClickAction()).assertCountEquals(2)
        }

    @Test
    @Config(sdk = [28])
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `legacy drawable background preserves rounded corners and configured alpha`() {
        listOf(0, 128, 255).forEach { alpha ->
            val color = (alpha shl 24) or 0x00112233
            val bitmap = legacyWidgetBackgroundBitmap(context, DpSize(160.dp, 100.dp), color)
            assertEquals(0, Color.alpha(bitmap.getPixel(0, 0)))
            assertEquals(alpha, Color.alpha(bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)))
        }
    }
}
