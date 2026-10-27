package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarHost
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.util.snackbar.LocalSnackbarVisibility
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarVisibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EditingFabButtonRowTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val dirty = MutableStateFlow(false)
    private val editable = MutableStateFlow(WidgetConfig.default)
    private val firstEdit = WidgetConfig.default.copy(
        appearance = WidgetConfig.default.appearance.copy(backgroundOpacity = 0.5f)
    )
    private val committed = MutableSharedFlow<WidgetConfig>(extraBufferCapacity = 1)
    private val snackbarHost = SnackbarHostState()
    private val snackbarVisibility = SnackbarVisibility()
    private lateinit var scope: CoroutineScope
    private var applyCount = 0

    private fun setContent() {
        composeRule.setContent {
            scope = rememberCoroutineScope()
            val isDirty by dirty.collectAsStateWithLifecycle()
            val currentConfig by editable.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalSnackbarHostState provides snackbarHost,
                LocalSnackbarVisibility provides snackbarVisibility
            ) {
                Scaffold(
                    snackbarHost = { AppSnackbarHost() },
                    floatingActionButton = {
                        EditingFabButtonRow(
                            configEditState = ConfigEditState(
                                isDirty = { isDirty },
                                editableConfig = { currentConfig },
                                revertChanges = { dirty.value = false },
                                commitChanges = { applyCount++ },
                                changesHaveBeenCommitted = committed
                            ),
                            modifier = Modifier.padding(top = 8.dp).height(70.dp)
                        )
                    },
                    contentWindowInsets = WindowInsets()
                ) { paddingValues ->
                    Box(Modifier.padding(paddingValues))
                }
            }
        }
    }

    @Test fun `editing shows FABs and reverting hides them`() {
        setContent()
        composeRule.onNodeWithText(apply).assertDoesNotExist()

        composeRule.runOnIdle {
            editable.value = firstEdit
            dirty.value = true
        }
        composeRule.waitUntil { applyIsDisplayed() }
        composeRule.onNodeWithText(reset).performClick()
        composeRule.waitUntil { !applyIsDisplayed() }
    }

    @Test fun `applying hides FABs after configuration becomes clean`() {
        setContent()
        composeRule.runOnIdle {
            editable.value = firstEdit
            dirty.value = true
        }
        composeRule.waitUntil { applyIsDisplayed() }
        composeRule.onNodeWithText(apply).performClick()
        composeRule.mainClock.autoAdvance = false
        composeRule.runOnIdle {
            assertEquals(1, applyCount)
            committed.tryEmit(editable.value)
        }
        // The applied-state dirty flag can lag behind the successful commit event.
        composeRule.mainClock.advanceTimeByFrame()
        assertEquals(null, snackbarHost.currentSnackbarData)
        composeRule.runOnIdle { dirty.value = false }
        composeRule.mainClock.advanceTimeByFrame()
        assertEquals(null, snackbarHost.currentSnackbarData)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitUntil { !applyIsDisplayed() }
        composeRule.waitUntil { snackbarHost.currentSnackbarData != null }
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.updated_widget_configuration)).assertIsDisplayed()
        composeRule.onNodeWithText(apply).assertDoesNotExist()
    }

    @Test fun `new edit during pending Apply keeps FABs visible after commit event`() {
        setContent()
        composeRule.runOnIdle {
            editable.value = firstEdit
            dirty.value = true
        }
        composeRule.waitUntil { applyIsDisplayed() }
        composeRule.onNodeWithText(apply).performClick()
        composeRule.runOnIdle {
            editable.value = WidgetConfig.default.copy(
                appearance = WidgetConfig.default.appearance.copy(backgroundOpacity = 0.7f)
            )
            committed.tryEmit(firstEdit)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(apply).assertIsDisplayed()
        assertEquals(null, snackbarHost.currentSnackbarData)
    }

    @Test fun `editing during snackbar dismisses it before showing FABs`() {
        setContent()
        showIndefiniteSnackbar()
        composeRule.waitUntil { snackbarVisibility.isVisible }
        assertEquals(SnackbarKind.Success, snackbarVisibility.kind)
        composeRule.onNodeWithText("Applied").assertIsDisplayed()
        composeRule.onNodeWithText(apply).assertDoesNotExist()

        composeRule.mainClock.autoAdvance = false
        composeRule.runOnIdle {
            editable.value = firstEdit
            dirty.value = true
        }
        composeRule.mainClock.advanceTimeByFrame()
        assertEquals(true, snackbarVisibility.isVisible)
        assertEquals(SnackbarKind.Success, snackbarVisibility.kind)
        composeRule.onNodeWithText(apply).assertDoesNotExist()
        composeRule.mainClock.autoAdvance = true
        composeRule.waitUntil(5_000) { snackbarHost.currentSnackbarData == null }
        composeRule.waitUntil(5_000) { !snackbarVisibility.isVisible }
        composeRule.waitUntil(5_000) { applyIsDisplayed() }
        composeRule.onNodeWithText(apply).assertIsDisplayed()
    }

    @Test fun `edit during snackbar exit does not start FAB entrance early`() {
        setContent()
        showIndefiniteSnackbar(kind = null)
        composeRule.waitUntil { snackbarVisibility.isVisible }

        composeRule.mainClock.autoAdvance = false
        composeRule.runOnIdle { snackbarHost.currentSnackbarData?.dismiss() }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle {
            editable.value = firstEdit
            dirty.value = true
        }
        composeRule.mainClock.advanceTimeByFrame()

        assertEquals(null, snackbarHost.currentSnackbarData)
        assertEquals(true, snackbarVisibility.isVisible)
        assertEquals(null, snackbarVisibility.kind)
        composeRule.onNodeWithText(apply).assertDoesNotExist()

        composeRule.mainClock.autoAdvance = true
        composeRule.waitUntil(5_000) { !snackbarVisibility.isVisible }
        composeRule.waitUntil(5_000) { applyIsDisplayed() }
        composeRule.onNodeWithText(apply).assertIsDisplayed()
    }

    @Test fun `warning snackbar stays above visible FABs through its exit animation`() {
        setContent()
        composeRule.runOnIdle {
            editable.value = firstEdit
            dirty.value = true
        }
        composeRule.waitUntil { applyIsDisplayed() }
        composeRule.onNodeWithText(apply).assertIsDisplayed()
        composeRule.onNodeWithText(reset).assertIsDisplayed()

        showIndefiniteSnackbar(kind = SnackbarKind.Warning, message = "Unsaved changes")
        composeRule.waitUntil { snackbarVisibility.isVisible }
        assertEquals(SnackbarKind.Warning, snackbarVisibility.kind)
        composeRule.onNodeWithText("Unsaved changes").assertIsDisplayed()
        composeRule.onNodeWithText(apply).assertIsDisplayed()
        composeRule.onNodeWithText(reset).assertIsDisplayed()
        val warningBottom = composeRule.onNodeWithText("Unsaved changes").getUnclippedBoundsInRoot().bottom
        val applyTop = composeRule.onNodeWithText(apply).getUnclippedBoundsInRoot().top
        assertTrue(warningBottom <= applyTop)

        composeRule.mainClock.autoAdvance = false
        composeRule.runOnIdle { snackbarHost.currentSnackbarData?.dismiss() }
        composeRule.mainClock.advanceTimeByFrame()

        assertEquals(null, snackbarHost.currentSnackbarData)
        assertEquals(true, snackbarVisibility.isVisible)
        assertEquals(SnackbarKind.Warning, snackbarVisibility.kind)
        composeRule.onNodeWithText(apply).assertIsDisplayed()
        composeRule.onNodeWithText(reset).assertIsDisplayed()

        composeRule.mainClock.autoAdvance = true
        composeRule.waitUntil(5_000) { !snackbarVisibility.isVisible }
        assertEquals(null, snackbarVisibility.kind)
        composeRule.onNodeWithText(apply).assertIsDisplayed()
        composeRule.onNodeWithText(reset).assertIsDisplayed()
    }

    private fun showIndefiniteSnackbar(kind: SnackbarKind? = SnackbarKind.Success, message: String = "Applied") {
        composeRule.runOnIdle {
            scope.launch {
                snackbarHost.showSnackbar(
                    AppSnackbarVisuals(message, duration = SnackbarDuration.Indefinite, kind = kind)
                )
            }
        }
    }

    private fun applyIsDisplayed(): Boolean =
        composeRule.onAllNodesWithText(apply).fetchSemanticsNodes().isNotEmpty()

    private val apply get() = composeRule.activity.getString(R.string.apply)
    private val reset get() = composeRule.activity.getString(R.string.reset)
}
