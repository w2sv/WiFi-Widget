package com.w2sv.wifiwidget

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.preferences.DataStoreProperty
import com.w2sv.common.preferences.DataStoreRepository
import kotlinx.coroutines.launch

abstract class DataStoreRepositoryHoldingViewModel(val dataStoreRepository: DataStoreRepository) :
    ViewModel() {

    fun <T> saveToDataStore(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            dataStoreRepository.save(key, value)
        }
    }

    fun <T, P : DataStoreProperty<T>> saveMapToDataStore(
        map: Map<P, T>
    ) {
        viewModelScope.launch {
            dataStoreRepository.saveMap(map)
        }
    }
}