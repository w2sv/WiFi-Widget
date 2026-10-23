package com.w2sv.wifiwidget.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.integration.environment.WidgetWorkEnvironment
import com.w2sv.wifiwidget.integration.environment.http.TestRemoteHttpResponses
import com.w2sv.wifiwidget.integration.environment.widgetConfig
import com.w2sv.wifiwidget.testing.launcher.GridCells
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
    /**
     * When only local presentation settings change, the pinned widget should reflect the persisted
     * configuration without fetching remote data or scheduling immediate work. This keeps ordinary
     * layout and utility edits responsive and free of unnecessary network traffic.
     */
    @Test
    fun `presentation changes render without remote requests or immediate work`() {
        env.setConfig(widgetConfig(WifiProperty.ISP))
        http.respondWithIpApi("Baseline ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        env.launcher.pinWidget(GridCells(3, 3))
        env.launcher.awaitWidgetText("Baseline ISP")
        assertFalse(env.launcher.widgetHasLabel("Frequency"))
        val requestsBefore = http.recordedRequests()
        val workBefore = env.work.infos(WidgetWorkEnvironment.IMMEDIATE_WORK).map { it.id }.toSet()

        val changed = env.changeConfig { previous ->
            previous.withUpdatedPropertyEnablement(WifiProperty.Frequency, true).copy(
                propertyOrder = listOf(WifiProperty.Frequency, WifiProperty.ISP) +
                    previous.propertyOrder.filterNot { it == WifiProperty.Frequency || it == WifiProperty.ISP },
                utilities = previous.utilities + (WidgetUtility.RefreshTimeDisplay to true)
            )
        }

        env.launcher.await("updated property order and refresh-time utility") {
            env.launcher.widgetLabels().take(2) == listOf("Frequency", "ISP") &&
                env.launcher.widgetHasResource("last_updated_tv")
        }
        assertEquals(changed, env.configSource.blockingSnapshot())
        assertEquals(requestsBefore, http.recordedRequests())
        assertEquals(workBefore, env.work.infos(WidgetWorkEnvironment.IMMEDIATE_WORK).map { it.id }.toSet())
    }

    /**
     * Changing required remote properties must render the new configuration and remove obsolete
     * data while HTTP is still pending. This keeps the widget current even when a refresh is slow.
     */
    @Test
    fun `remote requirement change renders before delayed HTTP completes`() {
        env.setConfig(widgetConfig(WifiProperty.ISP))
        http.respondWithIpApi("Old ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        env.launcher.pinWidget()
        env.launcher.awaitWidgetText("Old ISP")
        val gate = http.respondWithDelayedIpApi("New ISP")

        val immediateWorkBefore = env.work.infos(WidgetWorkEnvironment.IMMEDIATE_WORK).map { it.id }.toSet()
        val changed = env.changeConfig {
            it.withUpdatedPropertyEnablement(WifiProperty.ISP, false)
                .withUpdatedPropertyEnablement(WifiProperty.ASN, true)
                .let { current -> current.copy(utilities = current.utilities + (WidgetUtility.RefreshTimeDisplay to true)) }
        }
        try {
            env.launcher.await("new utility and old ISP removed while HTTP is held") {
                env.launcher.widgetHasResource("last_updated_tv") && !env.launcher.widgetHasText("Old ISP")
            }
            env.awaitRequest(TestRemoteHttpResponses.Endpoint.IpApi, 2)
            assertEquals(changed, env.configSource.blockingSnapshot())
            assertFalse(env.repository.data.value.ipApiData?.isp == "Old ISP")
            assertTrue(env.work.awaitNewWork(WidgetWorkEnvironment.IMMEDIATE_WORK, immediateWorkBefore).id !in immediateWorkBefore)
        } finally {
            gate.countDown()
        }
        val immediate = env.work.awaitNewWork(WidgetWorkEnvironment.IMMEDIATE_WORK, immediateWorkBefore)
        env.work.awaitWork(immediate.id, WorkInfo.State.SUCCEEDED)
        env.launcher.await("new ASN shown") { env.launcher.widgetHasText("AS424242") }
    }
}
