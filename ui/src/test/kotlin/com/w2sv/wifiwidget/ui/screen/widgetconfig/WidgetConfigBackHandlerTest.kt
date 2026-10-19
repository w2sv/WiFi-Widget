package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.w2sv.core.common.R
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetConfigBackHandlerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val isDirty = MutableStateFlow(false)
    private val snackbarHostState = SnackbarHostState()
    private var navigateBackCount = 0
    private var fallbackBackCount = 0

    @Test
    fun `dirty system back warns and second back confirms exit`() {
        assertDirtyBackRequiresConfirmation(::pressSystemBack)
    }

    @Test
    fun `dirty toolbar back warns and second back confirms exit`() {
        assertDirtyBackRequiresConfirmation(::pressToolbarBack)
    }

    @Test
    fun `clean toolbar back navigates back`() {
        setContent()

        pressToolbarBack()

        composeRule.runOnIdle {
            assertEquals(1, navigateBackCount)
            assertNull(snackbarHostState.currentSnackbarData)
        }
    }

    @Test
    fun `clean system back reaches preceding handler and navigates back`() {
        setContent()

        pressSystemBack()

        composeRule.runOnIdle {
            assertEquals(1, fallbackBackCount)
            assertEquals(1, navigateBackCount)
        }
    }

    @Test
    fun `returning to dirty state starts a new confirmation window`() {
        isDirty.value = true
        setContent()
        pressToolbarBack()
        composeRule.waitUntil { snackbarHostState.currentSnackbarData != null }

        composeRule.runOnIdle { isDirty.value = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isDirty.value = true }
        composeRule.waitForIdle()

        pressToolbarBack()

        composeRule.runOnIdle { assertEquals(0, navigateBackCount) }
    }

    private fun assertDirtyBackRequiresConfirmation(pressBack: () -> Unit) {
        isDirty.value = true
        setContent()

        pressBack()

        composeRule.waitUntil { snackbarHostState.currentSnackbarData != null }
        composeRule.runOnIdle {
            assertEquals(0, navigateBackCount)
            assertEquals(0, fallbackBackCount)
            assertEquals(
                composeRule.activity.getString(R.string.go_back_on_unsaved_changes_warning),
                snackbarHostState.currentSnackbarData?.visuals?.message
            )
        }

        pressBack()

        composeRule.runOnIdle {
            assertEquals(1, navigateBackCount)
            assertNull(snackbarHostState.currentSnackbarData)
        }
    }

    private fun setContent() {
        composeRule.setContent {
            CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                val navigateBack: () -> Unit = { navigateBackCount++ }
                BackHandler {
                    fallbackBackCount++
                    navigateBack()
                }
                val onBack = rememberWidgetConfigBackHandler(isDirty, navigateBack = navigateBack)
                Button(onClick = onBack) { Text("Toolbar back") }
            }
        }
    }

    private fun pressSystemBack() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    }

    private fun pressToolbarBack() {
        composeRule.onNodeWithText("Toolbar back").performClick()
    }
}
