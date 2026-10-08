package com.w2sv.wifiwidget.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.common.utils.IsLocationEnabled
import com.w2sv.core.common.R
import com.w2sv.domain.model.ThemeSettings
import com.w2sv.wifiwidget.ui.location.LocationAccessRationalHistory
import io.mockk.every
import io.mockk.mockk
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
    fun `shows location access permission rational when it has not been shown before`() {
        setContent(rationalShown = false)

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.location_access_permission_rational)
        ).assertIsDisplayed()
    }

    @Test
    fun `does not show location access permission rational when it has been shown before`() {
        setContent(rationalShown = true)

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.location_access_permission_rational)
        ).assertDoesNotExist()
    }

    private fun setContent(rationalShown: Boolean) {
        val activityViewModel = mockk<ActivityViewModel> {
            every { themeSettings } returns MutableStateFlow(ThemeSettings.Default)
            every { locationAccessPermissionHistory } returns PermissionRequestHistory(flowOf(false)) {}
            every { locationAccessRationalHistory } returns LocationAccessRationalHistory(flowOf(rationalShown)) {}
        }

        composeRule.setContent {
            WifiWidgetUI(
                isLocationEnabled = IsLocationEnabled { true },
                activityViewModel = activityViewModel,
                content = {}
            )
        }
    }
}
