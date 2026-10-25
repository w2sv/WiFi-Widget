package com.w2sv.widget.ui.properties

import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.widget.R
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.properties.renderdata.WifiPropertyRenderData
import com.w2sv.widget.ui.properties.renderdata.WifiPropertyRenderDataProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.inject.Provider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiPropertyRemoteViewsFactoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dataProvider = mockk<WifiPropertyRenderDataProvider> {
        every { this@mockk.invoke(any()) } returns mockk<WifiPropertyRenderData>()
    }
    private val repository = mockk<RemoteNetworkDataRepository>()
    private var directWifiDefault = true
    private var vpnDefault = false
    private val availabilitySource = NetworkAvailabilitySource {
        NetworkAvailabilitySnapshot(false, true, true, true, 1L, directWifiDefault, vpnDefault)
    }
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
            availabilitySource,
            scheduler
        )

    @Test fun `factory recreation schedules missing data without provider update`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns true
        factory().onCreate()
        verify(exactly = 1) { scheduler.enqueueRecovery() }
    }

    @Test fun `factory recreation schedules missing VPN data while Wi-Fi is connected`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns true
        directWifiDefault = false
        vpnDefault = true

        factory().onCreate()

        verify(exactly = 1) { scheduler.enqueueRecovery() }
    }

    @Test fun `initialized data does not reschedule on factory recreation`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns false
        repeat(3) { factory().onCreate() }
        verify(exactly = 0) { scheduler.enqueueRecovery() }
    }

    @Test fun `factory recreation does not schedule recovery through cellular`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns true
        directWifiDefault = false

        factory().onCreate()

        verify(exactly = 1) { repository.needsRecovery(config.remoteDataConfig) }
        verify(exactly = 0) { scheduler.enqueueRecovery() }
        verify(exactly = 1) { dataProvider.invoke(context) }
    }

    @Test
    fun `dataset replacement uses positional IDs without claiming stable row identity`() {
        val initial = renderData("Home", "84.1.2.3")
        val reordered = renderData("84.1.2.4", "Home", "Example ISP")
        val removed = renderData("Home")
        every { repository.needsRecovery(any()) } returns false
        every { dataProvider.invoke(context) } returnsMany listOf(initial, reordered, removed)
        val factory = factory()

        factory.onCreate()
        assertFalse(factory.hasStableIds())
        assertEquals(2, factory.getCount())
        assertEquals(0L, factory.getItemId(0))
        assertEquals(1L, factory.getItemId(1))
        assertEquals("84.1.2.3", factory.valueAt(1))

        factory.onDataSetChanged()
        assertEquals(3, factory.getCount())
        assertEquals(2L, factory.getItemId(2))
        assertEquals("84.1.2.4", factory.valueAt(0))
        assertEquals("Home", factory.valueAt(1))
        assertEquals("Example ISP", factory.valueAt(2))

        factory.onDataSetChanged()
        assertEquals(1, factory.getCount())
        assertEquals(0L, factory.getItemId(0))
        assertEquals(-1L, factory.getItemId(1))
        assertEquals("Home", factory.valueAt(0))
    }

    @Test
    fun `different widgets use distinct collection adapter intents`() {
        val first = WifiPropertyViewsService.intent(context, 1)
        val second = WifiPropertyViewsService.intent(context, 2)

        assertFalse(first.filterEquals(second))
    }

    @Test
    fun `new adapter registrations receive factories with independent datasets`() {
        val firstFactory = factory()
        val secondFactory = factory()
        val factoryProvider = mockk<Provider<WifiPropertyRemoteViewsFactory>> {
            every { get() } returnsMany listOf(firstFactory, secondFactory)
        }
        val service = WifiPropertyViewsService().apply {
            wifiPropertyRemoteViewsFactoryProvider = factoryProvider
        }
        every { repository.needsRecovery(any()) } returns false
        every { dataProvider.invoke(context) } returnsMany listOf(
            renderData("First widget", "84.1.2.3"),
            renderData("Second widget"),
            renderData("First updated")
        )

        assertSame(firstFactory, service.onGetViewFactory(WifiPropertyViewsService.intent(context, 1)))
        assertSame(secondFactory, service.onGetViewFactory(WifiPropertyViewsService.intent(context, 2)))
        assertNotSame(firstFactory, secondFactory)
        verify(exactly = 2) { factoryProvider.get() }

        firstFactory.onCreate()
        secondFactory.onCreate()
        assertEquals(2, firstFactory.getCount())
        assertEquals(1, secondFactory.getCount())
        assertEquals("First widget", firstFactory.valueAt(0))
        assertEquals("Second widget", secondFactory.valueAt(0))

        firstFactory.onDataSetChanged()
        assertEquals("First updated", firstFactory.valueAt(0))
        assertEquals("Second widget", secondFactory.valueAt(0))
    }

    private fun WifiPropertyRemoteViewsFactory.valueAt(position: Int): String =
        getViewAt(position)
            .apply(context, null)
            .findViewById<TextView>(R.id.property_value_tv)
            .text
            .toString()

    private fun renderData(vararg values: String) =
        WifiPropertyRenderData(
            presentations = values.map { WifiPropertyPresentation(SubscriptableText(it), it) },
            colors = WidgetColors(0, 1, 2),
            fontSize = FontSize.Medium,
            layout = R.layout.wifi_property_right_aligned
        )
}
