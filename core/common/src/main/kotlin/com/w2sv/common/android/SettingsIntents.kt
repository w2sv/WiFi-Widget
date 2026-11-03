package com.w2sv.common.android

import android.content.Intent
import android.provider.Settings

object SettingsIntents {

    /**
     * Intent that opens the system Wi-Fi settings screen.
     *
     * Includes FLAG_ACTIVITY_NEW_TASK so it can be launched from non-Activity
     * contexts such as services, broadcast receivers, or app widgets.
     */
    val wifi
        get() = Intent(Settings.ACTION_WIFI_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /**
     * Intent that opens the system Location settings screen where the user can
     * enable or disable location services (GPS).
     *
     * Includes FLAG_ACTIVITY_NEW_TASK so it can be launched from non-Activity
     * contexts such as services, broadcast receivers, or app widgets.
     */
    val location
        get() = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
