package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.repository.RemoteWifiDataRepository
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
internal class RemoteWifiDataRepositoryImpl @Inject constructor(
    private val ipApiDataFetcher: IpApiDataFetcher,
    private val publicIpFetcher: PublicIpFetcher,
    private val widgetConfigFlow: WidgetConfigFlow
) : RemoteWifiDataRepository {

    private val _data = MutableStateFlow(RemoteWifiData.empty)
    override val data = _data.asStateFlow()

    override suspend fun refresh() =
        coroutineScope {
            val config = widgetConfigFlow.first()

            val ipApiDeferred = async { ipApiDataFetcher.fetchIfNecessary(config) }
            val publicIpDeferred = async { publicIpFetcher.fetchIfNecessary(config) }

            _data.value = RemoteWifiData(
                ipApiData = ipApiDeferred.await(),
                publicIps = publicIpDeferred.await().orEmpty()
            )
        }
}
