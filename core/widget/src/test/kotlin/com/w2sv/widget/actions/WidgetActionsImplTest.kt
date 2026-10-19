package com.w2sv.widget.actions

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetActionsImplTest {
    private val context = mockk<Context>(relaxed = true) {
        every { packageName } returns "com.w2sv.widget"
    }
    private val manager = mockk<AppWidgetManager> {
        every { getAppWidgetIds(any()) } returns intArrayOf(1)
    }
    private val scheduler = mockk<WifiWidgetWorkScheduler>(relaxed = true)
    private val actions = WidgetActionsImpl(context, manager, scheduler, MutableSharedFlow())
    private val previous = WidgetConfig.default

    @Test fun `unsupported pin request invokes failure callback`() {
        every { manager.isRequestPinAppWidgetSupported } returns false
        var failures = 0

        actions.pin { failures++ }

        assertEquals(1, failures)
        verify(exactly = 0) { manager.requestPinAppWidget(any(), any(), any()) }
    }

    @Test fun `rejected pin request invokes failure callback`() {
        every { manager.isRequestPinAppWidgetSupported } returns true
        every { manager.requestPinAppWidget(any(), null, any()) } returns false
        val actions = WidgetActionsImpl(ApplicationProvider.getApplicationContext(), manager, scheduler, MutableSharedFlow())
        var failures = 0

        actions.pin { failures++ }

        assertEquals(1, failures)
        verify(exactly = 1) { manager.requestPinAppWidget(any(), null, any()) }
    }

    @Test fun `refresh setting changes apply periodic policy and render without refresh`() {
        val current = previous.copy(refreshing = previous.refreshing.copy(refreshPeriodically = false))

        actions.applyConfigurationChange(previous, current)

        val refreshAction = WifiWidgetProvider.refreshIntent(context).action
        val renderAction = WifiWidgetProvider.renderIntent(context).action
        verify(exactly = 1) { scheduler.applyRefreshingPolicy(current.refreshing) }
        verify(exactly = 0) { context.sendBroadcast(match { it.action == refreshAction }) }
        verify(exactly = 1) { context.sendBroadcast(match { it.action == renderAction }) }
    }

    @Test fun `remote data requirement changes render and request immediate refresh`() {
        val current = previous.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)

        actions.applyConfigurationChange(previous, current)

        val refreshAction = WifiWidgetProvider.refreshIntent(context).action
        val renderAction = WifiWidgetProvider.renderIntent(context).action
        verify(exactly = 0) { scheduler.applyRefreshingPolicy(any()) }
        verify(exactly = 1) { context.sendBroadcast(match { it.action == refreshAction }) }
        verify(exactly = 1) { context.sendBroadcast(match { it.action == renderAction }) }
    }

    @Test fun `presentation and local property changes render without refresh`() {
        val changes = listOf(
            previous.withUpdatedPropertyPosition(0, 1),
            previous.withUpdatedPropertyEnablement(WifiProperty.Frequency, false),
            previous.copy(appearance = previous.appearance.copy(backgroundOpacity = 0.5f)),
            previous.copy(utilities = previous.utilities.mapValues { false })
        )

        changes.forEach { actions.applyConfigurationChange(previous, it) }

        val refreshAction = WifiWidgetProvider.refreshIntent(context).action
        val renderAction = WifiWidgetProvider.renderIntent(context).action
        verify(exactly = 0) { scheduler.applyRefreshingPolicy(any()) }
        verify(exactly = 0) { context.sendBroadcast(match { it.action == refreshAction }) }
        verify(exactly = changes.size) { context.sendBroadcast(match { it.action == renderAction }) }
    }

    @Test fun `no installed widgets skip periodic work but still render`() {
        every { manager.getAppWidgetIds(any()) } returns intArrayOf()
        val current = previous.copy(refreshing = previous.refreshing.copy(refreshPeriodically = false))

        actions.applyConfigurationChange(previous, current)

        val refreshAction = WifiWidgetProvider.refreshIntent(context).action
        val renderAction = WifiWidgetProvider.renderIntent(context).action
        verify(exactly = 0) { scheduler.applyRefreshingPolicy(any()) }
        verify(exactly = 0) { context.sendBroadcast(match { it.action == refreshAction }) }
        verify(exactly = 1) { context.sendBroadcast(match { it.action == renderAction }) }
    }
}
