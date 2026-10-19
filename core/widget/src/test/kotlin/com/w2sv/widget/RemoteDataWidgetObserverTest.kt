package com.w2sv.widget

import android.appwidget.AppWidgetManager
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.RemoteWifiData
import com.w2sv.domain.networking.RemoteWifiDataRepository
import com.w2sv.domain.widget.WidgetActions
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RemoteDataWidgetObserverTest {
    private val data = MutableStateFlow(RemoteWifiData.empty)
    private val repository = mockk<RemoteWifiDataRepository> { every { data } returns this@RemoteDataWidgetObserverTest.data }
    private val actions = mockk<WidgetActions>(relaxed = true)
    private val manager = mockk<AppWidgetManager> {
        every { getAppWidgetIds(any()) } returns intArrayOf(1)
    }

    private fun TestScope.observer() =
        RemoteDataWidgetObserver(
            repository,
            actions,
            manager,
            ApplicationProvider.getApplicationContext(),
            CoroutineScope(backgroundScope.coroutineContext + StandardTestDispatcher(testScheduler))
        )

    private fun published(isp: String) =
        RemoteWifiData(IpApiData(null, null, "UTC", isp, null), emptyList())

    @Test fun `initial subscription skips current data`() =
        runTest {
            data.value = published("initial")
            observer().initialize()
            runCurrent()
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `changed data renders while equal data does not`() =
        runTest {
            observer().initialize()
            data.value = published("one")
            runCurrent()
            verify(exactly = 1) { actions.render() }
            data.value = published("one")
            runCurrent()
            verify(exactly = 1) { actions.render() }
            data.value = published("two")
            runCurrent()
            verify(exactly = 2) { actions.render() }
        }

    @Test fun `no installed widgets suppresses rendering`() =
        runTest {
            every { manager.getAppWidgetIds(any()) } returns intArrayOf()
            observer().initialize()
            data.value = published("one")
            runCurrent()
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `repeated initialization keeps one subscription`() =
        runTest {
            val observer = observer()
            observer.initialize()
            observer.initialize()
            data.value = published("one")
            runCurrent()
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `update immediately after startup is observed`() =
        runTest {
            val observer = observer()
            observer.initialize()
            data.value = published("immediate")
            runCurrent()
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `render failure does not stop observing later data changes`() =
        runTest {
            var attempts = 0
            var successfulRenders = 0
            every { actions.render() } answers {
                attempts++
                if (attempts == 1) throw IllegalStateException("launcher unavailable")
                successfulRenders++
            }
            observer().initialize()

            data.value = published("first")
            runCurrent()
            assertEquals(1, attempts)
            assertEquals(0, successfulRenders)

            data.value = published("second")
            runCurrent()
            assertEquals(2, attempts)
            assertEquals(1, successfulRenders)
        }
}
