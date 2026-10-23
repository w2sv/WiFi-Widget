package com.w2sv.wifiwidget.integration

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.widget.RemoteDataWidgetObserver
import com.w2sv.widget.ui.properties.WifiPropertyRemoteViewsFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetIntegrationDependencies {
    fun widgetConfigDataSource(): WidgetConfigDataSource
    fun widgetConfigSource(): WidgetConfigSource
    fun widgetActions(): WidgetActions
    fun remoteNetworkDataRepository(): RemoteNetworkDataRepository
    fun remoteDataWidgetObserver(): RemoteDataWidgetObserver
    fun wifiStatusSource(): WifiStatusSource
    fun wifiPropertyRemoteViewsFactory(): WifiPropertyRemoteViewsFactory
}

/** Owns the one real launcher widget, WorkManager, and persistent state for a test. */
class WidgetIntegrationEnvironment(private val workerFactory: HiltWorkerFactory, val http: TestRemoteHttpResponses) {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private val appPackage = context.packageName
    private val widgetProvider = ComponentName(appPackage, "com.w2sv.widget.WifiWidgetProvider")
    private val widgetManager = AppWidgetManager.getInstance(context)
    private val executor: ExecutorService = Executors.newFixedThreadPool(4)
    private lateinit var dependencies: WidgetIntegrationDependencies
    private lateinit var workManager: WorkManager
    private lateinit var driver: TestDriver

    val configSource get() = dependencies.widgetConfigSource()
    val actions get() = dependencies.widgetActions()
    val repository get() = dependencies.remoteNetworkDataRepository()
    val newRemoteViewsFactory get() = dependencies.wifiPropertyRemoteViewsFactory()

    fun start() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setExecutor(executor)
                .setTaskExecutor(executor)
                .build()
        )
        workManager = WorkManager.getInstance(context)
        driver = checkNotNull(WorkManagerTestInitHelper.getTestDriver(context))
        dependencies = EntryPointAccessors.fromApplication(context, WidgetIntegrationDependencies::class.java)
        resetWork()
        device.pressHome()
        await("Launcher3 workspace") { device.hasObject(By.res(LAUNCHER_PACKAGE, "workspace")) }
        removeWidgets()
        http.reset()
        setConfig(testConfig())
        requireUsableWifi()
        dependencies.remoteDataWidgetObserver().initialize()
        dependencies.remoteDataWidgetObserver().initialize()
    }

    fun close() {
        http.reset()
        if (::workManager.isInitialized) resetWork()
        if (!device.hasObject(By.res(LAUNCHER_PACKAGE, "workspace"))) device.pressHome()
        if (::dependencies.isInitialized) {
            removeWidgets()
            setConfig(testConfig())
        }
        executor.shutdownNow()
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

    fun pinWidget(): Int {
        val before = widgetIds()
        instrumentation.context.startActivity(
            Intent().setClassName(appPackage, "com.w2sv.wifiwidget.ui.WidgetPinActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        val add = By.pkg(LAUNCHER_PACKAGE).text("ADD TO HOME SCREEN")
        await("launcher pin confirmation") { device.hasObject(add) }
        device.findObject(add).click()
        val id = awaitValue("new widget ID") { widgetIds().firstOrNull { it !in before } }
        await("widget $id visible") { device.hasObject(By.res(appPackage, "widget_layout")) }
        return id
    }

    fun widgetIds(): Set<Int> =
        widgetManager.getAppWidgetIds(widgetProvider).toSet()

    fun awaitWidgetText(value: String) {
        if (!device.hasObject(By.res(LAUNCHER_PACKAGE, "workspace"))) device.pressHome()
        await("launcher widget text '$value'") {
            device.hasObject(By.res(appPackage, "widget_layout")) &&
                device.hasObject(By.res(appPackage, "property_value_tv").text(value))
        }
    }

    fun widgetHasText(value: String): Boolean =
        device.hasObject(By.res(appPackage, "property_value_tv").text(value))

    fun widgetHasLabel(value: String): Boolean =
        device.hasObject(By.res(appPackage, "property_label_tv").text(value))

    fun widgetLabels(): List<String> =
        runCatching { device.findObjects(By.res(appPackage, "property_label_tv")).mapNotNull { it.text } }
            .getOrDefault(emptyList())

    fun widgetHasResource(resource: String): Boolean =
        device.hasObject(By.res(appPackage, resource))

    fun triggerWidgetUpdate(id: Int) {
        context.sendBroadcast(
            Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .setComponent(widgetProvider)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id))
        )
    }

    fun awaitBroadcastIdle() {
        val descriptor = instrumentation.uiAutomation.executeShellCommand("am wait-for-broadcast-idle")
        android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
    }

    fun awaitRepositoryIsp(expected: String) =
        await("repository ISP '$expected'") { repository.data.value.ipApiData?.isp == expected }

    fun awaitRequest(endpoint: TestRemoteHttpResponses.Endpoint, count: Int) =
        await("$endpoint request count $count") { http.count(endpoint) >= count }

    fun work(name: String): List<WorkInfo> =
        workManager.getWorkInfosForUniqueWork(name).get(5, TimeUnit.SECONDS)

    fun awaitWork(name: String, state: WorkInfo.State): WorkInfo =
        awaitValue("$name work in $state") {
            work(name).firstOrNull { it.state == state }
        }

    fun awaitRetry(name: String): WorkInfo =
        awaitValue("$name retained for retry") {
            work(name).firstOrNull { it.state == WorkInfo.State.ENQUEUED && it.runAttemptCount >= 1 }
        }

    fun satisfyConstraints(work: WorkInfo) {
        driver.setAllConstraintsMet(work.id)
    }

    fun advanceRetry(work: WorkInfo) {
        driver.setInitialDelayMet(work.id)
        driver.setAllConstraintsMet(work.id)
    }

    fun await(
        description: String,
        timeoutMs: Long = 30_000,
        condition: () -> Boolean
    ) {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            SystemClock.sleep(100)
        }
        error("Timed out waiting for $description. ${diagnostics()}")
    }

    private fun <T : Any> awaitValue(description: String, value: () -> T?): T {
        var result: T? = null
        await(description) { value()?.also { result = it } != null }
        return checkNotNull(result)
    }

    private fun requireUsableWifi() {
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity.activeNetwork
        val capabilities = network?.let(connectivity::getNetworkCapabilities)
        check(
            dependencies.wifiStatusSource().snapshot() == WifiStatus.Connected &&
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        ) {
            "Managed emulator requires connected, validated Wi-Fi as default route; " +
                "status=${dependencies.wifiStatusSource().snapshot()}, network=$network, capabilities=$capabilities"
        }
    }

    private fun resetWork() {
        listOf(PERIODIC_WORK, IMMEDIATE_WORK, RECOVERY_WORK).forEach { name ->
            workManager.cancelUniqueWork(name).result.get(5, TimeUnit.SECONDS)
        }
        workManager.pruneWork().result.get(5, TimeUnit.SECONDS)
    }

    private fun removeWidgets() {
        repeat(8) {
            if (widgetIds().isEmpty()) return
            val widget = awaitValue("widget to remove") { findWidgetOnLauncherPages() }
            val workspace = checkNotNull(device.findObject(By.res(LAUNCHER_PACKAGE, "workspace")))
            val statusBar = checkNotNull(device.findObject(By.res(SYSTEM_UI_PACKAGE, "status_bar")))
            val target = android.graphics.Point(
                workspace.visibleBounds.centerX(),
                statusBar.visibleBounds.bottom + workspace.visibleBounds.height() / 16
            )
            widget.drag(target, 600)
            await("widget ID removal") { widgetIds().isEmpty() }
        }
        check(widgetIds().isEmpty()) { "Could not clear launcher widgets. ${diagnostics()}" }
    }

    private fun findWidgetOnLauncherPages(): UiObject2? {
        device.findObject(By.res(appPackage, "widget_layout"))?.let { return it }
        val workspace = device.findObject(By.res(LAUNCHER_PACKAGE, "workspace")) ?: return null
        repeat(5) {
            workspace.scroll(Direction.LEFT, 0.9f)
            device.findObject(By.res(appPackage, "widget_layout"))?.let { return it }
        }
        repeat(10) {
            workspace.scroll(Direction.RIGHT, 0.9f)
            device.findObject(By.res(appPackage, "widget_layout"))?.let { return it }
        }
        return null
    }

    private fun diagnostics(): String {
        val work = if (::workManager.isInitialized) {
            listOf(IMMEDIATE_WORK, RECOVERY_WORK, PERIODIC_WORK).associateWith { runCatching { work(it) }.getOrNull() }
        } else {
            emptyMap()
        }
        val data = if (::dependencies.isInitialized) repository.data.value else null
        val hierarchy = ByteArrayOutputStream().use { output ->
            device.dumpWindowHierarchy(output)
            output.toString("UTF-8")
        }
        return "repository=$data, work=$work, HTTP=${http.recordedRequests()}, widgetIds=${widgetIds()}, hierarchy=$hierarchy"
    }

    companion object {
        const val IMMEDIATE_WORK = "immediate_widget_refreshing"
        const val RECOVERY_WORK = "remote_widget_recovery"
        const val PERIODIC_WORK = "periodic_widget_refreshing"
        private const val LAUNCHER_PACKAGE = "com.android.launcher3"
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"

        fun testConfig(vararg enabled: WifiProperty): WidgetConfig =
            WidgetConfig.default.copy(
                propertyConfig = WidgetConfig.default.propertyConfig.mapValues { (property, config) ->
                    config.copy(isEnabled = property in enabled)
                },
                refreshing = WidgetConfig.default.refreshing.copy(refreshPeriodically = false),
                utilities = WidgetConfig.default.utilities.mapValues { false }
            )
    }
}
