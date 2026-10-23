package com.w2sv.wifiwidget.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.integration.environment.WidgetWorkEnvironment
import com.w2sv.wifiwidget.integration.environment.widgetConfig
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WidgetRecoveryIntegrationTest : WidgetIntegrationTestBase() {
    /**
     * An Android widget-update callback should recover missing remote data and render it, then
     * avoid new recovery work once requirements are satisfied. This covers update callbacks after
     * a widget's remote configuration changes or its process is recreated.
     */
    @Test
    fun `widget update schedules recovery and does not repeat satisfied work`() {
        env.setConfig(widgetConfig(WifiProperty.Frequency))
        val widgetId = env.launcher.pinWidget()
        assertTrue(env.work.infos(WidgetWorkEnvironment.RECOVERY_WORK).isEmpty())
        env.setConfig(widgetConfig(WifiProperty.ISP))
        http.respondWithIpApi("Recovered From Update ISP")
        env.launcher.triggerWidgetUpdate(widgetId)
        val recovery = env.work.awaitWork(WidgetWorkEnvironment.RECOVERY_WORK, WorkInfo.State.ENQUEUED)
        assertTrue(env.repository.needsRecovery(env.configSource.blockingSnapshot().remoteDataConfig))

        env.work.satisfyConstraints(recovery)
        env.work.awaitWork(recovery.id, WorkInfo.State.SUCCEEDED)
        env.awaitRepositoryIsp("Recovered From Update ISP")
        env.launcher.awaitWidgetText("Recovered From Update ISP")
        assertFalse(env.repository.needsRecovery(env.configSource.blockingSnapshot().remoteDataConfig))

        val workIds = env.work.infos(WidgetWorkEnvironment.RECOVERY_WORK).map { it.id }.toSet()
        env.launcher.triggerWidgetUpdate(widgetId)
        env.launcher.awaitBroadcastIdle()
        assertEquals(workIds, env.work.infos(WidgetWorkEnvironment.RECOVERY_WORK).map { it.id }.toSet())
    }

    /**
     * Creating a fresh collection factory with missing remote data should schedule recovery and
     * update the widget. This covers Launcher3 recreating the RemoteViews service independently
     * of a provider update callback.
     */
    @Test
    fun `fresh RemoteViews factory creation schedules missing data recovery`() {
        env.setConfig(widgetConfig(WifiProperty.Frequency))
        env.launcher.pinWidget()
        assertTrue(env.work.infos(WidgetWorkEnvironment.RECOVERY_WORK).isEmpty())
        env.setConfig(widgetConfig(WifiProperty.ISP))
        http.respondWithIpApi("Recovered From Factory ISP")

        val factory = env.newRemoteViewsFactory
        factory.onCreate()
        try {
            val recovery = env.work.awaitWork(WidgetWorkEnvironment.RECOVERY_WORK, WorkInfo.State.ENQUEUED)
            env.work.satisfyConstraints(recovery)
            env.work.awaitWork(recovery.id, WorkInfo.State.SUCCEEDED)
            env.awaitRepositoryIsp("Recovered From Factory ISP")
            env.launcher.awaitWidgetText("Recovered From Factory ISP")
        } finally {
            factory.onDestroy()
        }
    }
}
