package com.w2sv.wifiwidget.ui

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.w2sv.common.extensions.getDeflowedMap
import com.w2sv.common.extensions.getValueSynchronously
import com.w2sv.common.preferences.DataStoreProperty
import com.w2sv.common.preferences.DataStoreRepository
import com.w2sv.wifiwidget.extensions.getMutableStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class NonAppliedState<T>(
    protected val applyState: suspend (T) -> Unit
) {
    val stateChanged = MutableStateFlow(false)
    abstract val value: T

    suspend fun apply() {
        stateChanged.value = false
        applyState(value)
    }

    abstract fun reset()
}

class NonAppliedSnapshotStateMap<K : DataStoreProperty<V>, V>(
    private val coroutineScope: CoroutineScope,
    private val appliedFlowMap: Map<K, Flow<V>>,
    private val dataStoreRepository: DataStoreRepository,
    private val map: SnapshotStateMap<K, V> = appliedFlowMap
        .getDeflowedMap()
        .getMutableStateMap(),
    onApplyState: (Map<K, V>) -> Unit = { it }
) : NonAppliedState<Map<K, V>>({
    dataStoreRepository.saveMap(it)
    onApplyState(it)
}),
    MutableMap<K, V> by map {

    override val value: Map<K, V> get() = this

    override fun reset() {
        coroutineScope.launch {
            appliedFlowMap.forEach { (k, v) ->
                map[k] = v.first()
            }
        }
        stateChanged.value = false
    }

    private val dissimilarKeys = mutableSetOf<K>()

    override fun put(key: K, value: V): V? {
        val previous = map.put(key, value)
        coroutineScope.launch {
            when (value == appliedFlowMap.getValue(key).first()) {
                true -> dissimilarKeys.remove(key)
                false -> dissimilarKeys.add(key)
            }
            stateChanged.value = dissimilarKeys.isNotEmpty()
        }
        return previous
    }
}

class NonAppliedStateFlow<T>(
    private val coroutineScope: CoroutineScope,
    private val appliedFlow: Flow<T>,
    updateAppliedState: (T) -> Unit
) : NonAppliedState<T>(updateAppliedState),
    MutableStateFlow<T> by MutableStateFlow(
        appliedFlow.getValueSynchronously()
    ) {

    init {
        coroutineScope.launch {
            collect {
                stateChanged.value = it != appliedFlow.first()
            }
        }
    }

    override fun reset() {
        coroutineScope.launch {
            value = appliedFlow.first()
        }
        stateChanged.value = false
    }
}

class CoherentNonAppliedStates(
    vararg nonAppliedState: NonAppliedState<*>,
    coroutineScope: CoroutineScope
) : List<NonAppliedState<*>> by nonAppliedState.asList() {

    val requiringUpdate = MutableStateFlow(false)

    init {
        forEach {
            coroutineScope.launch {
                it.stateChanged.collect {
                    requiringUpdate.value = any { it.stateChanged.value }
                }
            }
        }
    }

    suspend fun apply() {
        forEach {
            it.apply()
        }
    }

    fun reset() {
        forEach {
            it.reset()
        }
    }
}