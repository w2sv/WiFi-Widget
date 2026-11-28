package com.w2sv.widget.ui.actions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.provider.Settings
import androidx.glance.appwidget.action.RunCallbackAction
import androidx.glance.appwidget.action.StartActivityIntentAction
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.di.GlanceWidgetEntryPoint
import com.w2sv.widget.di.glanceWidgetEntryPoint
import com.w2sv.widget.ui.actions.clipboard.CopyToClipboardActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetActionFactoryTest {
    private val context = mockk<Context> {
        every { packageName } returns "test.app"
    }
    private val destination = ComponentName(context.packageName, "test.app.RecoveryActivity")
    private val entryPoint = mockk<GlanceWidgetEntryPoint> {
        every { locationAccessActivityIntentProvider() } returns LocationAccessActivityIntentProvider { Intent().setComponent(destination) }
    }
    private val locationErrors = listOf(
        WifiPropertyResolutionError.NoLocationAccessPermission,
        WifiPropertyResolutionError.NoBackgroundLocationAccessPermission,
        WifiPropertyResolutionError.GpsDisabled
    )

    @Before
    fun setUp() {
        mockkStatic(::glanceWidgetEntryPoint)
        every { glanceWidgetEntryPoint(any()) } returns entryPoint
    }

    @After
    fun tearDown() {
        unmockkStatic(::glanceWidgetEntryPoint)
    }

    @Test
    fun `location errors recover even for blank values with consistent explicit launch intents`() {
        val intents = locationErrors.map { error ->
            (WidgetActionFactory.property(context, presentation(error, value = "")) as StartActivityIntentAction).intent
        }
        val intent = intents.first()
        assertEquals(destination, intent.component)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP, intent.flags)
        assertEquals("wifiwidget://${context.packageName}/location-access", intent.dataString)
        assertTrue(intents.all(intent::filterEquals))
    }

    @Test
    fun `resolved values copy the displayed label and value while blanks remain inert`() {
        val resolved = presentation().copy(label = SubscriptableText("IP", "Public"))
        val intent = (WidgetActionFactory.property(context, resolved) as StartActivityIntentAction).intent
        assertEquals(CopyToClipboardActivity::class.java.name, intent.component?.className)
        assertEquals(CopyToClipboardActivity.Payload("IP Public", "displayed value"), CopyToClipboardActivity.Payload.fromIntent(intent))

        listOf("", " \t").forEach { value -> assertNull(WidgetActionFactory.property(context, presentation(value = value))) }
    }

    @Test
    fun `other resolution errors remain inert`() {
        WifiPropertyResolutionError.entries.filterNot(locationErrors::contains).forEach { error ->
            assertNull(WidgetActionFactory.property(context, presentation(error)))
        }
    }

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

        val action = WidgetActionFactory.wifiSettings(context) as StartActivityIntentAction

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

        assertNull(WidgetActionFactory.wifiSettings(context))
    }

    @Test fun `refresh action selects the refresh callback`() {
        val action = WidgetActionFactory.refresh() as RunCallbackAction
        assertEquals(RefreshWidgetAction::class.java, action.callbackClass)
    }

    private fun presentation(error: WifiPropertyResolutionError? = null, value: String = "displayed value") =
        WifiPropertyPresentation(SubscriptableText("label"), value, resolutionError = error)
}
