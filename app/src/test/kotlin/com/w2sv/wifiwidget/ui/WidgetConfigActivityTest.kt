package com.w2sv.wifiwidget.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.wifiwidget.ui.screen.widgetconfig.WidgetConfigScreenViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Checks activity-owned configuration state and the launcher result contract. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = com.w2sv.wifiwidget.Application::class)
class WidgetConfigActivityTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun `configuration edits belong to the activity ViewModel and survive recreation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = widgetIntent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 42)
        val factory = viewModelFactory {
            initializer<WidgetConfigScreenViewModel> { error("Configuration ViewModel must already belong to the activity") }
        }
        ActivityScenario.launch<WidgetConfigActivity>(intent).use { scenario ->
            composeRule.waitForIdle()
            lateinit var viewModel: WidgetConfigScreenViewModel
            scenario.onActivity { activity ->
                viewModel = ViewModelProvider(activity, factory)[WidgetConfigScreenViewModel::class.java]
            }
            val initialOpacity = viewModel.reversibleConfig.value.appearance.backgroundOpacity
            val editedOpacity = if (initialOpacity == 0.5f) 0.6f else 0.5f
            composeRule.onNodeWithContentDescription(context.getString(R.string.background_opacity))
                .performSemanticsAction(SemanticsActions.SetProgress) { it(editedOpacity) }
            composeRule.waitUntil { viewModel.reversibleConfig.isDirty.value }

            scenario.recreate()

            scenario.onActivity { activity ->
                val restored = ViewModelProvider(activity, factory)[WidgetConfigScreenViewModel::class.java]
                assertSame(viewModel, restored)
                assertTrue(restored.reversibleConfig.isDirty.value)
                assertEquals(editedOpacity, restored.reversibleConfig.value.appearance.backgroundOpacity)
            }
            composeRule.onNodeWithContentDescription(context.getString(R.string.background_opacity))
                .assertRangeInfoEquals(ProgressBarRangeInfo(editedOpacity, 0f..1f, steps = 9))
        }
    }

    @Test
    fun `missing widget ID cancels and finishes before showing configuration`() {
        val activity = Robolectric.buildActivity(WidgetConfigActivity::class.java, widgetIntent()).create().get()

        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
        assertEquals(
            AppWidgetManager.INVALID_APPWIDGET_ID,
            shadowOf(activity).resultIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        )
    }

    @Test
    fun `invalid widget ID cancels and finishes`() {
        val activity = Robolectric.buildActivity(
            WidgetConfigActivity::class.java,
            widgetIntent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        ).create().get()

        assertTrue(activity.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
    }

    @Test
    fun `launcher Close requests update and returns success with the supplied widget ID`() {
        assertLauncherExit(useSystemBack = false)
    }

    @Test
    fun `launcher system Back requests update and returns success with the supplied widget ID`() {
        assertLauncherExit(useSystemBack = true)
    }

    private fun assertLauncherExit(useSystemBack: Boolean) {
        val widgetId = 42
        val intent = widgetIntent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        ActivityScenario.launch<WidgetConfigActivity>(intent).use { scenario ->
            lateinit var activity: WidgetConfigActivity
            val actions = mockk<WidgetActions>(relaxed = true)
            scenario.onActivity {
                activity = it
                it.widgetActions = actions
            }
            val close = composeRule.onNodeWithContentDescription(activity.getString(R.string.close))
            close.assertIsDisplayed()

            if (useSystemBack) {
                composeRule.runOnUiThread { activity.onBackPressedDispatcher.onBackPressed() }
            } else {
                close.performClick()
            }

            verify(exactly = 1) { actions.requestUpdate(widgetId) }
            assertTrue(activity.isFinishing)
            assertEquals(Activity.RESULT_OK, shadowOf(activity).resultCode)
            assertEquals(widgetId, shadowOf(activity).resultIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0))
        }
    }

    private fun widgetIntent(): Intent =
        Intent(ApplicationProvider.getApplicationContext(), WidgetConfigActivity::class.java)
}
