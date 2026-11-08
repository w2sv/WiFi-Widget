package com.w2sv.widget.ui.state

import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.widget.ui.actions.WidgetAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetUtilitiesTest {
    @Test fun `configured action buttons retain declaration order`() {
        val config = WidgetConfig.default.copy(utilities = WidgetUtility.entries.reversed().associateWith { true })
        val utilities = WidgetUtilities(config)

        assertEquals(listOf(WidgetAction.Refresh, WidgetAction.WifiSettings), utilities.actions)
        assertTrue(utilities.showRefreshTime)
    }

    @Test fun `action buttons can be enabled independently`() {
        val cases = listOf(
            emptySet<WidgetUtility>() to emptyList(),
            setOf(WidgetUtility.RefreshButton) to listOf(WidgetAction.Refresh),
            setOf(WidgetUtility.GoToWifiSettingsButton) to listOf(WidgetAction.WifiSettings),
            setOf(WidgetUtility.RefreshButton, WidgetUtility.GoToWifiSettingsButton) to WidgetAction.entries
        )

        cases.forEach { (enabled, expectedActions) ->
            val config = WidgetConfig.default.copy(utilities = WidgetUtility.entries.associateWith { it in enabled })
            val utilities = WidgetUtilities(config)

            assertEquals(expectedActions, utilities.actions)
            assertFalse(utilities.showRefreshTime)
        }
    }

    @Test fun `refresh timestamp does not enable an action button`() {
        val config = WidgetConfig.default.copy(
            utilities = WidgetUtility.entries.associateWith { it == WidgetUtility.RefreshTimeDisplay }
        )
        val utilities = WidgetUtilities(config)

        assertTrue(utilities.showRefreshTime)
        assertTrue(utilities.actions.isEmpty())
    }
}
