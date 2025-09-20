package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

// TODO: remove once https://issuetracker.google.com/issues/420443609 has been fixed
@Composable
@Suppress("UNCHECKED_CAST")
fun <T : NavKey> rememberNavBackStack(vararg initialKeys: T): NavBackStack<T> =
    rememberNavBackStack(*initialKeys) as NavBackStack<T>
