package com.w2sv.wifiwidget.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavHostController =
    staticCompositionLocalOf<NavHostController> { throw UninitializedPropertyAccessException("LocalRootNavHostController not yet provided") }