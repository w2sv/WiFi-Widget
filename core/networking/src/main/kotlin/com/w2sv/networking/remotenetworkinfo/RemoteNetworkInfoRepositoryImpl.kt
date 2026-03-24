package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.networking.RemoteNetworkInfo
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.networking.remotenetworkinfo.ip_api.IpApiDataFetcher
import com.w2sv.networking.remotenetworkinfo.public_ip.PublicIpFetcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

@Singleton
internal class RemoteNetworkInfoRepositoryImpl @Inject constructor(
    private val ipApiDataFetcher: IpApiDataFetcher,
    private val publicIpFetcher: PublicIpFetcher,
    private val widgetConfigFlow: WidgetConfigFlow
) : RemoteNetworkInfoRepository {

    private val _data = MutableStateFlow(RemoteNetworkInfo.empty)
    override val data = _data.asStateFlow()

    override suspend fun refresh() =
        coroutineScope {
            val config = widgetConfigFlow.first()

            val ipApiDeferred = async { ipApiDataFetcher.fetchIfNecessary(config) }
            val publicIpDeferred = async { publicIpFetcher.fetchIfNecessary(config) }

            _data.value = RemoteNetworkInfo(
                ipApiData = ipApiDeferred.await(),
                publicIps = publicIpDeferred.await().orEmpty()
            )
        }
}
