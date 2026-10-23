package com.w2sv.wifiwidget.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import com.w2sv.domain.wifiproperty.WifiProperty
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WidgetRecoveryIntegrationTest : WidgetIntegrationTestBase() {
    @Test
    fun `widget update schedules recovery and does not repeat satisfied work`() {
        env.setConfig(WidgetIntegrationEnvironment.testConfig(WifiProperty.Frequency))
        val widgetId = env.pinWidget()
        assertTrue(env.work(WidgetIntegrationEnvironment.RECOVERY_WORK).isEmpty())
        env.setConfig(WidgetIntegrationEnvironment.testConfig(WifiProperty.ISP))
        http.respondWithIpApi("Recovered From Update ISP")
        env.triggerWidgetUpdate(widgetId)
        val recovery = env.awaitWork(WidgetIntegrationEnvironment.RECOVERY_WORK, WorkInfo.State.ENQUEUED)
        assertTrue(env.repository.needsRecovery(env.configSource.blockingSnapshot().remoteDataConfig))

        env.satisfyConstraints(recovery)
        env.awaitWork(WidgetIntegrationEnvironment.RECOVERY_WORK, WorkInfo.State.SUCCEEDED)
        env.awaitRepositoryIsp("Recovered From Update ISP")
        env.awaitWidgetText("Recovered From Update ISP")
        assertFalse(env.repository.needsRecovery(env.configSource.blockingSnapshot().remoteDataConfig))

        val workIds = env.work(WidgetIntegrationEnvironment.RECOVERY_WORK).map { it.id }.toSet()
        env.triggerWidgetUpdate(widgetId)
        env.awaitBroadcastIdle()
        assertEquals(workIds, env.work(WidgetIntegrationEnvironment.RECOVERY_WORK).map { it.id }.toSet())
    }

    @Test
    fun `fresh RemoteViews factory creation schedules missing data recovery`() {
        env.setConfig(WidgetIntegrationEnvironment.testConfig(WifiProperty.Frequency))
        env.pinWidget()
        assertTrue(env.work(WidgetIntegrationEnvironment.RECOVERY_WORK).isEmpty())
        env.setConfig(WidgetIntegrationEnvironment.testConfig(WifiProperty.ISP))
        http.respondWithIpApi("Recovered From Factory ISP")

        val factory = env.newRemoteViewsFactory
        factory.onCreate()
        try {
            val recovery = env.awaitWork(WidgetIntegrationEnvironment.RECOVERY_WORK, WorkInfo.State.ENQUEUED)
            env.satisfyConstraints(recovery)
            env.awaitWork(WidgetIntegrationEnvironment.RECOVERY_WORK, WorkInfo.State.SUCCEEDED)
            env.awaitRepositoryIsp("Recovered From Factory ISP")
            env.awaitWidgetText("Recovered From Factory ISP")
        } finally {
            factory.onDestroy()
        }
    }
}
