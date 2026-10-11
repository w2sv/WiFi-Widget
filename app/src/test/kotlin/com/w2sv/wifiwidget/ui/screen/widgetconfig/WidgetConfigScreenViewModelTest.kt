package com.w2sv.wifiwidget.ui.screen.widgetconfig

import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.domain.repository.WidgetConfigSource
import com.w2sv.widget.actions.WidgetActions
import com.w2sv.wifiwidget.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WidgetConfigScreenViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val persisted = MutableStateFlow(WidgetConfig.default)
    private val dataSource = mockk<WidgetConfigDataSource> {
        every { config } returns persisted
        coEvery { update(any()) } coAnswers {
            persisted.value = firstArg<(WidgetConfig) -> WidgetConfig>()(persisted.value)
        }
    }
    private val configSource = mockk<WidgetConfigSource> {
        coEvery { snapshot() } answers { persisted.value }
    }
    var configurationChangeHasBeenApplied = false
    private val actions = mockk<WidgetActions>(relaxed = true) {
        every { applyConfigurationChange(any(), any()) } answers { configurationChangeHasBeenApplied = true }
    }

    @Test fun `successful commit calls applyConfigurationChange before emitting on changesHaveBeenCommitted`() =
        runTest {
            val viewModel = WidgetConfigScreenViewModel(dataSource, configSource, actions)
            val previous = persisted.value
            val edited = previous.withUpdatedPropertyPosition(0, 1)

            val notified = backgroundScope.launch(mainDispatcherRule.dispatcher) {
                viewModel.changesHaveBeenCommitted.first()
                assertTrue(configurationChangeHasBeenApplied)
            }

            viewModel.reversibleConfig.value = edited
            viewModel.reversibleConfig.commit()
            notified.join()

            assertEquals(edited, persisted.value)
            coVerify(exactly = 1) { configSource.snapshot() }
            verify(exactly = 1) { actions.applyConfigurationChange(previous, edited) }
        }

    @Test fun `failed persistence has no widget side effects or commit notification`() =
        runTest {
            coEvery { dataSource.update(any()) } throws IllegalStateException("write failed")
            val viewModel = WidgetConfigScreenViewModel(dataSource, configSource, actions)
            val notifications = mutableListOf<Unit>()
            backgroundScope.launch(mainDispatcherRule.dispatcher) {
                viewModel.changesHaveBeenCommitted.collect { notifications += it }
            }
            viewModel.reversibleConfig.value = persisted.value.withUpdatedPropertyPosition(0, 1)

            assertFailsWith<IllegalStateException> { viewModel.reversibleConfig.commit() }

            assertEquals(WidgetConfig.default, persisted.value)
            assertTrue(notifications.isEmpty())
            verify(exactly = 0) { actions.applyConfigurationChange(any(), any()) }
        }
}
