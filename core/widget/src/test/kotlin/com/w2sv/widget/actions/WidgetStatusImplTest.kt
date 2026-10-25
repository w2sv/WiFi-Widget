package com.w2sv.widget.actions

import android.appwidget.AppWidgetManager
import android.content.Context
import com.w2sv.widget.status.WidgetStatusImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetStatusImplTest {
    private val context = mockk<Context> {
        every { packageName } returns "com.w2sv.widget"
    }
    private var installedIds = intArrayOf()
    private val manager = mockk<AppWidgetManager> {
        every { getAppWidgetIds(any()) } answers { installedIds }
    }
    private val pinSuccesses = MutableSharedFlow<Unit>()
    private val status = WidgetStatusImpl(manager, context, pinSuccesses)

    @Test fun `installed widget state reflects host changes without caching IDs`() {
        assertFalse(status.hasInstalledWidgets)

        installedIds = intArrayOf(3, 7)
        assertArrayEquals(installedIds, status.ids)
        assertTrue(status.hasInstalledWidgets)

        installedIds = intArrayOf()
        assertFalse(status.hasInstalledWidgets)
        verify(exactly = 4) { manager.getAppWidgetIds(any()) }
    }

    @Test fun `pin successes expose the supplied event stream`() {
        assertSame(pinSuccesses, status.pinSuccesses)
    }
}
