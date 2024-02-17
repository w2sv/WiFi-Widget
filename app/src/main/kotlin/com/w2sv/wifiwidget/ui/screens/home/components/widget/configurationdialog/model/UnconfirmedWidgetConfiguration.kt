package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Stable
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Stable
class UnconfirmedWidgetConfiguration(
    val coloring: UnconfirmedStateFlow<WidgetColoring>,
    val presetColoringData: UnconfirmedStateFlow<WidgetColoring.Data.Preset>,
    val customColoringData: UnconfirmedStateFlow<WidgetColoring.Data.Custom>,
    val opacity: UnconfirmedStateFlow<Float>,
    val fontSize: UnconfirmedStateFlow<FontSize>,
    val wifiProperties: UnconfirmedStateMap<WidgetWifiProperty, Boolean>,
    val ipSubProperties: UnconfirmedStateMap<WidgetWifiProperty.IP.SubProperty, Boolean>,
    val bottomRowMap: UnconfirmedStateMap<WidgetBottomRowElement, Boolean>,
    val refreshingParametersMap: UnconfirmedStateMap<WidgetRefreshingParameter, Boolean>,
    private val scope: CoroutineScope,
    private val mutableSharedSnackbarVisuals: MutableSharedFlow<(Context) -> SnackbarVisuals>,
    onStateSynced: suspend () -> Unit
) : UnconfirmedStatesComposition(
    unconfirmedStates = listOf(
        coloring,
        presetColoringData,
        customColoringData,
        opacity,
        fontSize,
        wifiProperties,
        ipSubProperties,
        bottomRowMap,
        refreshingParametersMap
    ),
    coroutineScope = scope,
    onStateSynced = onStateSynced
) {
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
