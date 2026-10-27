package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import com.w2sv.wifiwidget.ui.util.snackbar.LocalSnackbarVisibility
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarVisibility
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetConfigRouteTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val applied = MutableStateFlow(WidgetConfig.default)
    private var stored = WidgetConfig.default
    private var publishUpdate = true
    private var updateGate: CompletableDeferred<Unit>? = null
    private val updateStarted = CompletableDeferred<Unit>()
    private val dataSource = mockk<WidgetConfigDataSource> {
        every { config } returns applied
        coEvery { update(any()) } coAnswers {
            updateStarted.complete(Unit)
            updateGate?.await()
            stored = firstArg<(WidgetConfig) -> WidgetConfig>()(stored)
            if (publishUpdate) applied.value = stored
        }
    }
    private val configSource = mockk<WidgetConfigSource> {
        coEvery { snapshot() } answers { stored }
    }
    private val actions = mockk<WidgetActions>(relaxed = true)
    private val viewModel = WidgetConfigScreenViewModel(dataSource, configSource, actions)
    private val locationAccess = mockk<LocationAccessCapability>(relaxed = true) {
        every { grantEvents } returns emptyFlow()
        every { foregroundPermissionsGranted } returns true
        every { isLocationEnabled } returns true
    }
    private val snackbarHost = SnackbarHostState()
    private val snackbarVisibility = SnackbarVisibility()
    private var closeCount = 0
    private var backCount = 0

    @Test fun `in app screen has Back arrow without Close or Done`() {
        setContent(launcher = false)

        composeRule.onNodeWithContentDescription(back).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(close).assertDoesNotExist()
        composeRule.onNodeWithText("Done").assertDoesNotExist()
        composeRule.onNodeWithContentDescription(back).performClick()
        assertEquals(1, backCount)
    }

    @Test fun `launcher screen has Close icon without Back or Done`() {
        setContent(launcher = true)

        composeRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(back).assertDoesNotExist()
        composeRule.onNodeWithText("Done").assertDoesNotExist()
    }

    @Test fun `launcher Close without edits completes without persistence`() {
        stored = nonDefaultConfig()
        applied.value = stored
        setContent(launcher = true)

        composeRule.onNodeWithContentDescription(close).performClick()

        assertEquals(1, closeCount)
        assertEquals(nonDefaultConfig(), stored)
        coVerify(exactly = 0) { dataSource.update(any()) }
    }

    @Test fun `launcher Close with unsaved edits warns and discards them`() {
        setContent(launcher = true)
        editConfig(nonDefaultConfig())

        composeRule.onNodeWithContentDescription(close).performClick()
        composeRule.waitUntil { snackbarHost.currentSnackbarData != null }
        assertEquals(0, closeCount)
        composeRule.onNodeWithContentDescription(close).performClick()

        assertEquals(1, closeCount)
        assertEquals(WidgetConfig.default, stored)
        coVerify(exactly = 0) { dataSource.update(any()) }
    }

    @Test fun `launcher system Back uses the same unsaved changes confirmation`() {
        setContent(launcher = true)
        editConfig(nonDefaultConfig())

        pressSystemBack()
        composeRule.waitUntil { snackbarHost.currentSnackbarData != null }
        assertEquals(0, closeCount)
        pressSystemBack()

        assertEquals(1, closeCount)
        assertEquals(WidgetConfig.default, stored)
        coVerify(exactly = 0) { dataSource.update(any()) }
    }

    @Test fun `launcher system Back closes clean configuration`() {
        setContent(launcher = true)

        pressSystemBack()

        assertEquals(1, closeCount)
        coVerify(exactly = 0) { dataSource.update(any()) }
    }

    @Test fun `Apply saves without closing and Close completes afterward`() {
        setContent(launcher = true)
        editConfig(nonDefaultConfig())

        composeRule.onNodeWithText(apply).performClick()
        composeRule.waitUntil { stored == nonDefaultConfig() }
        composeRule.waitUntil { snackbarHost.currentSnackbarData != null }
        composeRule.onNodeWithText(apply).assertDoesNotExist()
        assertEquals(0, closeCount)
        composeRule.onNodeWithContentDescription(close).performClick()

        assertEquals(1, closeCount)
        coVerify(exactly = 1) { dataSource.update(any()) }
        verify(exactly = 1) { actions.applyConfigurationChange(any(), any()) }
    }

    @Test fun `Reset reverts without persisting or leaving`() {
        setContent(launcher = false)
        editConfig(nonDefaultConfig())

        composeRule.onNodeWithText(reset).performClick()
        composeRule.waitUntil { !applyIsDisplayed() }

        assertEquals(WidgetConfig.default, viewModel.reversibleConfig.value)
        assertEquals(0, backCount)
        coVerify(exactly = 0) { dataSource.update(any()) }
    }

    @Test fun `successful Apply waits for stale dirty state before showing success snackbar`() {
        publishUpdate = false
        setContent(launcher = false)
        editConfig(nonDefaultConfig())

        composeRule.onNodeWithText(apply).performClick()
        composeRule.waitUntil { stored == nonDefaultConfig() }
        composeRule.waitForIdle()
        assertEquals(null, snackbarHost.currentSnackbarData)
        composeRule.onNodeWithText(apply).assertIsDisplayed()

        composeRule.runOnIdle { applied.value = stored }
        composeRule.waitUntil { !applyIsDisplayed() }
        composeRule.waitUntil { snackbarHost.currentSnackbarData != null }
        assertEquals(
            composeRule.activity.getString(R.string.updated_widget_configuration),
            snackbarHost.currentSnackbarData?.visuals?.message
        )
    }

    @Test fun `new edit during pending Apply remains visible after first commit`() {
        updateGate = CompletableDeferred()
        setContent(launcher = false)
        editConfig(nonDefaultConfig())

        composeRule.onNodeWithText(apply).performClick()
        composeRule.waitUntil { updateStarted.isCompleted }
        val newer = WidgetConfig.default.copy(appearance = WidgetConfig.default.appearance.copy(backgroundOpacity = 0.7f))
        editConfig(newer)
        updateGate?.complete(Unit)
        composeRule.waitUntil { stored == nonDefaultConfig() }
        composeRule.waitForIdle()

        assertEquals(newer, viewModel.reversibleConfig.value)
        composeRule.onNodeWithText(apply).assertIsDisplayed()
    }

    @Test fun `repeated Apply requests serialize and persist once`() {
        updateGate = CompletableDeferred()
        setContent(launcher = false)
        editConfig(nonDefaultConfig())

        composeRule.onNodeWithText(apply).performClick()
        composeRule.waitUntil { updateStarted.isCompleted }
        composeRule.onNodeWithText(apply).performClick()
        updateGate?.complete(Unit)
        composeRule.waitUntil { stored == nonDefaultConfig() }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { dataSource.update(any()) }
        verify(exactly = 1) { actions.applyConfigurationChange(any(), any()) }
    }

    @Test fun `failed Apply shows no success snackbar`() {
        coEvery { dataSource.update(any()) } throws IllegalStateException("write failed")
        setContent(launcher = false)
        editConfig(nonDefaultConfig())

        composeRule.onNodeWithText(apply).performClick()
        composeRule.waitForIdle()

        assertEquals(WidgetConfig.default, stored)
        assertEquals(null, snackbarHost.currentSnackbarData)
        composeRule.onNodeWithText(apply).assertIsDisplayed()
        verify(exactly = 0) { actions.applyConfigurationChange(any(), any()) }
    }

    private fun setContent(launcher: Boolean) {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalSnackbarHostState provides snackbarHost,
                LocalSnackbarVisibility provides snackbarVisibility
            ) {
                WidgetConfigRoute(
                    navigateBack = { backCount++ },
                    completeConfiguration = if (launcher) ({ closeCount++ }) else null,
                    locationAccessCapability = locationAccess,
                    viewModel = viewModel
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun editConfig(config: WidgetConfig) {
        composeRule.runOnIdle { viewModel.reversibleConfig.value = config }
        composeRule.waitUntil { viewModel.reversibleConfig.isDirty.value }
        composeRule.waitUntil(5_000) { applyIsDisplayed() }
        composeRule.onNodeWithText(apply).assertIsDisplayed()
    }

    private fun applyIsDisplayed(): Boolean =
        composeRule.onAllNodesWithText(apply).fetchSemanticsNodes().isNotEmpty()

    private fun pressSystemBack() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
    }

    private fun nonDefaultConfig(): WidgetConfig =
        WidgetConfig.default.copy(appearance = WidgetConfig.default.appearance.copy(backgroundOpacity = 0.5f))

    private val back get() = composeRule.activity.getString(R.string.navigate_back)
    private val close get() = composeRule.activity.getString(R.string.close)
    private val apply get() = composeRule.activity.getString(R.string.apply)
    private val reset get() = composeRule.activity.getString(R.string.reset)
}
