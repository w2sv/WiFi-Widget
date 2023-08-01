package com.w2sv.common.extensions

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isLaunchingSuppressed(launchedAtLeastOnce: Boolean): Boolean =
    !shouldShowRationale && launchedAtLeastOnce