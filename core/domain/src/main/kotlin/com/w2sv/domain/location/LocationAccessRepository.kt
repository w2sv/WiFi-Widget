package com.w2sv.domain.location

import kotlinx.coroutines.flow.Flow

interface LocationAccessRepository {
    val permissionRequested: Flow<Boolean>
    val onboardingCompleted: Flow<Boolean>

    suspend fun recordPermissionRequested()
    suspend fun completeOnboarding()
}
