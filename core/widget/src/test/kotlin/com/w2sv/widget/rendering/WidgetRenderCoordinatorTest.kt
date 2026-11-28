package com.w2sv.widget.rendering

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.test.core.app.ApplicationProvider
import com.w2sv.widget.ui.WifiGlanceWidget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetRenderCoordinatorTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before fun setUp() {
        mockkStatic("androidx.glance.appwidget.GlanceAppWidgetKt")
        coEvery { any<WifiGlanceWidget>().updateAll(context) } returns Unit
    }

    @After fun tearDown() {
        unmockkStatic("androidx.glance.appwidget.GlanceAppWidgetKt")
    }

    @Test fun `overlapping dispatch waits and render bursts conflate to the latest revision`() =
        runTest {
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            var dispatches = 0
            coEvery { any<WifiGlanceWidget>().updateAll(context) } coAnswers {
                if (++dispatches == 1) {
                    entered.complete(Unit)
                    release.await()
                }
            }
            val coordinator = WidgetRenderCoordinator(context, backgroundScope)
            coordinator.requestRender()
            entered.await()
            repeat(8) { coordinator.requestRender() }
            val workerDelivery = async { coordinator.dispatchPending() }
            runCurrent()
            assertFalse(workerDelivery.isCompleted)
            release.complete(Unit)
            workerDelivery.await()
            runCurrent()
            coordinator.dispatchPending()
            assertEquals(9L, coordinator.revisions.value)
            assertEquals(2, dispatches)
        }

    @Test fun `failed dispatch can retry without losing later requests`() =
        runTest {
            var attempts = 0
            coEvery { any<WifiGlanceWidget>().updateAll(context) } coAnswers {
                if (++attempts == 1) error("temporary failure")
            }
            val coordinator = WidgetRenderCoordinator(context, backgroundScope)
            coordinator.requestRender()
            runCurrent()
            coordinator.dispatchPending()
            coordinator.requestRender()
            runCurrent()
            assertEquals(3, attempts)
        }

    @Test fun `independent initialization always establishes a fresh baseline without dispatch`() =
        runTest {
            val coordinator = WidgetRenderCoordinator(context, backgroundScope)
            coordinator.withPresentationInitialization { assertEquals(1L, coordinator.revisions.value) }
            coordinator.invalidatePresentation()
            // That revision could have been prepared long ago while this widget had no session.
            coordinator.withPresentationInitialization { assertEquals(3L, coordinator.revisions.value) }
            runCurrent()
            coVerify(exactly = 0) { any<WifiGlanceWidget>().updateAll(context) }
        }

    @Test fun `overlapping initializations share a baseline`() =
        runTest {
            val coordinator = WidgetRenderCoordinator(context, backgroundScope)
            val release = CompletableDeferred<Unit>()
            val first = async(start = CoroutineStart.UNDISPATCHED) {
                coordinator.withPresentationInitialization {
                    assertEquals(1L, coordinator.revisions.value)
                    release.await()
                }
            }
            val second = async(start = CoroutineStart.UNDISPATCHED) {
                coordinator.withPresentationInitialization {
                    assertEquals(1L, coordinator.revisions.value)
                    release.await()
                }
            }
            release.complete(Unit)
            first.await()
            second.await()
            runCurrent()
            coVerify(exactly = 0) { any<WifiGlanceWidget>().updateAll(context) }
        }

    @Test fun `older cancellation cannot remove a replacements initialization`() =
        runTest {
            val coordinator = WidgetRenderCoordinator(context, backgroundScope)
            val older = async(start = CoroutineStart.UNDISPATCHED) {
                coordinator.withPresentationInitialization { awaitCancellation() }
            }
            coordinator.invalidatePresentation()
            val release = CompletableDeferred<Unit>()
            val replacement = async(start = CoroutineStart.UNDISPATCHED) {
                coordinator.withPresentationInitialization {
                    assertEquals(3L, coordinator.revisions.value)
                    release.await()
                }
            }
            older.cancel()
            older.join()
            coordinator.withPresentationInitialization { assertEquals(3L, coordinator.revisions.value) }
            release.complete(Unit)
            replacement.await()
            runCurrent()
            coVerify(exactly = 0) { any<WifiGlanceWidget>().updateAll(context) }
        }

    @Test fun `system data invalidation does not dispatch Glance work`() =
        runTest {
            val coordinator = WidgetRenderCoordinator(context, backgroundScope)
            coordinator.invalidatePresentation()
            assertEquals(1L, coordinator.revisions.value)
            runCurrent()
            coVerify(exactly = 0) { any<WifiGlanceWidget>().updateAll(context) }
        }
}
