package com.w2sv.wifiwidget.ui.location

import com.w2sv.domain.location.LocationPermissionLevel
import com.w2sv.domain.widget.WidgetManager
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Test

class WidgetLocationRecoveryObserverTest {
    private val actions = mockk<WidgetManager>(relaxed = true)
    private val observer = WidgetLocationRecoveryObserver(actions)

    @After fun `location access recovery never requests remote refresh`() {
        verify(exactly = 0) { actions.refresh() }
    }

    @Test
    fun `first sufficient observation renders stale widgets without remote refresh`() {
        listOf(LocationPermissionLevel.PreciseForeground, LocationPermissionLevel.PreciseBackground).forEach { level ->
            val process = WidgetLocationRecoveryObserver(actions)
            repeat(3) { process.observe(availability(level)) }
        }
        verify(exactly = 2) { actions.render() }
    }

    @Test
    fun `initial insufficient access and approximate improvements do not render`() {
        observer.observe(availability(LocationPermissionLevel.None))
        observer.observe(availability(LocationPermissionLevel.ApproximateOnly))
        LocationPermissionLevel.entries.forEach { level ->
            WidgetLocationRecoveryObserver(actions).observe(availability(level, enabled = false))
        }
        verify(exactly = 0) { actions.render() }
    }

    @Test
    fun `none and approximate access recover when precise foreground permission arrives`() {
        listOf(LocationPermissionLevel.None, LocationPermissionLevel.ApproximateOnly).forEach { level ->
            observer.observe(availability(level))
            observer.observe(availability(LocationPermissionLevel.PreciseForeground))
        }
        verify(exactly = 2) { actions.render() }
    }

    @Test
    fun `background upgrade renders and subsequent identical observations do not`() {
        observer.observe(availability(LocationPermissionLevel.PreciseForeground))
        repeat(4) { observer.observe(availability(LocationPermissionLevel.PreciseBackground)) }
        verify(exactly = 2) { actions.render() }
    }

    @Test
    fun `services recovery renders with either precise permission level`() {
        listOf(LocationPermissionLevel.PreciseForeground, LocationPermissionLevel.PreciseBackground).forEach { level ->
            observer.observe(availability(level, enabled = false))
            observer.observe(availability(level))
            observer.observe(availability(level))
        }
        verify(exactly = 2) { actions.render() }
    }

    @Test
    fun `permission improvements with services disabled wait for services recovery`() {
        LocationPermissionLevel.entries.forEach { observer.observe(availability(it, enabled = false)) }
        verify(exactly = 0) { actions.render() }

        observer.observe(availability(LocationPermissionLevel.PreciseBackground))
        observer.observe(availability(LocationPermissionLevel.PreciseBackground))
        verify(exactly = 1) { actions.render() }
    }

    @Test
    fun `downgrades do not render and revocation followed by restoration recovers`() {
        observer.observe(availability(LocationPermissionLevel.PreciseBackground))
        observer.observe(availability(LocationPermissionLevel.PreciseForeground))
        observer.observe(availability(LocationPermissionLevel.ApproximateOnly))
        observer.observe(availability(LocationPermissionLevel.None))
        verify(exactly = 1) { actions.render() }

        observer.observe(availability(LocationPermissionLevel.PreciseBackground))
        verify(exactly = 2) { actions.render() }
    }

    @Test
    fun `overlapping activity observations serialize comparison and render`() {
        observer.observe(availability(LocationPermissionLevel.None))
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(4)
        try {
            val observations = List(16) {
                executor.submit {
                    check(start.await(5, TimeUnit.SECONDS))
                    observer.observe(availability(LocationPermissionLevel.PreciseBackground))
                }
            }
            start.countDown()
            observations.forEach { it.get(5, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }
        verify(exactly = 1) { actions.render() }
    }

    private fun availability(level: LocationPermissionLevel, enabled: Boolean = true) =
        LocationAccessAvailability(level, enabled)
}
