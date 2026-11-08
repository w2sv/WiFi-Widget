package com.w2sv.widget.ui.actions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import com.w2sv.androidutils.content.getParcelableCompat
import com.w2sv.androidutils.content.toDebugString
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.androidutils.widget.makeToast
import com.w2sv.core.resources.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.parcelize.Parcelize
import slimber.log.i

/** Copies the Glance row's immutable payload and exits even when its arguments are invalid. */
@AndroidEntryPoint
internal class CopyPropertyToClipboardActivity : ComponentActivity() {

    @Inject
    lateinit var toastManager: ToastManager

    @Inject
    lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        i { "onCreate | intent=${intent.toDebugString()}" }

        copyPropertyFromIntent(intent, clipboardManager) { label ->
            toastManager.cancelPreviousAndShow(
                applicationContext.makeToast(
                    resources.getHtmlFormattedText(R.string.copied_to_clipboard, label),
                    Toast.LENGTH_SHORT
                )
            )
        }

        finishAndRemoveTask()
    }

    /** Row-specific clipboard payload; each Glance action has its own identity. */
    @Parcelize
    data class Property(val label: String, val value: String) : Parcelable {

        companion object {
            fun intent(propertyLabel: String, propertyValue: String): Intent =
                Intent().putExtra(
                    EXTRA,
                    Property(
                        label = propertyLabel,
                        value = propertyValue
                    )
                )

            fun fromIntent(intent: Intent): Property? =
                intent.getParcelableCompat<Property>(EXTRA)

            private const val EXTRA = "com.w2sv.wifiwidget.extra.CopyPropertyToClipboardBroadcastReceiverArgs"
        }
    }
}

/** Copies the property supplied by the intent and reports its label on success. */
@VisibleForTesting
fun copyPropertyFromIntent(
    intent: Intent,
    clipboardManager: ClipboardManager,
    onCopied: (String) -> Unit
) {
    val property = CopyPropertyToClipboardActivity.Property.fromIntent(intent) ?: return
    if (property.label.isBlank() || property.value.isBlank()) return

    @Suppress("UsePropertyAccessSyntax")
    clipboardManager.setPrimaryClip(ClipData.newPlainText(property.label, property.value))
    onCopied(property.label)
}
