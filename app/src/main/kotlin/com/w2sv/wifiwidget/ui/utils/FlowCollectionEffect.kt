package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

@Composable
fun <T> FlowCollectionEffect(flow: Flow<T>, collector: FlowCollector<T>) {
    LaunchedEffect(flow) {
        flow.collect(collector)
    }
}