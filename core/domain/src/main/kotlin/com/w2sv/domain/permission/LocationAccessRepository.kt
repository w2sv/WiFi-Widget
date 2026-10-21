package com.w2sv.domain.permission

import kotlinx.coroutines.flow.Flow

interface LocationAccessRepository {
    val rationalShown: Flow<Boolean>
    val permissionRequested: Flow<Boolean>

    suspend fun recordRationalShown()
    suspend fun recordPermissionRequested()
}
