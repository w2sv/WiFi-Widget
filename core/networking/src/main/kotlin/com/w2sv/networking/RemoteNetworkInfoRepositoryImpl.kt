package com.w2sv.networking

import com.w2sv.domain.model.RemoteNetworkInfo
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemoteNetworkInfoRepositoryImpl @Inject constructor(
    private val ipApiRepository: IpApiRepository,
    private val publicIpRepository: PublicIpRepository
) : RemoteNetworkInfoRepository {

    private val _data = MutableStateFlow(RemoteNetworkInfo(null, emptyMap()))
    override val data: StateFlow<RemoteNetworkInfo> = _data

    /**
     * Refreshes all network info **on-demand**.
     * Parallelizes the fetches and respects user settings.
     */
    override suspend fun refresh() = coroutineScope {
        i { "Refreshing RemoteNetworkInfo" }

        val ipApiDeferred = async {
            ipApiRepository.refresh()
            ipApiRepository.data.first()
        }

        val publicIpDeferred = async {
            publicIpRepository.refresh()
            publicIpRepository.data.first()
        }

        _data.value = RemoteNetworkInfo(
            ipApiData = ipApiDeferred.await(),
            publicIps = publicIpDeferred.await()
        )
    }
}
