package com.w2sv.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.widget.RemoteViews
import androidx.annotation.StringRes
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.common.preferences.CustomWidgetColors
import com.w2sv.common.preferences.EnumOrdinals
import com.w2sv.common.preferences.FloatPreferences
import com.w2sv.common.preferences.WidgetProperties
import com.w2sv.kotlinutils.extensions.getByOrdinal
import com.w2sv.widget.utils.isWifiConnected
import com.w2sv.widget.utils.setMakeUniqueActivityFlags
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

internal class WidgetPopulator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetProperties: WidgetProperties,
    private val enumOrdinals: EnumOrdinals,
    private val floatPreferences: FloatPreferences,
    private val customWidgetColors: CustomWidgetColors
) {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface EntryPointInterface {
        fun getInstance(): WidgetPopulator
    }

    companion object {
        fun getInstance(context: Context): WidgetPopulator =
            EntryPointAccessors.fromApplication(
                context,
                EntryPointInterface::class.java
            )
                .getInstance()
    }

    // ============
    // Populating
    // ============

    fun populate(widget: RemoteViews): RemoteViews =
        widget.apply {
            setContentLayout()
            setWidgetColors(
                getByOrdinal(enumOrdinals.widgetTheme),
                customWidgetColors,
                floatPreferences.opacity,
                context
            )
            setLastUpdatedTV()
            setOnClickPendingIntents()
        }

    private fun RemoteViews.setContentLayout() {
        when (context.getSystemService(WifiManager::class.java).isWifiEnabled) {
            true -> {
                when (context.getSystemService(ConnectivityManager::class.java).isWifiConnected) {
                    true -> setWifiProperties(context, widgetProperties)
                    false -> setNoConnectionAvailableLayout(com.w2sv.common.R.string.no_wifi_connection)
                }
            }

            false -> setNoConnectionAvailableLayout(com.w2sv.common.R.string.wifi_disabled)
        }
    }

    private fun RemoteViews.setNoConnectionAvailableLayout(@StringRes wifiStatusStringRes: Int) {
        crossVisualize(
            R.id.wifi_properties_layout,
            R.id.no_connection_available_layout
        )
        setTextViewText(R.id.wifi_status_tv, context.getString(wifiStatusStringRes))
    }

    // ============
    // Bottom Row
    // ============

    private fun RemoteViews.setLastUpdatedTV() {
        val now = Date()
        setTextViewText(
            R.id.last_updated_tv,
            "${
                DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
            } ${SimpleDateFormat("EE", Locale.getDefault()).format(now)}"
        )
    }

    private fun RemoteViews.setOnClickPendingIntents() {
        // refresh_button
        setOnClickPendingIntent(
            R.id.refresh_button,
            WifiWidgetProvider.getRefreshDataPendingIntent(context)
        )

        // settings_button
        setOnClickPendingIntent(
            R.id.settings_button,
            PendingIntent.getActivity(
                context,
                PendingIntentCode.LaunchHomeActivity.ordinal,
                Intent.makeRestartActivityTask(
                    ComponentName(
                        context,
                        "com.w2sv.wifiwidget.ui.screens.home.HomeActivity"
                    )
                )
                    .putExtra(
                        WifiWidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START,
                        true
                    ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        // widget_layout
        setOnClickPendingIntent(
            R.id.widget_layout,
            PendingIntent.getActivity(
                context,
                PendingIntentCode.LaunchHomeActivity.ordinal,
                Intent(Settings.ACTION_WIFI_SETTINGS)
                    .setMakeUniqueActivityFlags(),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}