package com.w2sv.wifiwidget.ui.designsystem.animation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.test.ComposeUiTestConfig
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EntranceTest {
    private val durationScale = object : MotionDurationScale {
        override var scaleFactor = 1f
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>(ComposeUiTestConfig(durationScale))

    @Test
    fun `recomposition preserves the shared animation and it completes`() {
        composeRule.mainClock.autoAdvance = false
        val recompose = mutableIntStateOf(0)
        lateinit var state: EntranceState
        composeRule.setContent {
            recompose.intValue
            state = rememberEntranceState()
        }
        composeRule.mainClock.advanceTimeBy(200)
        val original = state
        val progress = state.progress.value
        assertTrue(progress > 0f && progress < 1f)

        composeRule.runOnIdle { recompose.intValue++ }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle {
            assertSame(original, state)
            assertTrue(state.progress.value >= progress)
        }

        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.runOnIdle { assertEquals(1f, state.progress.value, 0f) }
    }

    @Test
    fun `initial animation can be disabled`() {
        composeRule.mainClock.autoAdvance = false
        lateinit var state: EntranceState
        composeRule.setContent { state = rememberEntranceState(animate = false) }

        composeRule.runOnIdle {
            assertEquals(1f, state.progress.value, 0f)
            assertFalse(state.progress.isRunning)
        }
    }

    @Test
    fun `restoration starts fully visible instead of replaying entrance`() {
        composeRule.mainClock.autoAdvance = false
        val restoration = StateRestorationTester(composeRule)
        var state: EntranceState? = null
        val initialProgresses = mutableListOf<Float>()
        restoration.setContent {
            val currentState = rememberEntranceState()
            if (state !== currentState) initialProgresses += currentState.progress.value
            state = currentState
        }
        composeRule.mainClock.advanceTimeBy(200)
        val original = checkNotNull(state)
        assertTrue(original.progress.value < 1f)

        // The restoration tester needs recomposition frames to dispose and rebuild its content.
        composeRule.mainClock.autoAdvance = true
        restoration.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            assertNotSame(original, state)
            assertEquals(listOf(0f, 1f), initialProgresses)
        }
    }

    @Test
    fun `disabled system animations complete without the entrance delay`() {
        durationScale.scaleFactor = 0f
        composeRule.mainClock.autoAdvance = false
        lateinit var state: EntranceState
        composeRule.setContent { state = rememberEntranceState() }
        composeRule.mainClock.advanceTimeBy(64)

        composeRule.runOnIdle { assertEquals(1f, state.progress.value, 0f) }
    }
}
