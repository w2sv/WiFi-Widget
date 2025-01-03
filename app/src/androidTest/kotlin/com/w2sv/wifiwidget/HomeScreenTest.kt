package com.w2sv.wifiwidget

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeContentTestRule: ComposeContentTestRule = createAndroidComposeRule<MainActivity>()

    private val context by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    @Test
    fun locationAccessDialog() {
        with(composeContentTestRule) {
            waitForIdle()

            onNodeWithText(context.getString(R.string.location_access_permission_rational))
                .assertIsDisplayed()

            onNodeWithText(context.getString(R.string.understood))
                .performClick()
        }
    }
}
