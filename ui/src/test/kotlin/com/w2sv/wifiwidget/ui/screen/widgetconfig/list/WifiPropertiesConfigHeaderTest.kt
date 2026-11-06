package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiPropertiesConfigHeaderTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var config by mutableStateOf(WidgetConfig.default)

    @Test
    fun `move disabled menu action updates the order and disables itself`() {
        composeRule.setContent {
            WifiPropertiesConfigHeader(
                config = config,
                updateConfig = { update -> config = update(config) }
            )
        }

        val action = composeRule.activity.getString(R.string.move_disabled_properties_to_bottom)
        val showMenu = composeRule.activity.getString(R.string.show_dropdown_menu)
        composeRule.onNodeWithContentDescription(showMenu).performClick()
        composeRule.onNodeWithText(action).assertIsEnabled().performClick()

        composeRule.onNodeWithContentDescription(showMenu).performClick()
        composeRule.onNodeWithText(action).assertIsNotEnabled()
    }
}
