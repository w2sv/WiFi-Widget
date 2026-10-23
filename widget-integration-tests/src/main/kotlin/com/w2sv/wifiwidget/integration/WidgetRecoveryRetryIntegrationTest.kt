package com.w2sv.wifiwidget.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.wifiwidget.integration.environment.WidgetWorkEnvironment
import com.w2sv.wifiwidget.integration.environment.http.TestRemoteHttpResponses
import com.w2sv.wifiwidget.integration.environment.widgetConfig
import com.w2sv.wifiwidget.testing.launcher.GridCells
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WidgetRecoveryRetryIntegrationTest : WidgetIntegrationTestBase() {
    /**
     * When IP-API succeeds but public-IP retrieval fails, the ISP should appear before WorkManager
     * retries only the missing request. This preserves useful partial data while recovery remains
     * incomplete and confirms that a later success finishes the widget update.
     */
    @Test
    fun `partial recovery retries only failed public IP and updates widget on success`() {
        val config = widgetConfig(WifiProperty.ISP, WifiProperty.PublicIp)
            .withUpdatedPropertyConfig(WifiProperty.PublicIp) { it.withUpdatedSetting(IpSetting.V6Enabled, false) }
        env.setConfig(config)
        http.respondWithIpApi("Partial Recovery ISP")
        http.fail(TestRemoteHttpResponses.Endpoint.IpV4)
        http.respondWithIpV4("203.0.113.42")
        env.launcher.pinWidget(GridCells(3, 3))

        val recovery = env.work.awaitWork(WidgetWorkEnvironment.RECOVERY_WORK, WorkInfo.State.ENQUEUED)
        env.work.satisfyConstraints(recovery)
        val retry = env.work.awaitRetry(recovery.id)
        env.awaitRepositoryIsp("Partial Recovery ISP")
        env.launcher.awaitWidgetText("Partial Recovery ISP")
        assertTrue(env.repository.data.value.publicIps.isEmpty())
        assertTrue(env.repository.needsRecovery(config.remoteDataConfig))
        assertEquals(1, http.count(TestRemoteHttpResponses.Endpoint.IpApi))
        assertEquals(1, http.count(TestRemoteHttpResponses.Endpoint.IpV4))

        env.work.advanceRetry(retry)
        env.work.awaitWork(recovery.id, WorkInfo.State.SUCCEEDED)
        env.launcher.awaitWidgetText("203.0.113.42")
        assertEquals(1, http.count(TestRemoteHttpResponses.Endpoint.IpApi))
        assertEquals(2, http.count(TestRemoteHttpResponses.Endpoint.IpV4))
        assertFalse(env.repository.needsRecovery(config.remoteDataConfig))
    }
}
