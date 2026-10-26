package com.w2sv.domain.location

import kotlinx.coroutines.flow.Flow

interface LocationAccessRepository {
    val rationalShown: Flow<Boolean>
    val permissionRequested: Flow<Boolean>

    suspend fun recordRationalShown()
    suspend fun recordPermissionRequested()
}
