package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.container.WidgetRenderer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiWidgetProviderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val manager = mockk<AppWidgetManager>(relaxed = true)
    private val scheduler = mockk<WifiWidgetWorkScheduler>(relaxed = true)
    private val repository = mockk<RemoteNetworkDataRepository>()
    private val renderer = mockk<WidgetRenderer> {
        every { this@mockk.invoke(any(), any(), any(), any()) } returns mockk<RemoteViews>()
    }
    private val config = WidgetConfig.default

    private fun provider(): WifiWidgetProvider =
        WifiWidgetProvider().apply {
            appWidgetManager = manager
            refreshManager = scheduler
            remoteNetworkDataRepository = repository
            widgetRenderer = renderer
            widgetConfigSource = mockk<WidgetConfigSource> {
                every { blockingSnapshot() } returns config
            }
        }

    @Test fun `first widget applies persisted periodic refresh policy`() {
        provider().onEnabled(context)
        verify(exactly = 1) { scheduler.applyRefreshingPolicy(config.refreshing) }
    }

    @Test fun `update schedules recovery for missing remote data`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns true
        provider().onUpdate(context, manager, intArrayOf(1))
        verify(exactly = 1) { scheduler.enqueueRecovery() }
    }

    @Test fun `repeated updates with initialized data do not schedule recovery`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns false
        val provider = provider()
        repeat(3) { provider.onUpdate(context, manager, intArrayOf(1)) }
        verify(exactly = 0) { scheduler.enqueueRecovery() }
    }

    @Test fun `render path does not start another recovery`() {
        provider().renderWidgets(context, manager, intArrayOf(1), config)
        verify(exactly = 0) { repository.needsRecovery(any()) }
        verify(exactly = 0) { scheduler.enqueueRecovery() }
        verify(exactly = 1) { manager.updateAppWidget(1, any<RemoteViews>()) }
    }

    @Test fun `removing the last widget cancels pending recovery`() {
        provider().onDisabled(context)
        verify(exactly = 1) { scheduler.cancelPeriodicWork() }
        verify(exactly = 1) { scheduler.cancelRecoveryWork() }
    }
}
