package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.datastore.preferences.PersistedValue

interface PermissionRepository {
    val locationAccessPermissionRationalShown: PersistedValue.UniTyped<Boolean>
    val locationAccessPermissionRequested: PersistedValue.UniTyped<Boolean>
}