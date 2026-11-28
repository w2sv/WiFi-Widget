package com.w2sv.widget.ui.state

import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.widget.ui.actions.WidgetButtonAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetUtilitiesTest {
    @Test fun `configured action buttons retain declaration order`() {
        val config = WidgetConfig.default.copy(utilities = WidgetUtility.entries.reversed().associateWith { true })
        val utilities = WidgetUtilities(config)

        assertEquals(listOf(WidgetButtonAction.Refresh, WidgetButtonAction.WifiSettings), utilities.actions)
        assertTrue(utilities.showRefreshTime)
    }

    @Test fun `action buttons can be enabled independently`() {
        val cases = listOf(
            emptySet<WidgetUtility>() to emptyList(),
            setOf(WidgetUtility.RefreshButton) to listOf(WidgetButtonAction.Refresh),
            setOf(WidgetUtility.GoToWifiSettingsButton) to listOf(WidgetButtonAction.WifiSettings),
            setOf(WidgetUtility.RefreshButton, WidgetUtility.GoToWifiSettingsButton) to WidgetButtonAction.entries
        )

        cases.forEach { (enabled, expectedActions) ->
            val config = WidgetConfig.default.copy(utilities = WidgetUtility.entries.associateWith { it in enabled })
            val utilities = WidgetUtilities(config)

            assertEquals(expectedActions, utilities.actions)
            assertFalse(utilities.showRefreshTime)
        }
    }
}
