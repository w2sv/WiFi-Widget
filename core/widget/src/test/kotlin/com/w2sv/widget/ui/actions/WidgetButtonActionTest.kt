package com.w2sv.widget.ui.actions

import android.content.Context
import androidx.glance.action.Action
import com.w2sv.core.resources.R
import com.w2sv.domain.widget.WidgetUtility
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetButtonActionTest {
    @Before
    fun setUp() {
        mockkObject(WidgetActionFactory)
    }

    @After
    fun tearDown() {
        unmockkObject(WidgetActionFactory)
    }

    @Test fun `refresh button uses its configured metadata and factory action`() {
        val context = mockk<Context>()
        val refreshAction = mockk<Action>()
        every { WidgetActionFactory.refresh() } returns refreshAction
        val action = WidgetButtonAction.Refresh

        assertEquals(WidgetUtility.RefreshButton, action.configurationKey)
        assertEquals(R.drawable.ic_refresh_24, action.iconRes)
        assertEquals(R.string.refresh_data, action.descriptionRes)
        assertSame(refreshAction, action.createAction(context))
    }

    @Test fun `Wi-Fi settings button uses its configured metadata and factory action`() {
        val context = mockk<Context>()
        val wifiSettingsAction = mockk<Action>()
        every { WidgetActionFactory.wifiSettings(context) } returns wifiSettingsAction
        val action = WidgetButtonAction.WifiSettings

        assertEquals(WidgetUtility.GoToWifiSettingsButton, action.configurationKey)
        assertEquals(R.drawable.ic_wifi_settings_24, action.iconRes)
        assertEquals(R.string.open_wifi_settings_button, action.descriptionRes)
        assertSame(wifiSettingsAction, action.createAction(context))
    }
}
