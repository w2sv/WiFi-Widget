package com.w2sv.wifiwidget.ui.designsystem.animation

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EntranceStateTest {
    @Test
    fun `elements share progress with staggered clamped windows`() =
        runTest {
            val state = EntranceState(initialProgress = 0f)
            assertEquals(0f, state.elementProgress(0), 0f)

            state.progress.snapTo(0.08f)
            assertEquals(0.16f, state.elementProgress(0), 0.0001f)
            assertEquals(0f, state.elementProgress(1), 0.0001f)

            state.progress.snapTo(0.33f)
            assertEquals(0.5f, state.elementProgress(1), 0.0001f)

            state.progress.snapTo(0.82f)
            assertEquals(1f, state.elementProgress(0), 0.0001f)
            assertEquals(1f, state.elementProgress(4), 0.0001f)
        }

    @Test
    fun `completed progress displays the full sequence immediately`() {
        val state = EntranceState(initialProgress = 1f)
        for (index in 0..4) {
            assertEquals(1f, state.elementProgress(index), 0f)
        }
    }
}
