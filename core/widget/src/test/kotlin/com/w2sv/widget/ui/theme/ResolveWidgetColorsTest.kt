package com.w2sv.widget.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.widget.R
import com.w2sv.domain.preferences.Theme
import com.w2sv.domain.widget.WidgetColoring
import com.w2sv.domain.widget.WidgetColoringStrategy
import com.w2sv.domain.widget.WidgetColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ResolveWidgetColorsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun `custom colors pass through unchanged`() {
        val colors = WidgetColors(0x80112233.toInt(), 0xFF445566.toInt(), 0xFF778899.toInt())
        val coloring = WidgetColoring(custom = WidgetColoringStrategy.Custom(colors), useCustom = true)

        assertSame(colors, coloring.resolve(context))
    }

    @Test fun `explicit light and dark static presets use existing resources regardless of system appearance`() {
        val night = context.withNightMode(true)
        val day = context.withNightMode(false)

        assertEquals(staticColors(day, false), preset(Theme.Light, false).resolve(night))
        assertEquals(staticColors(night, true), preset(Theme.Dark, false).resolve(day))
    }

    @Test fun `default static preset follows system appearance`() {
        assertEquals(staticColors(context, false), preset(Theme.Default, false).resolve(context.withNightMode(false)))
        assertEquals(staticColors(context, true), preset(Theme.Default, false).resolve(context.withNightMode(true)))
    }

    @Test fun `dynamic preset maps the selected Compose light and dark schemes`() {
        val night = context.withNightMode(true)
        val day = context.withNightMode(false)

        assertEquals(dynamicColors(day, false), preset(Theme.Light, true).resolve(night))
        assertEquals(dynamicColors(night, true), preset(Theme.Dark, true).resolve(day))
        assertEquals(dynamicColors(day, false), preset(Theme.Default, true).resolve(day))
        assertEquals(dynamicColors(night, true), preset(Theme.Default, true).resolve(night))
    }

    @Test
    @Config(sdk = [30])
    fun `dynamic preset falls back to static colors before API 31`() {
        assertEquals(staticColors(context, false), preset(Theme.Light, true).resolve(context))
        assertEquals(staticColors(context, true), preset(Theme.Dark, true).resolve(context))
    }

    private fun preset(theme: Theme, dynamic: Boolean) =
        WidgetColoring(preset = WidgetColoringStrategy.Preset(theme, dynamic))

    private fun Context.withNightMode(night: Boolean): Context {
        val configuration = Configuration(resources.configuration)
        configuration.uiMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
            if (night) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        return createConfigurationContext(configuration)
    }

    private fun staticColors(context: Context, dark: Boolean): WidgetColors =
        if (dark) {
            WidgetColors(
                context.getColor(R.color.background_dark),
                context.getColor(R.color.default_label),
                context.getColor(R.color.foreground_dark)
            )
        } else {
            WidgetColors(
                context.getColor(R.color.background_light),
                context.getColor(R.color.label_light),
                context.getColor(R.color.foreground_light)
            )
        }

    private fun dynamicColors(context: Context, dark: Boolean): WidgetColors {
        val scheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        return WidgetColors(scheme.surface.toArgb(), scheme.primary.toArgb(), scheme.onSurface.toArgb())
    }
}
