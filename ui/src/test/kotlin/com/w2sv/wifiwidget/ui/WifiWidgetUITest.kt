package com.w2sv.wifiwidget.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.domain.preferences.ThemeSettings
import com.w2sv.wifiwidget.ui.location.LocationAccessDependencies
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiWidgetUITest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `renders app content without an automatic permission dialog`() {
        setContent()

        composeRule.onNodeWithText("Content").assertIsDisplayed()
    }

    private fun setContent() {
        val activityViewModel = mockk<ActivityViewModel> {
            every { themeSettings } returns MutableStateFlow(ThemeSettings.Default)
        }
        val locationEnabled = object : IsLocationEnabled {
            override fun invoke(): Boolean =
                true
            override val flow: Flow<Boolean> = flowOf(true)
        }
        val locationAccessDependencies = mockk<LocationAccessDependencies> {
            every { isLocationEnabled } returns locationEnabled
            every { requestHistory } returns PermissionRequestHistory(flowOf(false)) {}
        }

        composeRule.setContent {
            WifiWidgetUI(
                versionName = "Test",
                locationAccessDependencies = locationAccessDependencies,
                activityViewModel = activityViewModel,
                content = { Text("Content") }
            )
        }
    }
}
