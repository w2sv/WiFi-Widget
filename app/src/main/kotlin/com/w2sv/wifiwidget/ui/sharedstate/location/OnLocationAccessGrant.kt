package com.w2sv.wifiwidget.ui.sharedstate.location

import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.wifiproperty.WifiProperty

@Immutable
sealed interface OnLocationAccessGrant {
    @Immutable
    data object EnableLocationAccessDependentProperties : OnLocationAccessGrant

    @Immutable
    data object TriggerWidgetDataRefresh : OnLocationAccessGrant

    @Immutable
    @JvmInline
    value class EnableProperty(val property: WifiProperty) : OnLocationAccessGrant
}
