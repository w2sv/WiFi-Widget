package com.w2sv.wifiwidget.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.di.LocationAccessActivityModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import org.robolectric.util.ReflectionHelpers.ClassParameter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = com.w2sv.wifiwidget.Application::class)
class LocationAccessActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<LocationAccessActivity>()

    @Test
    fun `cold start renders existing location access without onboarding navigation`() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.location_access)).assertIsDisplayed()
        assertFalse(composeRule.activity.isFinishing)
        assertEquals(null, shadowOf(composeRule.activity).nextStartedActivity)
    }

    @Test
    fun `manifest configures isolated task affinity and explicit launch intent`() {
        val activity = composeRule.activity
        val provider = LocationAccessActivityModule.provideLocationAccessActivityIntentProvider()
        val intent = provider(activity)
        val info = activity.packageManager.getActivityInfo(checkNotNull(intent.component), 0)
        val mainInfo = activity.packageManager.getActivityInfo(ComponentName(activity, MainActivity::class.java), 0)
        val configInfo = activity.packageManager.getActivityInfo(ComponentName(activity, WidgetConfigActivity::class.java), 0)

        assertEquals(ComponentName(activity, LocationAccessActivity::class.java), intent.component)
        assertEquals(activity.packageName + ".locationaccess", info.taskAffinity)
        assertNotEquals(mainInfo.taskAffinity, info.taskAffinity)
        assertNotEquals(configInfo.taskAffinity, info.taskAffinity)
        assertEquals(ActivityInfo.LAUNCH_MULTIPLE, info.launchMode)
        assertFalse(info.exported)
        assertTrue(info.flags and ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS != 0)
        assertEquals(0, info.flags and ActivityInfo.FLAG_NO_HISTORY)
        assertNotSame(intent, provider(activity))
        assertEquals(0, intent.flags)
        assertNull(intent.data)
        assertNull(intent.action)
    }

    @Test
    fun `toolbar Back closes and removes temporary task`() {
        val activity = composeRule.activity
        composeRule.onNodeWithContentDescription(activity.getString(R.string.navigate_back)).performClick()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun `system Back closes and removes temporary task`() {
        val activity = composeRule.activity
        composeRule.runOnUiThread { activity.onBackPressedDispatcher.onBackPressed() }

        assertTrue(activity.isFinishing)
    }

    @Test
    fun `foreground access keeps flow open for background settings and Done removes the task`() {
        val activity = composeRule.activity
        composeRule.onNodeWithText(activity.getString(R.string.enable_location_access)).performClick()
        val request = checkNotNull(shadowOf(activity).lastRequestedPermission)
        // Consume the runtime-permission activity before inspecting the subsequent Settings launch.
        shadowOf(activity).nextStartedActivity
        assertEquals(
            setOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            request.requestedPermissions.toSet()
        )
        composeRule.runOnUiThread {
            shadowOf(activity.application).grantPermissions(*request.requestedPermissions)
            @Suppress("DEPRECATION")
            activity.onRequestPermissionsResult(
                request.requestCode,
                request.requestedPermissions,
                IntArray(request.requestedPermissions.size) { PackageManager.PERMISSION_GRANTED }
            )
        }
        composeRule.waitForIdle()

        assertFalse(activity.isFinishing)
        composeRule.onNodeWithText(activity.getString(R.string.manage_in_android_settings)).performClick()
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, shadowOf(activity).nextStartedActivity.action)
        grantOnSettingsReturn()

        composeRule.onNodeWithText(activity.getString(R.string.enable_background_updates)).performClick()
        composeRule.onNodeWithText(activity.getString(R.string.continue_action)).performClick()

        assertEquals(
            listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            shadowOf(activity).lastRequestedPermission.requestedPermissions.toList()
        )
        assertFalse(activity.isFinishing)

        grantOnSettingsReturn(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        composeRule.onNodeWithText(activity.getString(R.string.done)).performClick()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun `new widget intent retains existing view models and updates activity intent`() {
        val activity = composeRule.activity
        val store = activity.viewModelStore
        // ActivityScenario tracks lifecycle events by the launch intent's filter identity.
        val nextIntent = Intent(activity.intent).putExtra("repeated_click", true)

        composeRule.runOnUiThread {
            ReflectionHelpers.callInstanceMethod<Unit>(activity, "onNewIntent", ClassParameter.from(Intent::class.java, nextIntent))
        }

        assertSame(nextIntent, activity.intent)
        assertSame(store, activity.viewModelStore)
        assertFalse(activity.isFinishing)
        composeRule.onNodeWithText(activity.getString(R.string.location_access)).assertIsDisplayed()
        assertEquals(null, shadowOf(activity).nextStartedActivity)
    }

    private fun grantOnSettingsReturn(vararg permissions: String) {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        shadowOf(composeRule.activity.application).grantPermissions(*permissions)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()
    }
}
