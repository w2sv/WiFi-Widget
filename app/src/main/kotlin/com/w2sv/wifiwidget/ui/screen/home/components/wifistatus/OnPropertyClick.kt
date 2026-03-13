package com.w2sv.wifiwidget.ui.screen.home.components.wifistatus

import android.content.ClipData
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.w2sv.core.common.R
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyResolutionError
import com.w2sv.wifiwidget.ui.LocalLocationAccessCapability
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.LocationAccessCapability
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarController
import com.w2sv.wifiwidget.ui.util.snackbar.rememberSnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class PropertyOnClickScope(
    val clipboard: Clipboard,
    val snackbarController: SnackbarController,
    val locationAccessCapability: LocationAccessCapability
)

@Composable
fun rememberPropertyOnClickScope(): PropertyOnClickScope {
    val clipboard = LocalClipboard.current
    val snackbarController = rememberSnackbarController()
    val locationAccessCapability = LocalLocationAccessCapability.current

    return remember(clipboard, snackbarController, locationAccessCapability) {
        PropertyOnClickScope(
            clipboard = clipboard,
            snackbarController = snackbarController,
            locationAccessCapability = locationAccessCapability
        )
    }
}

fun PropertyOnClickScope.onPropertyClick(
    scope: CoroutineScope,
    label: CharSequence,
    value: String,
    resolutionError: WifiPropertyResolutionError?
) {
    when (resolutionError) {
        null -> scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, value)))
            snackbarController.showReplacing { copiedToClipboardSnackbar(label) }
        }

        WifiPropertyResolutionError.NoLocationAccessPermission -> locationAccessCapability.requestPermission()
        WifiPropertyResolutionError.GpsDisabled -> locationAccessCapability.openLocationSettings()
    }
}

private fun Context.copiedToClipboardSnackbar(label: CharSequence) =
    AppSnackbarVisuals(
        msg = buildAnnotatedString {
            append("${getString(R.string.copied)} ")
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(label)
            }
            append(" ${getString(R.string.to_clipboard)}.")
        },
        kind = SnackbarKind.Success
    )
