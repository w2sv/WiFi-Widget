package com.w2sv.widget.ui.actions.clipboard

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 32, 33, 35])
class CopyToClipboardActivityTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val clipboard = application.getSystemService(ClipboardManager::class.java)

    @Test fun `copy processing uses current row value and confirms only before Android 13`() {
        val copiedLabels = mutableListOf<String>()
        copyToClipboardFromIntent(
            CopyToClipboardActivity.Payload.intent(
                "IP Public",
                "84.1.2.3"
            ),
            clipboard
        ) { copiedLabels += it }
        assertEquals("84.1.2.3", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        assertEquals("IP Public", clipboard.primaryClip?.description?.label)
        copyToClipboardFromIntent(
            CopyToClipboardActivity.Payload.intent("SSID", "Home"),
            clipboard
        ) { copiedLabels += it }
        assertEquals("Home", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        assertEquals("SSID", clipboard.primaryClip?.description?.label)
        val expectedConfirmations = if (Build.VERSION.SDK_INT < 33) listOf("IP Public", "SSID") else emptyList()
        assertEquals(expectedConfirmations, copiedLabels)
    }

    @Test fun `missing and malformed arguments leave clipboard and toast untouched`() {
        clipboard.setPrimaryClip(ClipData.newPlainText("previous", "old value"))
        val copiedLabels = mutableListOf<String>()
        val key = CopyToClipboardActivity.Payload.intent("SSID", "Home").extras!!.keySet().single()
        listOf(
            Intent(),
            Intent().putExtra(key, "wrong parcelable type"),
            CopyToClipboardActivity.Payload.intent("SSID", " "),
            CopyToClipboardActivity.Payload.intent(" ", "Home")
        ).forEach { intent ->
            copyToClipboardFromIntent(intent, clipboard) { copiedLabels += it }
        }
        assertEquals("old value", clipboard.primaryClip?.getItemAt(0)?.text.toString())
        assertTrue(copiedLabels.isEmpty())
    }

    @Test fun `manifest resolves the renamed clipboard activity with its existing task isolation`() {
        val info = application.packageManager.getActivityInfo(ComponentName(application, CopyToClipboardActivity::class.java), 0)

        assertEquals(CopyToClipboardActivity::class.java.name, info.name)
        assertEquals("com.w2sv.wifiwidget.taskaffinity.CopyPropertyToClipboard", info.taskAffinity)
        assertTrue(info.flags and ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS != 0)
        assertTrue(info.flags and ActivityInfo.FLAG_NO_HISTORY != 0)
        assertEquals(android.R.style.Theme_Translucent_NoTitleBar, info.theme)
    }
}
