package com.w2sv.widget.refreshing

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.w2sv.domain.widget.WidgetRefreshing
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WifiWidgetWorkSchedulerTest {
    @Test fun `immediate and periodic work keep their persisted unique names`() {
        val workManager = mockk<WorkManager>(relaxed = true)
        val oneTimeRequests = mutableListOf<OneTimeWorkRequest>()
        val periodicRequests = mutableListOf<PeriodicWorkRequest>()
        every { workManager.enqueueUniqueWork(any(), any(), capture(oneTimeRequests)) } returns mockk()
        every { workManager.enqueueUniquePeriodicWork(any(), any(), capture(periodicRequests)) } returns mockk()
        val scheduler = WifiWidgetWorkScheduler(workManager)

        scheduler.enqueueImmediateRefresh()
        scheduler.applyRefreshingPolicy(WidgetRefreshing())

        verify {
            workManager.enqueueUniqueWork("immediate_widget_refreshing", ExistingWorkPolicy.REPLACE, any<OneTimeWorkRequest>())
            workManager.enqueueUniquePeriodicWork(
                "periodic_widget_refreshing",
                ExistingPeriodicWorkPolicy.UPDATE,
                any<PeriodicWorkRequest>()
            )
        }
        assertEquals(RefreshWork.Immediate, RefreshWork.fromWorkData(oneTimeRequests.single().workSpec.input))
        assertEquals(RefreshWork.Periodic, RefreshWork.fromWorkData(periodicRequests.single().workSpec.input))
        assertFalse(periodicRequests.single().workSpec.constraints.requiresBatteryNotLow())
        assertEquals(Duration.ofMinutes(15).toMillis(), periodicRequests.single().workSpec.intervalDuration)
        assertEquals(Duration.ofMinutes(15).toMillis(), periodicRequests.single().workSpec.initialDelay)
    }

    @Test fun `configured interval updates periodic work without a battery constraint`() {
        val workManager = mockk<WorkManager>(relaxed = true)
        val requests = mutableListOf<PeriodicWorkRequest>()
        every { workManager.enqueueUniquePeriodicWork(any(), any(), capture(requests)) } returns mockk()
        val scheduler = WifiWidgetWorkScheduler(workManager)

        scheduler.applyRefreshingPolicy(WidgetRefreshing(interval = 30.minutes))

        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                "periodic_widget_refreshing",
                ExistingPeriodicWorkPolicy.UPDATE,
                any<PeriodicWorkRequest>()
            )
        }
        assertEquals(Duration.ofMinutes(30).toMillis(), requests.single().workSpec.intervalDuration)
        assertEquals(Duration.ofMinutes(30).toMillis(), requests.single().workSpec.initialDelay)
        assertFalse(requests.single().workSpec.constraints.requiresBatteryNotLow())
    }

    @Test fun `disabled periodic refreshing cancels existing work`() {
        val workManager = mockk<WorkManager>(relaxed = true)
        WifiWidgetWorkScheduler(workManager).applyRefreshingPolicy(WidgetRefreshing(refreshPeriodically = false))

        verify(exactly = 1) { workManager.cancelUniqueWork("periodic_widget_refreshing") }
        verify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any<PeriodicWorkRequest>()) }
    }

    @Test fun `recovery uses unique work with network constraint and backoff`() {
        val workManager = mockk<WorkManager>(relaxed = true)
        val requests = mutableListOf<OneTimeWorkRequest>()
        every { workManager.enqueueUniqueWork(any(), any(), capture(requests)) } returns mockk()
        val scheduler = WifiWidgetWorkScheduler(workManager)

        scheduler.enqueueRecovery()
        scheduler.enqueueRecovery()

        verify(exactly = 2) {
            workManager.enqueueUniqueWork("remote_widget_recovery", ExistingWorkPolicy.KEEP, any<OneTimeWorkRequest>())
        }
        assertEquals(2, requests.size)
        requests.forEach { request ->
            assertEquals(NetworkType.CONNECTED, request.workSpec.constraints.requiredNetworkType)
            assertEquals(BackoffPolicy.EXPONENTIAL, request.workSpec.backoffPolicy)
            assertEquals(RefreshWork.Recovery, RefreshWork.fromWorkData(request.workSpec.input))
        }
    }
}
