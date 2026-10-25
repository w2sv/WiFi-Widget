package com.w2sv.widget.ui.properties

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.RemoteViews
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.widget.R
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.widget.ui.container.WidgetRenderer
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CopyPropertyToClipboardActivityTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val clipboard = application.getSystemService(ClipboardManager::class.java)

    @Test
    fun `connected widget attaches the copy template to its property collection`() {
        assertTrue(shadowOf(renderedTemplate()).savedIntent.extras?.isEmpty ?: true)
    }

    @Test
    fun `shared widget template contains no row arguments and repeated sends use current fill-in`() {
        val firstWidgetTemplate = renderedTemplate()
        val secondWidgetTemplate = renderedTemplate()
        val template = shadowOf(firstWidgetTemplate)

        assertEquals(firstWidgetTemplate, secondWidgetTemplate)
        assertEquals(-1, template.requestCode)
        assertFalse(template.isImmutable)
        assertTrue(template.flags and PendingIntent.FLAG_UPDATE_CURRENT != 0)
        assertEquals(CopyPropertyToClipboardActivity::class.java.name, template.savedIntent.component?.className)
        assertTrue(template.savedIntent.extras?.isEmpty ?: true)

        firstWidgetTemplate.send(application, 0, CopyPropertyToClipboardActivity.Property.intent("IP Public", "84.1.2.3"))
        assertEquals(
            CopyPropertyToClipboardActivity.Property("IP Public", "84.1.2.3"),
            CopyPropertyToClipboardActivity.Property.fromIntent(shadowOf(application).nextStartedActivity)
        )
        secondWidgetTemplate.send(application, 0, CopyPropertyToClipboardActivity.Property.intent("SSID", "Home"))
        assertEquals(
            CopyPropertyToClipboardActivity.Property("SSID", "Home"),
            CopyPropertyToClipboardActivity.Property.fromIntent(shadowOf(application).nextStartedActivity)
        )
        assertTrue(template.savedIntent.extras?.isEmpty ?: true)
    }

    @Test
    fun `copy processing uses the latest row value and labels every successful copy`() {
        val copiedLabels = mutableListOf<String>()

        copyPropertyFromIntent(CopyPropertyToClipboardActivity.Property.intent("IP Public", "84.1.2.3"), clipboard) { copiedLabels += it }
        assertEquals("84.1.2.3", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        copyPropertyFromIntent(CopyPropertyToClipboardActivity.Property.intent("SSID", "Home"), clipboard) { copiedLabels += it }
        assertEquals("Home", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        assertEquals(listOf("IP Public", "SSID"), copiedLabels)
    }

    @Test
    fun `missing and malformed arguments leave clipboard and toast untouched`() {
        clipboard.setPrimaryClip(ClipData.newPlainText("previous", "old value"))
        val copiedLabels = mutableListOf<String>()
        val valid = CopyPropertyToClipboardActivity.Property.intent("SSID", "Home")
        val argsKey = valid.extras!!.keySet().single()
        val invalid = listOf(
            Intent(),
            Intent().putExtra(argsKey, "wrong parcelable type")
        )

        invalid.forEach { intent -> copyPropertyFromIntent(intent, clipboard) { copiedLabels += it } }

        assertEquals("old value", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        assertTrue(copiedLabels.isEmpty())
        assertNull(CopyPropertyToClipboardActivity.Property.fromIntent(Intent()))
    }

    @Test
    fun `empty row fields are copied as supplied`() {
        val copiedLabels = mutableListOf<String>()

        copyPropertyFromIntent(CopyPropertyToClipboardActivity.Property.intent("", "Home"), clipboard) { copiedLabels += it }
        assertEquals("Home", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        copyPropertyFromIntent(CopyPropertyToClipboardActivity.Property.intent("SSID", ""), clipboard) { copiedLabels += it }
        assertEquals("", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        assertEquals(listOf("", "SSID"), copiedLabels)
    }

    private fun renderedTemplate(): PendingIntent {
        val views = mockk<RemoteViews>(relaxed = true)
        val widgetManager = mockk<AppWidgetManager>(relaxed = true)
        val statusSource = mockk<WifiStatusSource> {
            every { snapshot() } returns WifiStatus.ConnectedInactive
        }
        val template = slot<PendingIntent>()
        val config = WidgetConfig.default.copy(utilities = WidgetUtility.entries.associateWith { false })

        WidgetRenderer(application, widgetManager, statusSource, Clock.systemUTC())(
            views,
            42,
            config,
            WidgetColors(0, 1, 2)
        )

        verify(exactly = 1) { views.setPendingIntentTemplate(R.id.wifi_property_list_view, capture(template)) }
        return template.captured
    }
}
