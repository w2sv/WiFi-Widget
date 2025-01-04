package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog

import androidx.compose.foundation.ScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.OnChange
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.DialogButton
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.sharedviewmodel.WidgetViewModel
import com.w2sv.wifiwidget.ui.utils.activityViewModel

/**
 * The scroll (pixel) value beyond which the dialog will be triggered. Corresponds to a scroll position where the WiFi property configuration card header
 * is at the very top of the screen.
 */
private const val DISPLAY_SCROLL_THRESHOLD = 1300

@Composable
fun OptionalPropertyReorderingDiscoveryDialog(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    widgetVM: WidgetViewModel = activityViewModel()
) {
    var displayPropertyReorderingDiscoveryDialog by rememberSaveable { mutableStateOf(false) }
    if (displayPropertyReorderingDiscoveryDialog) {
        PropertyReorderingDiscoveryDialog(
            modifier = modifier,
            onDismissRequest = {
                widgetVM.savePropertyReorderingDiscoveryShown()
                displayPropertyReorderingDiscoveryDialog = false
            }
        )
    }

    val propertyReorderingDiscoveryShown by widgetVM.propertyReorderingDiscoveryShown.collectAsStateWithLifecycle()

    OnChange(scrollState.value) {
        if (it > DISPLAY_SCROLL_THRESHOLD && !propertyReorderingDiscoveryShown) {
            displayPropertyReorderingDiscoveryDialog = true
        }
    }
}

@Composable
private fun PropertyReorderingDiscoveryDialog(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.okay))
            }
        },
        modifier = modifier,
        icon = { InfoIcon() },
        text = { Text(stringResource(R.string.property_reordering_discovery_dialog_text)) },
        properties = DialogProperties(dismissOnClickOutside = false)
    )
}
