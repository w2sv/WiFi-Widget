package com.w2sv.wifiwidget.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.IpSetting
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WidgetRecoveryRetryIntegrationTest : WidgetIntegrationTestBase() {
    @Test
    fun `partial recovery retries only failed public IP and updates widget on success`() {
        val config = WidgetIntegrationEnvironment.testConfig(WifiProperty.ISP, WifiProperty.PublicIp)
            .withUpdatedPropertyConfig(WifiProperty.PublicIp) { it.withUpdatedSetting(IpSetting.V6Enabled, false) }
        env.setConfig(config)
        http.respondWithIpApi("Partial Recovery ISP")
        http.fail(TestRemoteHttpResponses.Endpoint.IpV4)
        http.respondWithIpV4("203.0.113.42")
        env.pinWidget()

        val recovery = env.awaitWork(WidgetIntegrationEnvironment.RECOVERY_WORK, WorkInfo.State.ENQUEUED)
        env.satisfyConstraints(recovery)
        val retry = env.awaitRetry(WidgetIntegrationEnvironment.RECOVERY_WORK)
        env.awaitRepositoryIsp("Partial Recovery ISP")
        assertTrue(env.repository.data.value.publicIps.isEmpty())
        assertTrue(env.repository.needsRecovery(config.remoteDataConfig))
        assertEquals(1, http.count(TestRemoteHttpResponses.Endpoint.IpApi))
        assertEquals(1, http.count(TestRemoteHttpResponses.Endpoint.IpV4))

        env.advanceRetry(retry)
        env.awaitWork(WidgetIntegrationEnvironment.RECOVERY_WORK, WorkInfo.State.SUCCEEDED)
        env.awaitWidgetText("203.0.113.42")
        assertEquals(1, http.count(TestRemoteHttpResponses.Endpoint.IpApi))
        assertEquals(2, http.count(TestRemoteHttpResponses.Endpoint.IpV4))
        assertFalse(env.repository.needsRecovery(config.remoteDataConfig))
    }
}
