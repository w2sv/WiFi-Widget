package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SharedSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.utils.SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Stable
class UnconfirmedWidgetConfiguration(
    val wifiProperties: UnconfirmedStateMap<WidgetWifiProperty, Boolean>,
    val subWifiProperties: UnconfirmedStateMap<WidgetWifiProperty.IP.SubProperty, Boolean>,
    val buttonMap: UnconfirmedStateMap<WidgetButton, Boolean>,
    val refreshingParametersMap: UnconfirmedStateMap<WidgetRefreshingParameter, Boolean>,
    val useDynamicColors: UnconfirmedStateFlow<Boolean>,
    val theme: UnconfirmedStateFlow<Theme>,
    val customColorsMap: UnconfirmedStateMap<WidgetColorSection, Int>,
    val opacity: UnconfirmedStateFlow<Float>,
    private val scope: CoroutineScope,
    private val sharedSnackbarVisuals: SharedSnackbarVisuals,
    onStateSynced: suspend () -> Unit
) : UnconfirmedStatesComposition(
    unconfirmedStates = listOf(
        wifiProperties,
        subWifiProperties,
        buttonMap,
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

    fun onLocationAccessPermissionStatusChanged(
        isGranted: Boolean,
        trigger: LocationAccessPermissionRequestTrigger?
    ) {
        when {
            !isGranted -> {
                val changedProperties = setLocationAccessRequiringPropertyEnablementAndSync(false)
                if (changedProperties.isNotEmpty()) {
                    scope.launchDelayed(1_000) {
                        sharedSnackbarVisuals.emit(
                            AppSnackbarVisuals(
                                msg = buildString {
                                    append("Disabled ${changedProperties.first()} ")
                                    changedProperties.getOrNull(1)?.let {
                                        append("& $it ")
                                    }
                                    append("due to location access having been revoked.")
                                },
                                duration = SnackbarDuration.Long,
                                kind = SnackbarKind.Error
                            )
                        )
                    }
                }
            }

            trigger is LocationAccessPermissionRequestTrigger.InitialAppLaunch -> {
                setLocationAccessRequiringPropertyEnablementAndSync(true)
            }

            trigger is LocationAccessPermissionRequestTrigger.PropertyCheckChange -> {
                wifiProperties[trigger.property] = true
            }
        }
    }

    /**
     * @return Boolean, representing whether anything has been changed.
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
