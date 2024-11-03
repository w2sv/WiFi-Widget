package com.w2sv.domain.repository

import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow

interface PermissionRepository {
    val locationAccessPermissionRationalShown: DataStoreFlow<Boolean>
    val locationAccessPermissionRequested: DataStoreFlow<Boolean>
}
