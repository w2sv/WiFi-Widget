package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.w2sv.androidutils.coroutines.getValueSynchronously
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun <T : R, R> Flow<T>.collectAsStateWithInitial(context: CoroutineContext = EmptyCoroutineContext): State<R> =
    collectAsState(getValueSynchronously(), context)
