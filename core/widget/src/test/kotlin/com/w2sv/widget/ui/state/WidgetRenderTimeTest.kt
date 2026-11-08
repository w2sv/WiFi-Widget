package com.w2sv.widget.ui.state

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS")
class WidgetRenderTimeTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val date = Date.from(Instant.parse("2026-08-16T22:30:00Z"))
    private val timeZone = TimeZone.getTimeZone("UTC")

    @Test fun `compact 12-hour time retains the period`() {
        Settings.System.putString(context.contentResolver, Settings.System.TIME_12_24, "12")

        val (full, compact) = FormattedRenderTime(context, date, timeZone)

        assertTrue(compact.contains("PM"))
        assertTrue(full.startsWith(compact))
        assertTrue(full.length > compact.length)
    }

    @Test fun `compact 24-hour time follows the device preference`() {
        Settings.System.putString(context.contentResolver, Settings.System.TIME_12_24, "24")

        val (full, compact) = FormattedRenderTime(context, date, timeZone)

        assertEquals("22:30", compact)
        assertTrue(full.startsWith(compact))
    }

    @Test fun `weekday follows the context locale`() {
        Settings.System.putString(context.contentResolver, Settings.System.TIME_12_24, "24")
        val configuration = Configuration(context.resources.configuration).apply { setLocale(Locale.GERMAN) }
        val localizedContext = context.createConfigurationContext(configuration)

        val (full, compact) = FormattedRenderTime(localizedContext, date, timeZone)

        val expectedDay = SimpleDateFormat("EEE", Locale.GERMAN)
            .apply { timeZone = this@WidgetRenderTimeTest.timeZone }
            .format(date)
        assertTrue(full.endsWith(expectedDay))
        assertTrue(full.startsWith(compact))
    }
}
