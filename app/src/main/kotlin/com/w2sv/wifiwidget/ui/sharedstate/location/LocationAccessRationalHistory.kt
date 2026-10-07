package com.w2sv.wifiwidget.ui.sharedstate.location

import kotlinx.coroutines.flow.Flow

data class LocationAccessRationalHistory(val wasShownBefore: Flow<Boolean>, val recordWasShown: () -> Unit)
