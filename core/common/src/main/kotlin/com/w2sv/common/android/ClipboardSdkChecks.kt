package com.w2sv.common.android

import android.os.Build

/** Android 13 and newer provide system clipboard confirmation. */
val requiresCustomClipboardConfirmation: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
