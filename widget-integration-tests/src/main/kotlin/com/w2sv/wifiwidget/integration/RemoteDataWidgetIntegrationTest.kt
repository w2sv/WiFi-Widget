package com.w2sv.wifiwidget.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.wifiproperty.WifiProperty
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RemoteDataWidgetIntegrationTest : WidgetIntegrationTestBase() {
    @Test
    fun `HTTP refresh publishes ISP and updates the existing launcher widget twice`() {
        env.setConfig(WidgetIntegrationEnvironment.testConfig(WifiProperty.ISP))
        http.respondWithIpApi("First Integration ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        val widgetId = env.pinWidget()
        env.awaitWidgetText("First Integration ISP")

        http.respondWithIpApi("Second Integration ISP")
        env.actions.refresh()
        env.awaitWork(WidgetIntegrationEnvironment.IMMEDIATE_WORK, WorkInfo.State.SUCCEEDED)
        env.awaitRepositoryIsp("Second Integration ISP")
        env.awaitWidgetText("Second Integration ISP")
        assertEquals(setOf(widgetId), env.widgetIds())
        assertEquals(2, http.count(TestRemoteHttpResponses.Endpoint.IpApi))
    }

    @Test
    fun `repository publication alone drives observer rendering`() {
        env.setConfig(WidgetIntegrationEnvironment.testConfig(WifiProperty.ISP))
        http.respondWithIpApi("Observer Baseline ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        env.pinWidget()
        env.awaitWidgetText("Observer Baseline ISP")

        http.respondWithIpApi("Observer Updated ISP")
        assertEquals(RemoteRefreshResult.Complete, runBlocking { env.repository.refresh() })
        env.awaitRepositoryIsp("Observer Updated ISP")
        env.awaitWidgetText("Observer Updated ISP")
        assertTrue(env.work(WidgetIntegrationEnvironment.IMMEDIATE_WORK).isEmpty())
    }
}
