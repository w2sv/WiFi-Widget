package com.w2sv.wifiwidget.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.w2sv.core.common.R
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeContentTestRule: ComposeContentTestRule = createAndroidComposeRule<MainActivity>()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun homeScreenAndLocationAccessPermissionRationalShown() {
        with(composeContentTestRule) {
            waitForIdle()

            onNodeWithText(context.getString(R.string.location_access_permission_rational))
                .assertIsDisplayed()
        }
    }
}
