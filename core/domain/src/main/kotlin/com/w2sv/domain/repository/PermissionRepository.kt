package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlow

interface PermissionRepository {
    val locationAccessPermissionRationalShown: DataStoreFlow<Boolean>
    val locationAccessPermissionRequested: DataStoreFlow<Boolean>
}