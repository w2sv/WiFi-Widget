package com.w2sv.widget.ui.actions

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.ui.content.copyIntent
import com.w2sv.widget.ui.state.WidgetPropertyRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CopyPropertyToClipboardActivityTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val clipboard = application.getSystemService(ClipboardManager::class.java)

    @Test fun `displayed values and copy intents share the same row snapshot`() {
        val first = row(1, "IP", "Public", "84.1.2.3")
        val changed = row(1, "IP", "Public", "84.1.2.4")
        val another = row(2, "SSID", null, "Home")
        val duplicate = row(3, "IP", "Public", "84.1.2.3")

        assertEquals("84.1.2.3", CopyPropertyToClipboardActivity.Property.fromIntent(first.presentation.copyIntent(application)!!)?.value)
        assertEquals("84.1.2.4", CopyPropertyToClipboardActivity.Property.fromIntent(changed.presentation.copyIntent(application)!!)?.value)
        assertEquals(
            "IP Public",
            CopyPropertyToClipboardActivity.Property.fromIntent(changed.presentation.copyIntent(application)!!)?.label
        )
        assertEquals("Home", CopyPropertyToClipboardActivity.Property.fromIntent(another.presentation.copyIntent(application)!!)?.value)
        assertEquals(
            "84.1.2.3",
            CopyPropertyToClipboardActivity.Property.fromIntent(duplicate.presentation.copyIntent(application)!!)?.value
        )
    }

    @Test fun `resolution errors never create a copy action`() {
        WifiPropertyResolutionError.entries.forEach { error ->
            val row = row(1, "IP", "Public", "Unavailable").copy(
                presentation = row(1, "IP", "Public", "Unavailable").presentation.copy(resolutionError = error)
            )
            assertNull(row.presentation.copyIntent(application))
        }
    }

    @Test fun `empty values do not create copy actions`() {
        assertNull(row(1, "SSID", null, " ").presentation.copyIntent(application))
    }

    @Test fun `copy processing uses current row value and confirms every successful copy`() {
        val copiedLabels = mutableListOf<String>()
        copyPropertyFromIntent(
            CopyPropertyToClipboardActivity.Property.intent(
                "IP Public",
                "84.1.2.3"
            ),
            clipboard
        ) { copiedLabels += it }
        assertEquals("84.1.2.3", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        copyPropertyFromIntent(
            CopyPropertyToClipboardActivity.Property.intent("SSID", "Home"),
            clipboard
        ) { copiedLabels += it }
        assertEquals("Home", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        assertEquals(listOf("IP Public", "SSID"), copiedLabels)
    }

    @Test fun `missing and malformed arguments leave clipboard and toast untouched`() {
        clipboard.setPrimaryClip(ClipData.newPlainText("previous", "old value"))
        val copiedLabels = mutableListOf<String>()
        val key = CopyPropertyToClipboardActivity.Property.intent("SSID", "Home").extras!!.keySet().single()
        listOf(
            Intent(),
            Intent().putExtra(key, "wrong parcelable type"),
            CopyPropertyToClipboardActivity.Property.intent("SSID", " "),
            CopyPropertyToClipboardActivity.Property.intent(" ", "Home")
        ).forEach { intent ->
            copyPropertyFromIntent(intent, clipboard) { copiedLabels += it }
        }
        assertEquals("old value", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        assertTrue(copiedLabels.isEmpty())
    }

    private fun row(
        id: Long,
        label: String,
        subscript: String?,
        value: String
    ) =
        WidgetPropertyRow(id, WifiPropertyPresentation(SubscriptableText(label, subscript), value))
}
