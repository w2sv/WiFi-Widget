package com.w2sv.wifiwidget.ui

import androidx.activity.ComponentActivity
import com.w2sv.domain.widget.WidgetActions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import slimber.log.w

/** Used exclusively by screenshot generation tasks to request widget pinning. */
@AndroidEntryPoint
class WidgetPinActivity : ComponentActivity() {

    @Inject
    lateinit var widgetActions: WidgetActions

    private var pinRequested = false

    override fun onResume() {
        super.onResume()
        if (pinRequested) return
        pinRequested = true

        try {
            widgetActions.pin { w { "Widget pin request was unsupported or rejected" } }
        } finally {
            finish()
        }
    }
}
