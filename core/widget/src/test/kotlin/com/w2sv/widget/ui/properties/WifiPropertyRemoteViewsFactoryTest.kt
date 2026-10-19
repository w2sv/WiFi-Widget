package com.w2sv.widget.ui.properties

import android.content.Context
import com.w2sv.domain.networking.RemoteWifiDataRepository
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.properties.renderdata.WifiPropertyRenderData
import com.w2sv.widget.ui.properties.renderdata.WifiPropertyRenderDataProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class WifiPropertyRemoteViewsFactoryTest {
    private val context = mockk<Context>()
    private val dataProvider = mockk<WifiPropertyRenderDataProvider> {
        every { this@mockk.invoke(any()) } returns mockk<WifiPropertyRenderData>()
    }
    private val repository = mockk<RemoteWifiDataRepository>()
    private val scheduler = mockk<WifiWidgetWorkScheduler>(relaxed = true)
    private val config = WidgetConfig.default
    private val configSource = mockk<WidgetConfigSource> {
        every { blockingSnapshot() } returns config
    }

    private fun factory() =
        WifiPropertyRemoteViewsFactory(
            context,
            dataProvider,
            configSource,
            repository,
            scheduler
        )

    @Test fun `factory recreation schedules missing data without provider update`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns true
        factory().onCreate()
        verify(exactly = 1) { scheduler.enqueueRecovery() }
    }

    @Test fun `initialized data does not reschedule on factory recreation`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns false
        repeat(3) { factory().onCreate() }
        verify(exactly = 0) { scheduler.enqueueRecovery() }
    }
}
