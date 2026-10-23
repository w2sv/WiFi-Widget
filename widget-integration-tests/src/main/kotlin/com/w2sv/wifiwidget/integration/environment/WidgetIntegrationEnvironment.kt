package com.w2sv.wifiwidget.integration.environment

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Operation
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.wifiwidget.integration.environment.http.TestRemoteHttpResponses
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** Coordinates persistent state, HTTP fixtures, launcher UI, and work for one integration test. */
class WidgetIntegrationEnvironment(workerFactory: HiltWorkerFactory, val http: TestRemoteHttpResponses) {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    val launcher = WidgetLauncherEnvironment(instrumentation, context, ::diagnostics)
    val work = WidgetWorkEnvironment(context, workerFactory) { description, condition ->
        await(description, condition = condition)
    }
    private lateinit var dependencies: WidgetIntegrationDependencies

    val configSource get() = dependencies.widgetConfigSource()
    val actions get() = dependencies.widgetActions()
    val repository get() = dependencies.remoteNetworkDataRepository()
    val newRemoteViewsFactory get() = dependencies.wifiPropertyRemoteViewsFactory()

    fun start() {
        dependencies = EntryPointAccessors.fromApplication(context, WidgetIntegrationDependencies::class.java)
        http.reset()
        setConfig(widgetConfig())
        work.initialize()
        work.reset()
        launcher.prepare()
        awaitUsableWifi()
        dependencies.remoteDataWidgetObserver().initialize()
        // Repeated initialization must leave the observer with one subscription.
        dependencies.remoteDataWidgetObserver().initialize()
    }

    fun close() {
        val failures = mutableListOf<Throwable>()
        fun attempt(action: () -> Unit): Boolean {
            try {
                action()
                return true
            } catch (failure: Throwable) {
                failures.add(failure)
                return false
            }
        }
        val cancellations = mutableListOf<Operation>()
        try {
            // A queued widget broadcast can enqueue work after cancellation if it has not run yet.
            val broadcastsDrained = attempt { launcher.awaitBroadcastIdle() }
            var cancellationsCompleted = true
            if (work.isInitialized) {
                WidgetWorkEnvironment.UNIQUE_WORK_NAMES.forEach { name ->
                    if (!attempt { cancellations.add(work.cancel(name)) }) cancellationsCompleted = false
                }
            }
            // Do not wait for work cancellation while an HTTP interceptor is still gated.
            val gatesReleased = attempt { http.releaseGates() }
            cancellations.forEach { operation ->
                if (!attempt { work.awaitCancellation(operation) }) cancellationsCompleted = false
            }
            // Cancellation signals the coroutine but does not wait for an interceptor blocked in HTTP.
            val httpIdle = attempt { http.awaitIdle() }
            attempt { launcher.showLauncher() }
            attempt { launcher.clearExistingWidgets() }
            if (::dependencies.isInitialized) attempt { setConfig(widgetConfig()) }
            if (work.isInitialized) attempt { work.prune() }
            val cleanupDiagnostics = if (failures.isEmpty()) null else runCatching { diagnostics() }.getOrNull()
            if (broadcastsDrained && cancellationsCompleted && gatesReleased && httpIdle) attempt { http.reset() }
            if (failures.isNotEmpty()) {
                val primary = failures.first()
                failures.drop(1).forEach(primary::addSuppressed)
                cleanupDiagnostics?.let { primary.addSuppressed(AssertionError("Cleanup state: $it")) }
                throw primary
            }
        } finally {
            work.shutdown()
        }
    }

    fun setConfig(config: WidgetConfig) {
        runBlocking {
            dependencies.widgetConfigDataSource().update { config }
            configSource.flow.first { it == config }
        }
        check(configSource.blockingSnapshot() == config) { "Configuration was not persisted: ${configSource.blockingSnapshot()}" }
    }

    fun changeConfig(transform: (WidgetConfig) -> WidgetConfig): WidgetConfig {
        val previous = configSource.blockingSnapshot()
        val current = transform(previous)
        setConfig(current)
        actions.applyConfigurationChange(previous, current)
        return current
    }

    fun awaitRepositoryIsp(expected: String) =
        await("repository ISP '$expected'") { repository.data.value.ipApiData?.isp == expected }

    fun awaitRequest(endpoint: TestRemoteHttpResponses.Endpoint, count: Int) =
        await("$endpoint request count $count") { http.count(endpoint) >= count }

    private fun await(
        description: String,
        timeoutMs: Long = 30_000,
        condition: () -> Boolean
    ) =
        launcher.await(description, timeoutMs, condition)

    private fun awaitUsableWifi() {
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        await("connected, validated Wi-Fi default route", timeoutMs = 60_000) {
            val network = connectivity.activeNetwork
            val capabilities = network?.let(connectivity::getNetworkCapabilities)
            dependencies.wifiStatusSource().snapshot() == WifiStatus.Connected &&
                network != null &&
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }

    private fun diagnostics(): String {
        val data = repository.data.value.takeIf { ::dependencies.isInitialized }
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity.activeNetwork
        val capabilities = network?.let(connectivity::getNetworkCapabilities)
        val status = if (::dependencies.isInitialized) runCatching { dependencies.wifiStatusSource().snapshot() }.getOrNull() else null
        return "repository=$data, work=${work.diagnostics()}, HTTP=${http.recordedRequests()}, " +
            "wifiStatus=$status, network=$network, capabilities=$capabilities, ${launcher.diagnostics()}"
    }
}
