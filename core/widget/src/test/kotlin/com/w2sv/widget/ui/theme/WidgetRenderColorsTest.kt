package com.w2sv.widget.ui.theme

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.w2sv.domain.widget.WidgetColors
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetRenderColorsTest {
    @Test fun `opacity applies to background and floating surface without changing base colors or blends`() {
        val base = WidgetColors(0x7F123456, 0x80224466.toInt(), 0xCC778899.toInt())
        val expectedSurfaceRgb = ColorUtils.blendARGB(base.background or -0x1000000, base.primary or -0x1000000, 0.12f)
        val expectedSubProperty = ColorUtils.blendARGB(base.secondary, base.background, 0.2f)

        listOf(0f to 0, 0.5f to 128, 1f to 255).forEach { (opacity, alpha) ->
            val actual = base.toRenderColors(opacity)
            assertEquals(alpha, Color.alpha(actual.background))
            assertEquals(base.background and 0x00FFFFFF, actual.background and 0x00FFFFFF)
            assertEquals(base.primary, actual.primary)
            assertEquals(base.secondary, actual.secondary)
            assertEquals(alpha, Color.alpha(actual.floatingSurface))
            assertEquals(expectedSurfaceRgb and 0x00FFFFFF, actual.floatingSurface and 0x00FFFFFF)
            assertEquals(expectedSubProperty, actual.subPropertyText)
        }
    }
}
