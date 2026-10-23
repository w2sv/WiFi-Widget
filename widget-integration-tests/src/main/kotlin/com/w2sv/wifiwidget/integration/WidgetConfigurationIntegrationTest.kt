package com.w2sv.wifiwidget.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.domain.wifiproperty.WifiProperty
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WidgetConfigurationIntegrationTest : WidgetIntegrationTestBase() {
    @Test
    fun `presentation changes render without remote requests or immediate work`() {
        env.setConfig(WidgetIntegrationEnvironment.testConfig(WifiProperty.ISP, WifiProperty.Frequency))
        http.respondWithIpApi("Baseline ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        env.pinWidget()
        env.awaitWidgetText("Baseline ISP")
        val requestsBefore = http.recordedRequests()
        val workBefore = env.work(WidgetIntegrationEnvironment.IMMEDIATE_WORK).map { it.id }.toSet()

        val changed = env.changeConfig { previous ->
            previous.copy(
                propertyOrder = listOf(WifiProperty.Frequency, WifiProperty.ISP) +
                    previous.propertyOrder.filterNot { it == WifiProperty.Frequency || it == WifiProperty.ISP },
                appearance = previous.appearance.copy(propertyValueAlignment = WifiPropertyValueAlignment.Left),
                utilities = previous.utilities + (WidgetUtility.RefreshTimeDisplay to true)
            )
        }

        env.await("updated property order and refresh-time utility") {
            env.widgetLabels().take(2) == listOf("Frequency", "ISP") &&
                env.widgetHasResource("last_updated_tv")
        }
        assertEquals(changed, env.configSource.blockingSnapshot())
        assertEquals(requestsBefore, http.recordedRequests())
        assertEquals(workBefore, env.work(WidgetIntegrationEnvironment.IMMEDIATE_WORK).map { it.id }.toSet())
    }

    @Test
    fun `remote requirement change renders before delayed HTTP completes`() {
        env.setConfig(WidgetIntegrationEnvironment.testConfig(WifiProperty.ISP))
        http.respondWithIpApi("Old ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        env.pinWidget()
        env.awaitWidgetText("Old ISP")
        val gate = http.respondWithDelayedIpApi("New ISP")

        val changed = env.changeConfig {
            it.withUpdatedPropertyEnablement(WifiProperty.ISP, false)
                .withUpdatedPropertyEnablement(WifiProperty.ASN, true)
                .let { current -> current.copy(utilities = current.utilities + (WidgetUtility.RefreshTimeDisplay to true)) }
        }
        try {
            env.await("new utility and old ISP removed while HTTP is held") {
                env.widgetHasResource("last_updated_tv") && !env.widgetHasText("Old ISP")
            }
            env.awaitRequest(TestRemoteHttpResponses.Endpoint.IpApi, 2)
            assertEquals(changed, env.configSource.blockingSnapshot())
            assertFalse(env.repository.data.value.ipApiData?.isp == "Old ISP")
            assertTrue(env.work(WidgetIntegrationEnvironment.IMMEDIATE_WORK).isNotEmpty())
        } finally {
            gate.countDown()
        }
        env.awaitWork(WidgetIntegrationEnvironment.IMMEDIATE_WORK, WorkInfo.State.SUCCEEDED)
        env.await("new ASN shown") { env.widgetHasText("AS424242") }
    }
}
