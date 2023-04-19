package com.w2sv.wifiwidget.ui.screens.home

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.locationServicesEnabled
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.common.WifiProperty
import com.w2sv.common.datastore.DataStoreRepository
import com.w2sv.common.datastore.DataStoreRepositoryInterfacingViewModel
import com.w2sv.common.datastore.PreferencesKey
import com.w2sv.common.extensions.getValueSynchronously
import com.w2sv.common.extensions.hasPermission
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.NonAppliedStateFlow
import com.w2sv.wifiwidget.ui.screens.home.locationaccesspermission.BACKGROUND_LOCATION_ACCESS_GRANT_REQUIRED
import com.w2sv.wifiwidget.ui.screens.home.locationaccesspermission.LocationAccessPermissionRequestTrigger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    dataStoreRepository: DataStoreRepository,
    @ApplicationContext context: Context
) : DataStoreRepositoryInterfacingViewModel(dataStoreRepository) {

    val nonAppliedInAppTheme = NonAppliedStateFlow(
        viewModelScope,
        dataStoreRepository.inAppTheme
    ) {
        dataStoreRepository.save(PreferencesKey.IN_APP_THEME, it)
    }

    // ========================
    // Widget Pin Listening
    // ========================

    fun onWidgetOptionsUpdated(widgetId: Int, context: Context) {
        if (widgetIds.add(widgetId)) {
            onNewWidgetPinned(widgetId, context)
        }
    }

    private fun onNewWidgetPinned(widgetId: Int, context: Context) {
        i { "Pinned new widget w ID=$widgetId" }
        context.showToast(R.string.pinned_widget)

        viewModelScope.launch {
            if (dataStoreRepository.wifiProperties.getValue(WifiProperty.SSID).first())
                when {
                    !context.locationServicesEnabled -> context.showToast(
                        R.string.on_pin_widget_wo_gps_enabled,
                        Toast.LENGTH_LONG
                    )

                    !context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> context.showToast(
                        R.string.on_pin_widget_wo_location_access_permission,
                        Toast.LENGTH_LONG
                    )

                    (BACKGROUND_LOCATION_ACCESS_GRANT_REQUIRED) && !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && !showBackgroundLocationAccessRational.value -> context.showToast(
                        R.string.on_pin_widget_wo_background_location_access_permission,
                        Toast.LENGTH_LONG
                    )
                }
        }
    }

    private val widgetIds: MutableSet<Int> =
        WidgetProvider.getWidgetIds(context).toMutableSet()

    // =============
    // LAP := Location Access Permission
    // =============

    val lapRationalTrigger: MutableStateFlow<LocationAccessPermissionRequestTrigger?> =
        MutableStateFlow(null)

    val lapRationalShown: Boolean
        get() = dataStoreRepository.locationAccessPermissionRationalShown.getValueSynchronously()

    val lapRequestTrigger: MutableStateFlow<LocationAccessPermissionRequestTrigger?> =
        MutableStateFlow(null)

    val lapRequestLaunchedAtLeastOnce: Boolean
        get() = dataStoreRepository.locationAccessPermissionRequestedAtLeastOnce.getValueSynchronously()

    val showBackgroundLocationAccessRational = MutableStateFlow(false)

    // ==============
    // BackPress Handling
    // ==============

    val exitApplication = MutableSharedFlow<Unit>()

    fun onBackPress(context: Context) {
        backPressHandler.invoke(
            onFirstPress = {
                context.showToast(context.getString(R.string.tap_again_to_exit))
            },
            onSecondPress = {
                viewModelScope.launch {
                    exitApplication.emit(Unit)
                }
            }
        )
    }

    private val backPressHandler = BackPressHandler(
        viewModelScope,
        context.resources.getLong(R.integer.backpress_confirmation_window)
    )
}