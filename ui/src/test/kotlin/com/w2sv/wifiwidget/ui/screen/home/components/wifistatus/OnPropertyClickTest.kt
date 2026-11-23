package com.w2sv.wifiwidget.ui.screen.home.components.wifistatus

import android.content.ClipboardManager
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.w2sv.composed.material3.rememberSnackbarController
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class OnPropertyClickTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val snackbarHost = SnackbarHostState()
    private lateinit var clickScope: PropertyOnClickScope
    private lateinit var scope: CoroutineScope

    @Test
    @Config(sdk = [32])
    fun `Android 12L copies values and replaces the custom success snackbar`() {
        setContent()
        copy("SSID", "Home")
        assertClipboard("SSID", "Home")
        assertSuccessSnackbar("SSID")

        copy("BSSID", "00:11:22:33:44:55")
        assertClipboard("BSSID", "00:11:22:33:44:55")
        assertSuccessSnackbar("BSSID")
    }

    @Test
    @Config(sdk = [33])
    fun `Android 13 copies values without showing or replacing a snackbar`() {
        setContent()
        copy("SSID", "Home")
        assertClipboard("SSID", "Home")
        composeRule.runOnIdle { assertNull(snackbarHost.currentSnackbarData) }

        val existing = AppSnackbarVisuals("Existing message")
        composeRule.runOnIdle {
            scope.launch { snackbarHost.showSnackbar(existing) }
        }
        composeRule.waitForIdle()
        copy("BSSID", "00:11:22:33:44:55")
        assertClipboard("BSSID", "00:11:22:33:44:55")
        composeRule.runOnIdle { assertSame(existing, snackbarHost.currentSnackbarData?.visuals) }
    }

    private fun setContent() {
        composeRule.setContent {
            scope = rememberCoroutineScope()
            clickScope = PropertyOnClickScope(
                clipboard = LocalClipboard.current,
                snackbarController = rememberSnackbarController(snackbarHost),
                locationAccessCapability = mockk(),
                onMissingLocationAccess = { error("Resolved properties must copy") }
            )
        }
    }

    private fun copy(label: String, value: String) {
        composeRule.runOnIdle {
            clickScope.onPropertyClick(scope, label, value, resolutionError = null)
        }
        composeRule.waitForIdle()
    }

    private fun assertClipboard(label: String, value: String) {
        val clip = composeRule.activity.getSystemService(ClipboardManager::class.java).primaryClip
        assertEquals(label, clip?.description?.label)
        assertEquals(value, clip?.getItemAt(0)?.text.toString())
    }

    private fun assertSuccessSnackbar(label: String) {
        composeRule.runOnIdle {
            val visuals = snackbarHost.currentSnackbarData?.visuals as AppSnackbarVisuals
            assertEquals(SnackbarKind.Success, visuals.kind)
            val message = visuals.msg as AnnotatedString
            val context = composeRule.activity
            assertEquals("${context.getString(R.string.copied)} $label ${context.getString(R.string.to_clipboard)}.", message.text)
            val labelStyle = message.spanStyles.single()
            assertEquals(FontWeight.SemiBold, labelStyle.item.fontWeight)
            assertEquals(label, message.text.substring(labelStyle.start, labelStyle.end))
        }
    }
}
