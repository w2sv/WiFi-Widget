package com.w2sv.common.datastore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

abstract class DataStoreRepositoryInterfacingViewModel(override val dataStoreRepository: DataStoreRepository) :
    ViewModel(),
    DataStoreRepository.Interface {

    override val coroutineScope: CoroutineScope get() = viewModelScope
}