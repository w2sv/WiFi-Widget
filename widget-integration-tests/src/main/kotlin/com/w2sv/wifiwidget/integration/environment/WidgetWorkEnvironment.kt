package com.w2sv.wifiwidget.integration.environment

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Owns deterministic WorkManager execution and its executor for one integration test. */
class WidgetWorkEnvironment(
    private val context: Context,
    private val workerFactory: HiltWorkerFactory,
    private val await: (String, () -> Boolean) -> Unit
) {
    private val executor: ExecutorService = Executors.newFixedThreadPool(4)
    private lateinit var manager: WorkManager
    private lateinit var driver: TestDriver

    val isInitialized: Boolean get() = ::manager.isInitialized

    fun initialize() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setExecutor(executor)
                .setTaskExecutor(executor)
                .build()
        )
        manager = WorkManager.getInstance(context)
        driver = checkNotNull(WorkManagerTestInitHelper.getTestDriver(context))
    }

    fun reset() {
        UNIQUE_WORK_NAMES.forEach { name ->
            manager.cancelUniqueWork(name).result.get(5, TimeUnit.SECONDS)
        }
        manager.pruneWork().result.get(5, TimeUnit.SECONDS)
    }

    fun cancel(name: String): Operation =
        manager.cancelUniqueWork(name)

    fun awaitCancellation(operation: Operation) {
        operation.result.get(10, TimeUnit.SECONDS)
    }

    fun prune() {
        manager.pruneWork().result.get(10, TimeUnit.SECONDS)
    }

    fun shutdown() {
        executor.shutdownNow()
    }

    fun infos(name: String): List<WorkInfo> =
        manager.getWorkInfosForUniqueWork(name).get(5, TimeUnit.SECONDS)

    fun awaitNewWork(name: String, previousIds: Set<UUID> = emptySet()): WorkInfo =
        awaitValue("new $name work after $previousIds") {
            infos(name).firstOrNull { it.id !in previousIds }
        }

    fun awaitWork(name: String, state: WorkInfo.State): WorkInfo =
        awaitValue("$name work in $state") {
            infos(name).firstOrNull { it.state == state }
        }

    fun awaitWork(id: UUID, state: WorkInfo.State): WorkInfo =
        awaitValue("work $id in $state") {
            manager.getWorkInfoById(id).get(5, TimeUnit.SECONDS)?.takeIf { it.state == state }
        }

    fun awaitRetry(id: UUID): WorkInfo =
        awaitValue("work $id retained for retry") {
            manager.getWorkInfoById(id).get(5, TimeUnit.SECONDS)
                ?.takeIf { it.state == WorkInfo.State.ENQUEUED && it.runAttemptCount >= 1 }
        }

    fun satisfyConstraints(work: WorkInfo) {
        driver.setAllConstraintsMet(work.id)
    }

    fun advanceRetry(work: WorkInfo) {
        driver.setInitialDelayMet(work.id)
        driver.setAllConstraintsMet(work.id)
    }

    fun diagnostics(): Map<String, List<WorkInfo>?> =
        if (isInitialized) UNIQUE_WORK_NAMES.associateWith { runCatching { infos(it) }.getOrNull() } else emptyMap()

    private fun <T : Any> awaitValue(description: String, value: () -> T?): T {
        var result: T? = null
        await(description) { value()?.also { result = it } != null }
        return checkNotNull(result)
    }

    companion object {
        const val IMMEDIATE_WORK = "immediate_widget_refreshing"
        const val RECOVERY_WORK = "remote_widget_recovery"
        const val PERIODIC_WORK = "periodic_widget_refreshing"

        val UNIQUE_WORK_NAMES = listOf(PERIODIC_WORK, IMMEDIATE_WORK, RECOVERY_WORK)
    }
}
