package com.w2sv.wifiwidget.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.resources.R
import com.w2sv.wifiwidget.di.LocationAccessActivityModule
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = com.w2sv.wifiwidget.Application::class)
class LocationAccessConfigurationStateTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun `temporary recovery preserves configuration destination dialog and unsaved appearance edits`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configIntent = Intent(context, WidgetConfigActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 42)

        ActivityScenario.launch<WidgetConfigActivity>(configIntent).use { configuration ->
            composeRule.onNodeWithContentDescription(context.getString(R.string.background_opacity))
                .performSemanticsAction(SemanticsActions.SetProgress) { it(0.5f) }
            composeRule.onNodeWithText(context.getString(R.string.apply)).assertIsDisplayed()
            composeRule.onNodeWithTag("widget_config_list").performScrollToNode(hasText(context.getString(R.string.ssid)))
            composeRule.onNodeWithContentDescription(
                context.getString(R.string.info_icon_cd, context.getString(R.string.ssid))
            ).performClick()
            composeRule.onNodeWithText(context.getString(R.string.ssid_description)).assertExists()

            configuration.moveToState(Lifecycle.State.CREATED)
            ActivityScenario.launch<LocationAccessActivity>(
                LocationAccessActivityModule.provideLocationAccessActivityIntentProvider()(context)
            ).use { recovery ->
                recovery.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            }
            configuration.moveToState(Lifecycle.State.RESUMED)

            configuration.onActivity { assertFalse(it.isFinishing) }
            composeRule.onNodeWithText(context.getString(R.string.widget_configuration)).assertExists()
            composeRule.onNodeWithText(context.getString(R.string.ssid_description)).assertExists()
            composeRule.onNodeWithText(context.getString(R.string.close)).performClick()
            composeRule.onNodeWithText(context.getString(R.string.apply)).assertExists()
            composeRule.onNodeWithTag("widget_config_list").performScrollToNode(hasText(context.getString(R.string.background_opacity)))
            composeRule.onNodeWithContentDescription(context.getString(R.string.background_opacity))
                .assertRangeInfoEquals(ProgressBarRangeInfo(0.5f, 0f..1f, steps = 9))
        }
    }
}
