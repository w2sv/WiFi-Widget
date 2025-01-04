package com.w2sv.wifiwidget.states

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.LocationAccessPermissionStatus
import com.w2sv.wifiwidget.ui.states.LocationAccessState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class LocationAccessStateTest {

    @Test
    fun `newStatus emits on multiplePermissionsState allPermissionsGranted changes`() = runTest {
        val multiplePermissionsState = MultiplePermissionsStateTestImpl()

        locationAccessState(multiplePermissionsState).newStatus.test {
            Snapshot.withMutableSnapshot { multiplePermissionsState.allPermissionsGrantedState.value = true }
            assertEquals(LocationAccessPermissionStatus.Granted(null), awaitItem())

            Snapshot.withMutableSnapshot { multiplePermissionsState.allPermissionsGrantedState.value = false }
            assertEquals(LocationAccessPermissionStatus.NotGranted, awaitItem())
        }
    }

    @Test
    fun `saveRequestLaunchedBefore called on requestResult emission while requestLaunchedBefore false and not called if requestLaunchedBefore true`() =
        runTest {
            val requestResult = MutableSharedFlow<Boolean>()
            val requestLaunchedBefore = MutableStateFlow(false)
            var saveRequestLaunchedBeforeCallCount = 0

            locationAccessState(
                requestResult = requestResult,
                requestLaunchedBefore = requestLaunchedBefore,
                saveRequestLaunchedBefore = { saveRequestLaunchedBeforeCallCount += 1 },
            )

            requestResult.emit(false)
            assertEquals(1, saveRequestLaunchedBeforeCallCount)

            requestLaunchedBefore.value = true
            requestResult.emit(false)
            assertEquals(1, saveRequestLaunchedBeforeCallCount)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun locationAccessState(
    permissionsState: MultiplePermissionsStateTestImpl = MultiplePermissionsStateTestImpl(),
    requestResult: MutableSharedFlow<Boolean> = MutableSharedFlow(),
    requestLaunchedBefore: MutableStateFlow<Boolean> = MutableStateFlow(false),
    saveRequestLaunchedBefore: () -> Unit = {},
    rationalShown: MutableStateFlow<Boolean> = MutableStateFlow(false),
    saveRationalShown: () -> Unit = {},
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    scope: CoroutineScope = TestScope(UnconfinedTestDispatcher()),
    context: Context = ApplicationProvider.getApplicationContext()
): LocationAccessState {
    return LocationAccessState(
        permissionsState = permissionsState,
        requestResult = requestResult,
        backgroundAccessState = null,
        requestLaunchedBefore = requestLaunchedBefore,
        saveRequestLaunchedBefore = saveRequestLaunchedBefore,
        rationalShown = rationalShown,
        saveRationalShown = saveRationalShown,
        snackbarHostState = snackbarHostState,
        scope = scope,
        context = context
    )
}

private class MultiplePermissionsStateTestImpl : MultiplePermissionsState {
    override val permissions: List<PermissionState> = emptyList()
    override val revokedPermissions: List<PermissionState> = emptyList()

    val allPermissionsGrantedState = mutableStateOf(false)
    override val allPermissionsGranted: Boolean get() = allPermissionsGrantedState.value

    val shouldShowRationaleState = mutableStateOf(false)
    override val shouldShowRationale: Boolean get() = shouldShowRationaleState.value

    override fun launchMultiplePermissionRequest() {}
}
