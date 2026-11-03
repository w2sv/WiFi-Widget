package com.w2sv.wifiwidget.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.wifiwidget.BuildConfig
import com.w2sv.wifiwidget.ui.location.LocationAccessDependencies
import com.w2sv.wifiwidget.ui.screen.widgetconfig.WidgetConfigRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var locationAccessDependencies: LocationAccessDependencies

    @Inject
    lateinit var widgetActions: WidgetActions

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        val widgetId = initializeWidgetConfiguration() ?: return

        setContent {
            WifiWidgetUI(versionName = BuildConfig.VERSION_NAME, locationAccessDependencies = locationAccessDependencies) {
                WidgetConfigRoute(
                    navigateBack = ::finish,
                    completeConfiguration = { completeWidgetConfiguration(widgetId) }
                )
            }
        }
    }

    /** Defaults the host result to cancellation before accepting a launcher-supplied widget ID. */
    private fun initializeWidgetConfiguration(): Int? {
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return null
        }
        return widgetId
    }

    /** Requests the provider update lifecycle because the host may omit its initial update. */
    private fun completeWidgetConfiguration(widgetId: Int) {
        widgetActions.requestUpdate(widgetId)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
    }
}
