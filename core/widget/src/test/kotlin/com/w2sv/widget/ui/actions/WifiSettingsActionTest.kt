package com.w2sv.widget.ui.actions

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.provider.Settings
import androidx.glance.appwidget.action.StartActivityIntentAction
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiSettingsActionTest {
    @Test fun `Wi-Fi settings action explicitly targets the resolved activity`() {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { packageManager.resolveActivity(any(), PackageManager.MATCH_DEFAULT_ONLY) } returns ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                applicationInfo = ApplicationInfo().apply { packageName = "com.android.settings" }
                packageName = "com.android.settings"
                name = "com.android.settings.WifiSettingsActivity"
            }
        }

        val action = wifiSettingsAction(context) as StartActivityIntentAction

        assertEquals(Settings.ACTION_WIFI_SETTINGS, action.intent.action)
        assertEquals("com.android.settings", action.intent.component?.packageName)
        assertEquals("com.android.settings.WifiSettingsActivity", action.intent.component?.className)
        assertTrue(action.intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test fun `unresolved settings destination has no activity action`() {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { packageManager.resolveActivity(any(), PackageManager.MATCH_DEFAULT_ONLY) } returns null

        assertNull(wifiSettingsAction(context))
    }
}
