package com.w2sv.wifiwidget.storescreenshots.scenario.di

import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

@Singleton
class ScreenshotWifiStatusSource @Inject constructor() : WifiStatusSource {
    override fun snapshot(): WifiStatus =
        WifiStatus.Connected

    override val status: Flow<WifiStatus> = flowOf(WifiStatus.Connected)

    override val defaultEgressChanges: Flow<Unit> = emptyFlow()
}
