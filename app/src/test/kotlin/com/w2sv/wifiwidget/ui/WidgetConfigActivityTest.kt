package com.w2sv.wifiwidget.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.widget.WidgetActions
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Checks the launcher result contract without changing the Activity's private lifecycle helpers. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = com.w2sv.wifiwidget.Application::class)
class WidgetConfigActivityTest {

    @Test
    fun `missing widget ID cancels and finishes before showing configuration`() {
        val activity = Robolectric.buildActivity(WidgetConfigActivity::class.java, widgetIntent()).create().get()

        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
        assertEquals(
            AppWidgetManager.INVALID_APPWIDGET_ID,
            shadowOf(activity).resultIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        )
    }

    @Test
    fun `invalid widget ID cancels and finishes`() {
        val activity = Robolectric.buildActivity(
            WidgetConfigActivity::class.java,
            widgetIntent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        ).create().get()

        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
    }

    @Test
    fun `normal launcher Close requests update and returns success with the supplied widget ID`() {
        val widgetId = 42
        val activity = Robolectric.buildActivity(
            WidgetConfigActivity::class.java,
            widgetIntent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        ).create().get()
        val actions = mockk<WidgetActions>(relaxed = true)
        activity.widgetActions = actions

        val complete = WidgetConfigActivity::class.java.getDeclaredMethod("completeWidgetConfiguration", Int::class.javaPrimitiveType)
        complete.isAccessible = true
        complete.invoke(activity, widgetId)

        verify(exactly = 1) { actions.requestUpdate(widgetId) }
        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_OK, shadowOf(activity).resultCode)
        assertEquals(widgetId, shadowOf(activity).resultIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0))
    }

    private fun widgetIntent(): Intent =
        Intent(ApplicationProvider.getApplicationContext(), WidgetConfigActivity::class.java)
}
