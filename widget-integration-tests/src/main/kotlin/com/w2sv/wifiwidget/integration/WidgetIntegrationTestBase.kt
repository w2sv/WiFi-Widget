package com.w2sv.wifiwidget.integration

import androidx.hilt.work.HiltWorkerFactory
import com.w2sv.wifiwidget.integration.environment.WidgetIntegrationEnvironment
import com.w2sv.wifiwidget.integration.environment.http.TestRemoteHttpResponses
import dagger.hilt.android.testing.HiltAndroidRule
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class WidgetIntegrationTestBase {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var http: TestRemoteHttpResponses

    protected lateinit var env: WidgetIntegrationEnvironment

    @Before
    fun setUpWidgetIntegration() {
        hiltRule.inject()
        env = WidgetIntegrationEnvironment(workerFactory, http)
        env.start()
    }

    @After
    fun tearDownWidgetIntegration() {
        if (::env.isInitialized) env.close()
    }
}
