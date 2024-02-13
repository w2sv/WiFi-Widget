package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.compose.runtime.Stable
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.di.MutableSharedSnackbarVisualsFlow
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionStatus
import com.w2sv.wifiwidget.ui.utils.SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Stable
class UnconfirmedWidgetConfiguration(
    val wifiProperties: UnconfirmedStateMap<WidgetWifiProperty, Boolean>,
    val ipSubProperties: UnconfirmedStateMap<WidgetWifiProperty.IP.SubProperty, Boolean>,
    val bottomBar: UnconfirmedStateMap<WidgetBottomBarElement, Boolean>,
    val refreshingParametersMap: UnconfirmedStateMap<WidgetRefreshingParameter, Boolean>,
    val useDynamicColors: UnconfirmedStateFlow<Boolean>,
    val theme: UnconfirmedStateFlow<Theme>,
    val customColorsMap: UnconfirmedStateMap<WidgetColorSection, Int>,
    val opacity: UnconfirmedStateFlow<Float>,
    private val scope: CoroutineScope,
    private val mutableSharedSnackbarVisuals: MutableSharedSnackbarVisualsFlow,
    onStateSynced: suspend () -> Unit
) : UnconfirmedStatesComposition(
    unconfirmedStates = listOf(
        wifiProperties,
        ipSubProperties,
        bottomBar,
        refreshingParametersMap,
        useDynamicColors,
        theme,
        customColorsMap,
        opacity,
    ),
    coroutineScope = scope,
    onStateSynced = onStateSynced
) {
    val customThemeSelected = theme
        .map {
            it == Theme.Custom
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT),
            initialValue = false
        )

    val anyLocationAccessRequiringPropertyEnabled: Boolean
        get() = WidgetWifiProperty.NonIP.LocationAccessRequiring.entries
            .any {
                wifiProperties.persistedStateFlowMap.getValue(it).value
            }

    fun onLocationAccessPermissionStatusChanged(
        status: LocationAccessPermissionStatus
    ) {
        when (status) {
            is LocationAccessPermissionStatus.NotGranted -> {
                val changedProperties = setLocationAccessRequiringPropertyEnablementAndSync(false)
                if (changedProperties.isNotEmpty()) {
                    scope.launchDelayed(1_000) {
                        mutableSharedSnackbarVisuals.emit {
                            AppSnackbarVisuals(
                                msg = buildString {
                                    append("Disabled ${changedProperties.first()} ")
                                    changedProperties.getOrNull(1)?.let {
                                        append("& $it ")
                                    }
                                    append("due to location access having been revoked.")
                                },
                                kind = SnackbarKind.Error
                            )
                        }
                    }
                }
            }

            is LocationAccessPermissionStatus.Granted -> {
                when (status.trigger) {
                    is LocationAccessPermissionRequestTrigger.InitialAppLaunch -> {
                        setLocationAccessRequiringPropertyEnablementAndSync(true)
                    }

                    is LocationAccessPermissionRequestTrigger.PropertyCheckChange -> {
                        wifiProperties[status.trigger.property] = true
                    }

                    else -> Unit
                }
            }
        }
    }

    /**
     * @return LocationAccessRequiring WidgetWifiProperties whose enablement status has changed.
     */
    private fun setLocationAccessRequiringPropertyEnablementAndSync(value: Boolean): List<WidgetWifiProperty.NonIP.LocationAccessRequiring> {
        val changedProperties = mutableListOf<WidgetWifiProperty.NonIP.LocationAccessRequiring>()
        WidgetWifiProperty.NonIP.LocationAccessRequiring.entries.forEach {
            if (wifiProperties.persistedStateFlowMap.getValue(it).value != value) {
                wifiProperties[it] = value
                changedProperties.add(it)
            }
        }

        if (changedProperties.isNotEmpty()) {
            scope.launch { wifiProperties.sync() }
        }

        return changedProperties
    }
}
