package com.w2sv.domain.permission

import com.w2sv.persistedpreferences.PersistedPreference

interface PermissionRepository {
    val locationAccessPermissionRationalShown: PersistedPreference<Boolean>
    val locationAccessPermissionRequested: PersistedPreference<Boolean>
}
