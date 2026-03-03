package com.w2sv.wifiwidget.ui.sharedstate.location

import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.wifiproperty.WifiProperty

@Immutable
sealed interface OnLocationAccessGranted {

    val asEnabledPropertyOrNull: EnableProperty?
        get() = this as? EnableProperty

    @Immutable
    data object EnableLocationAccessRequiringProperties : OnLocationAccessGranted

    @Immutable
    data object TriggerWidgetDataRefresh : OnLocationAccessGranted

    @Immutable
    @JvmInline
    value class EnableProperty(val property: WifiProperty) : OnLocationAccessGranted
}
