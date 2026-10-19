package com.w2sv.wifiwidget.storescreenshots.scenario.di

import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentationProvider
import com.w2sv.wifiwidget.storescreenshots.scenario.ActiveScreenshotScenario
import com.w2sv.wifiwidget.ui.screen.home.di.HomeScreenBinderModule
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.LocationEnabledProvider
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.LocationEnabledProviderImpl
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiState
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiStateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

@ViewModelScoped
class ScreenshotWifiStateProvider @Inject constructor(
    activeScenario: ActiveScreenshotScenario,
    presentationProvider: WifiPropertyPresentationProvider
) : WifiStateProvider {
    private val scenario = activeScenario.current
    private val config = scenario.widgetConfig

    override val wifiState: StateFlow<WifiState> = MutableStateFlow(
        WifiState.Connected(
            status = WifiStatus.Connected,
            propertyPresentations = runBlocking {
                presentationProvider(
                    enabledProperties = config.enabledProperties,
                    enabledIpSettings = config::enabledIpSettings,
                    remoteNetworkData = RemoteNetworkData.empty
                )
            }
        )
    )

    override fun onLocationAccessChanged() =
        Unit
}

@Module
@TestInstallIn(components = [ViewModelComponent::class], replaces = [HomeScreenBinderModule::class])
interface ScreenshotHomeScreenModule {
    @Binds
    fun wifiStateProvider(instance: ScreenshotWifiStateProvider): WifiStateProvider

    @Binds
    fun locationEnabledProvider(instance: LocationEnabledProviderImpl): LocationEnabledProvider
}
