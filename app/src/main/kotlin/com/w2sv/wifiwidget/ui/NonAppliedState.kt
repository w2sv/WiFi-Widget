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

abstract class NonAppliedState<T> {
    val stateChanged = MutableStateFlow(false)

    protected fun resetStateChanged() {
        stateChanged.value = false
    }

    abstract val value: T
    abstract suspend fun apply()
    abstract fun reset()
}

class NonAppliedSnapshotStateMap<K : DataStoreProperty<V>, V>(
    private val coroutineScope: CoroutineScope,
    private val appliedFlowMap: Map<K, Flow<V>>,
    private val dataStoreRepository: DataStoreRepository,
    private val map: SnapshotStateMap<K, V> = appliedFlowMap
        .getDeflowedMap()
        .getMutableStateMap(),
    private val onApplyState: (Map<K, V>) -> Unit = {}
) : NonAppliedState<Map<K, V>>(),
    MutableMap<K, V> by map {

    override val value: Map<K, V> get() = this

    override suspend fun apply() {
        dataStoreRepository.saveMap(value)
        onApplyState(value)
        resetStateChanged()
    }

    override fun reset() {
        coroutineScope.launch {
            appliedFlowMap.forEach { (k, v) ->
                map[k] = v.first()
            }
        }
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
    private val updateAppliedState: (T) -> Unit
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

    override suspend fun apply() {
        updateAppliedState(value)
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