package com.w2sv.widget.ui.content

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.widget.ui.state.FormattedRenderTime
import com.w2sv.widget.ui.state.WidgetPresentationState
import com.w2sv.widget.ui.state.WidgetUtilities
import com.w2sv.widget.ui.state.WidgetWifiState
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

    @Test fun `floating surface follows resolved background alpha`() {
        val opaque = WidgetPresentationState(
            wifiState = WidgetWifiState.Disconnected(WifiStatus.Disabled),
            colors = WidgetColors(0xFFF7F9FF.toInt(), 0xFF00639A.toInt(), 0xFF30343B.toInt()),
            backgroundColor = 0xFFF7F9FF.toInt(),
            fontSize = FontSize.Medium,
            valueAlignment = WifiPropertyValueAlignment.Right,
            utilities = WidgetUtilities(false, emptyList()),
            renderTime = FormattedRenderTime("10:30 AM Sun", "10:30 AM")
        )
        listOf(0, 64, 128, 255).forEach { alpha ->
            val translucent = opaque.copy(backgroundColor = (alpha shl 24) or (opaque.backgroundColor and 0x00FFFFFF))
            assertEquals(alpha, Color.alpha(floatingSurfaceColor(translucent)))
        }
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
