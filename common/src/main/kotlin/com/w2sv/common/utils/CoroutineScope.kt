package com.w2sv.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> CoroutineScope.launchFlowCollections(vararg collectionArgs: Pair<Flow<T>, FlowCollector<T>>) {
    collectionArgs.forEach { (flow, collector) ->
        launch {
            flow.collect(collector)
        }
    }
}