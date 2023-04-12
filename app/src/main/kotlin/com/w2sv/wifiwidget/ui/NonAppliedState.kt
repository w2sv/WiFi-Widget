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
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

abstract class NonAppliedState<T> {
    val stateChanged = MutableStateFlow(false)

    protected fun resetStateChanged(){
        stateChanged.value = false
    }

    abstract val value: T
    abstract suspend fun sync()
    abstract fun reset()
}

class NonAppliedSnapshotStateMap<K : DataStoreProperty<V>, V>(
    private val coroutineScope: CoroutineScope,
    private val appliedFlowMap: Map<K, Flow<V>>,
    private val dataStoreRepository: DataStoreRepository,
    private val map: SnapshotStateMap<K, V> = appliedFlowMap
        .getDeflowedMap()
        .getMutableStateMap(),
    private val onStateSynced: (Map<K, V>) -> Unit = {}
) : NonAppliedState<Map<K, V>>(),
    MutableMap<K, V> by map {

    override val value: Map<K, V> get() = this

    override suspend fun sync() {
        dataStoreRepository.saveMap(value)
        onStateSynced(value)
        dissimilarKeys.clear()
        resetStateChanged()
    }

    override fun reset() {
        coroutineScope.launch {
            appliedFlowMap.forEach { (k, v) ->
                map[k] = v.first()
            }
        }
        dissimilarKeys.clear()
        resetStateChanged()
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
    private val syncState: (T) -> Unit
) : NonAppliedState<T>(),
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

    override suspend fun sync() {
        syncState(value)
        resetStateChanged()
    }

    override fun reset() {
        coroutineScope.launch {
            value = appliedFlow.first()
        }
        resetStateChanged()
    }
}

class CoherentNonAppliedStates(
    vararg nonAppliedState: NonAppliedState<*>,
    coroutineScope: CoroutineScope
) : List<NonAppliedState<*>> by nonAppliedState.asList() {

    val stateChanged = MutableStateFlow(false)

    private val changedStateIndices = mutableSetOf<Int>()

    init {
        coroutineScope.launch {
            mapIndexed { i, it -> it.stateChanged.transform { emit(it to i) } }
                .merge()
                .collect { (stateChanged, i) ->
                    if (stateChanged) {
                        changedStateIndices.add(i)
                    } else {
                        changedStateIndices.remove(i)
                    }

                    this@CoherentNonAppliedStates.stateChanged.value = changedStateIndices.isNotEmpty()
                }
        }
    }

    suspend fun sync() {
        forEach {
            if (it.stateChanged.value) {
                it.sync()
            }
        }
        changedStateIndices.clear()
    }

    fun reset() {
        forEach {
            if (it.stateChanged.value) {
                it.reset()
            }
        }
        changedStateIndices.clear()
    }
}