package com.w2sv.widget.ui.actions

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.provider.Settings
import androidx.glance.appwidget.action.RunCallbackAction
import androidx.glance.appwidget.action.StartActivityIntentAction
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetUtility
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetActionTest {
    @Test fun `refresh button uses its configured icon description and callback`() {
        val action = WidgetAction.Refresh

        assertEquals(WidgetUtility.RefreshButton, action.configurationKey)
        assertEquals(R.drawable.ic_refresh_24, action.iconRes)
        assertEquals(R.string.refresh_data, action.descriptionRes)
        assertEquals(RefreshWidgetAction::class.java, (action.createAction(mockk<Context>()) as RunCallbackAction).callbackClass)
    }

    @Test fun `Wi-Fi settings button uses its configured icon description and explicit activity`() {
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

        val action = WidgetAction.WifiSettings
        val glanceAction = action.createAction(context) as StartActivityIntentAction

        assertEquals(WidgetUtility.GoToWifiSettingsButton, action.configurationKey)
        assertEquals(R.drawable.ic_wifi_settings_24, action.iconRes)
        assertEquals(R.string.open_wifi_settings_button, action.descriptionRes)
        assertEquals(Settings.ACTION_WIFI_SETTINGS, glanceAction.intent.action)
        assertEquals("com.android.settings.WifiSettingsActivity", glanceAction.intent.component?.className)
    }

    @Test fun `unresolved Wi-Fi settings destination leaves the button without a click action`() {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { packageManager.resolveActivity(any(), PackageManager.MATCH_DEFAULT_ONLY) } returns null

        assertNull(WidgetAction.WifiSettings.createAction(context))
    }
}
