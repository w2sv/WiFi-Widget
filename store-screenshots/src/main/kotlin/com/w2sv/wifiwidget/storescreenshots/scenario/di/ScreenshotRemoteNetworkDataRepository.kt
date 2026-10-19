package com.w2sv.wifiwidget.storescreenshots.scenario.di

import com.w2sv.domain.networking.RemoteDataConfig
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.RemoteRefreshResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class ScreenshotRemoteNetworkDataRepository @Inject constructor() : RemoteNetworkDataRepository {
    override val data: StateFlow<RemoteNetworkData> = MutableStateFlow(RemoteNetworkData.empty)

    override fun reconciledSnapshot(config: RemoteDataConfig): RemoteNetworkData =
        RemoteNetworkData.empty

    override fun needsRecovery(config: RemoteDataConfig): Boolean =
        false

    override suspend fun refresh(onlyIfMissing: Boolean): RemoteRefreshResult =
        RemoteRefreshResult.Complete
}
