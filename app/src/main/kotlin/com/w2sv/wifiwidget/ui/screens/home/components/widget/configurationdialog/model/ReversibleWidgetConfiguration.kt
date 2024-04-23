package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Stable
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateFlow
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateMap
import com.w2sv.androidutils.ui.reversible_state.ReversibleStatesComposition
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
class ReversibleWidgetConfiguration(
    val coloringConfig: ReversibleStateFlow<WidgetColoring.Config>,
    val opacity: ReversibleStateFlow<Float>,
    val fontSize: ReversibleStateFlow<FontSize>,
    val wifiProperties: ReversibleStateMap<WidgetWifiProperty, Boolean>,
    val ipSubProperties: ReversibleStateMap<WidgetWifiProperty.IP.SubProperty, Boolean>,
    val bottomRowMap: ReversibleStateMap<WidgetBottomRowElement, Boolean>,
    val refreshingParametersMap: ReversibleStateMap<WidgetRefreshingParameter, Boolean>,
    val refreshIntervalMinutes: ReversibleStateFlow<Int>,
    private val scope: CoroutineScope,
    private val mutableSharedSnackbarVisuals: MutableSharedFlow<(Context) -> SnackbarVisuals>,
    onStateSynced: suspend () -> Unit
) : ReversibleStatesComposition(
    reversibleStates = listOf(
        coloringConfig,
        opacity,
        fontSize,
        wifiProperties,
        ipSubProperties,
        bottomRowMap,
        refreshingParametersMap
    ),
    scope = scope,
    onStateSynced = { onStateSynced() }
) {
    val anyLocationAccessRequiringPropertyEnabled: Boolean
        get() = WidgetWifiProperty.NonIP.LocationAccessRequiring.entries
            .any {
                wifiProperties.appliedStateMap.getValue(it).value
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
            if (wifiProperties.appliedStateMap.getValue(it).value != value) {
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
