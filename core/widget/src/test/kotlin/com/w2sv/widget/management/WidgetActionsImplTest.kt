package com.w2sv.widget.management

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.widget.WidgetStatus
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.preview.pinPreviewSize
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.rendering.WidgetRenderCoordinator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlinx.coroutines.test.runTest
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
    private val manager = mockk<AppWidgetManager>()
    private val widgetStatus = mockk<WidgetStatus> {
        every { hasInstalledWidgets } returns true
    }
    private val scheduler = mockk<WifiWidgetWorkScheduler>(relaxed = true)
    private val renderCoordinator = mockk<WidgetRenderCoordinator>(relaxed = true)
    private val configSource = mockk<WidgetConfigSource>()
    private val actions = WidgetActionsImpl(context, manager, scheduler, renderCoordinator, configSource, widgetStatus)
    private val previous = WidgetConfig.default

    @Test fun `no installed widgets skip immediate refresh requests`() {
        every { widgetStatus.hasInstalledWidgets } returns false

        actions.refresh()

        verify(exactly = 0) { context.sendBroadcast(any()) }
        verify(exactly = 0) { scheduler.enqueueImmediateRefresh() }
    }

    @Test fun `each manual refresh with installed widgets requests immediate work`() {
        val refreshIntent = WifiWidgetProvider.refreshIntent(context)

        repeat(2) { actions.refresh() }

        verify(exactly = 2) {
            context.sendBroadcast(match { it.action == refreshIntent.action && it.component == refreshIntent.component })
        }
    }

    @Test fun `refresh checks current widget installation after a previous empty result`() {
        every { widgetStatus.hasInstalledWidgets } returnsMany listOf(false, true)

        repeat(2) { actions.refresh() }

        verify(exactly = 1) { context.sendBroadcast(any()) }
    }

    @Test fun `unsupported pin request returns before reading configuration`() =
        runTest {
            every { manager.isRequestPinAppWidgetSupported } returns false

            val submitted = actions.pin()

            assertEquals(false, submitted)
            coVerify(exactly = 0) { configSource.snapshot() }
            verify(exactly = 0) { manager.requestPinAppWidget(any(), any(), any()) }
        }

    @Test fun `pin preview preparation failure returns false`() =
        runTest {
            every { manager.isRequestPinAppWidgetSupported } returns true
            coEvery { configSource.snapshot() } throws IllegalStateException("missing config")
            val actions =
                WidgetActionsImpl(
                    ApplicationProvider.getApplicationContext(),
                    manager,
                    scheduler,
                    renderCoordinator,
                    configSource,
                    widgetStatus
                )

            val submitted = actions.pin()

            assertEquals(false, submitted)
            coVerify(exactly = 1) { configSource.snapshot() }
        }

    @Test fun `successful pin request returns after platform submission`() =
        runTest {
            every { manager.isRequestPinAppWidgetSupported } returns true
            coEvery { configSource.snapshot() } returns WidgetConfig.default
            mockkConstructor(GlanceAppWidgetManager::class)
            try {
                coEvery {
                    anyConstructed<GlanceAppWidgetManager>().requestPinGlanceAppWidget(
                        WifiWidgetProvider::class.java,
                        any(),
                        pinPreviewSize,
                        null,
                        any()
                    )
                } returns true
                val actions =
                    WidgetActionsImpl(
                        ApplicationProvider.getApplicationContext(),
                        manager,
                        scheduler,
                        renderCoordinator,
                        configSource,
                        widgetStatus
                    )

                assertEquals(true, actions.pin())
                coVerify(exactly = 1) { configSource.snapshot() }
                coVerify(exactly = 1) {
                    anyConstructed<GlanceAppWidgetManager>().requestPinGlanceAppWidget(
                        WifiWidgetProvider::class.java,
                        any(),
                        pinPreviewSize,
                        null,
                        any()
                    )
                }
            } finally {
                unmockkConstructor(GlanceAppWidgetManager::class)
            }
        }

    @Test fun `refresh setting changes apply periodic policy and render without refresh`() {
        val current = previous.copy(refreshing = previous.refreshing.copy(refreshPeriodically = false))

        actions.applyConfigurationChange(previous, current)

        val refreshAction = WifiWidgetProvider.refreshIntent(context).action
        verify(exactly = 1) { scheduler.applyRefreshingPolicy(current.refreshing) }
        verify(exactly = 0) { context.sendBroadcast(match { it.action == refreshAction }) }
        verify(exactly = 1) { renderCoordinator.requestRender() }
    }

    @Test fun `remote data requirement changes render and request immediate refresh`() {
        val current = previous.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)

        actions.applyConfigurationChange(previous, current)

        val refreshAction = WifiWidgetProvider.refreshIntent(context).action
        verify(exactly = 0) { scheduler.applyRefreshingPolicy(any()) }
        verify(exactly = 1) { context.sendBroadcast(match { it.action == refreshAction }) }
        verify(exactly = 1) { renderCoordinator.requestRender() }
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
        verify(exactly = 0) { scheduler.applyRefreshingPolicy(any()) }
        verify(exactly = 0) { context.sendBroadcast(match { it.action == refreshAction }) }
        verify(exactly = changes.size) { renderCoordinator.requestRender() }
    }

    @Test fun `no installed widgets skip periodic work but still render`() {
        every { widgetStatus.hasInstalledWidgets } returns false
        val current = previous.copy(refreshing = previous.refreshing.copy(refreshPeriodically = false))

        actions.applyConfigurationChange(previous, current)

        val refreshAction = WifiWidgetProvider.refreshIntent(context).action
        verify(exactly = 0) { scheduler.applyRefreshingPolicy(any()) }
        verify(exactly = 0) { context.sendBroadcast(match { it.action == refreshAction }) }
        verify(exactly = 1) { renderCoordinator.requestRender() }
    }

    @Test fun `initial update targets the provider with the supplied widget ID`() {
        actions.requestUpdate(42)

        verify(exactly = 1) {
            context.sendBroadcast(
                match {
                    it.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE &&
                        it.component?.className == WifiWidgetProvider::class.java.name &&
                        it.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)?.contentEquals(intArrayOf(42)) == true
                }
            )
        }
        verify(exactly = 0) { widgetStatus.hasInstalledWidgets }
    }
}
