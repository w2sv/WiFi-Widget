package com.w2sv.wifiwidget.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.wifiwidget.integration.environment.WidgetWorkEnvironment
import com.w2sv.wifiwidget.integration.environment.http.TestRemoteHttpResponses
import com.w2sv.wifiwidget.integration.environment.widgetConfig
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RemoteDataWidgetIntegrationTest : WidgetIntegrationTestBase() {
    /**
     * A normal refresh of an existing widget should publish fetched ISP data and replace its
     * launcher value without repinning. This covers repeated user-triggered remote updates.
     */
    @Test
    fun `HTTP refresh publishes ISP and updates the existing launcher widget twice`() {
        env.setConfig(widgetConfig(WifiProperty.ISP))
        http.respondWithIpApi("First Integration ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        val widgetId = env.launcher.pinWidget()
        env.launcher.awaitWidgetText("First Integration ISP")

        http.respondWithIpApi("Second Integration ISP")
        env.actions.refresh()
        env.work.awaitWork(WidgetWorkEnvironment.IMMEDIATE_WORK, WorkInfo.State.SUCCEEDED)
        env.awaitRepositoryIsp("Second Integration ISP")
        env.launcher.awaitWidgetText("Second Integration ISP")
        assertEquals(setOf(widgetId), env.launcher.widgetIds())
        assertEquals(2, http.count(TestRemoteHttpResponses.Endpoint.IpApi))
    }

    /**
     * A repository refresh outside WorkManager should still update a pinned widget through the
     * observer. This protects callers that publish remote data without requesting a render.
     */
    @Test
    fun `repository publication alone drives observer rendering`() {
        env.setConfig(widgetConfig(WifiProperty.ISP))
        http.respondWithIpApi("Observer Baseline ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        env.launcher.pinWidget()
        env.launcher.awaitWidgetText("Observer Baseline ISP")

        http.respondWithIpApi("Observer Updated ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        env.awaitRepositoryIsp("Observer Updated ISP")
        env.launcher.awaitWidgetText("Observer Updated ISP")
        assertTrue(env.work.infos(WidgetWorkEnvironment.IMMEDIATE_WORK).isEmpty())
    }
}
