package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.material3.rememberSnackbarController
import com.w2sv.core.resources.R
import com.w2sv.domain.wifiproperty.AccessPointNames
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.IconDefaults
import com.w2sv.wifiwidget.ui.designsystem.InfoIcon
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.configlist.ConfigListToken
import com.w2sv.wifiwidget.ui.screen.widgetconfig.dialog.WidgetConfigDialog
import kotlinx.coroutines.launch
import slimber.log.e

private const val EXPORT_FILE_NAME = "access_point_names.txt"

/**
 * Lists the user-assigned access point names and allows for adding, editing, removing, importing and exporting them.
 * Imports are merged into the existing names; files use the `bssid|name` format of [AccessPointNames].
 */
@Composable
fun AccessPointNamesEditor(
    names: AccessPointNames,
    updateNames: (AccessPointNames) -> Unit,
    showDialog: (WidgetConfigDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarController = rememberSnackbarController(LocalSnackbarHostState.current)

    fun showSnackbar(msg: String, kind: SnackbarKind) {
        scope.launch { snackbarController.replaceCurrentWith { AppSnackbarVisuals(msg = msg, kind = kind) } }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val imported = context.readText(uri)?.let(AccessPointNames::parse)
        when {
            imported == null -> showSnackbar(context.getString(R.string.file_access_failed), SnackbarKind.Warning)
            imported.isEmpty -> showSnackbar(context.getString(R.string.no_access_point_names_found_in_file), SnackbarKind.Warning)
            else -> {
                updateNames(names.mergedWith(imported))
                showSnackbar(context.getString(R.string.imported_n_access_point_names, imported.map.size), SnackbarKind.Success)
            }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        if (context.writeText(uri, names.toFileContent())) {
            showSnackbar(context.getString(R.string.exported_access_point_names), SnackbarKind.Success)
        } else {
            showSnackbar(context.getString(R.string.file_access_failed), SnackbarKind.Warning)
        }
    }

    Column(modifier = modifier.padding(end = ConfigListToken.itemSpacing)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = ConfigListToken.itemSpacing)
        ) {
            Text(
                text = stringResource(R.string.access_point_names),
                fontSize = ConfigListToken.FontSize.subSetting,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    showDialog(
                        WidgetConfigDialog.Info(
                            titleRes = R.string.access_point_names_file_format,
                            descriptionRes = R.string.access_point_names_file_format_description
                        )
                    )
                }
            ) {
                InfoIcon(
                    contentDescription = stringResource(R.string.access_point_names_file_format),
                    modifier = Modifier.size(IconDefaults.SizeBig),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (names.isEmpty) {
            Text(
                text = stringResource(R.string.no_access_point_names_defined),
                fontSize = ConfigListToken.FontSize.subSetting,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        names.map.entries.sortedBy { it.value.lowercase() }.forEach { (bssid, name) ->
            AccessPointNameRow(
                bssid = bssid,
                name = name,
                onClick = { showDialog(WidgetConfigDialog.AccessPointNameEditing(bssid, name, isBssidEditable = false)) },
                onRemove = { updateNames(names.without(bssid)) }
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = {
                    val bssid = context.connectedBssid()
                    if (bssid == null) {
                        showSnackbar(context.getString(R.string.not_connected_to_wifi), SnackbarKind.Warning)
                    } else {
                        showDialog(
                            WidgetConfigDialog.AccessPointNameEditing(
                                bssid = bssid,
                                name = names.nameOf(bssid).orEmpty(),
                                isBssidEditable = false
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.name_current_access_point))
            }
            IconButton(onClick = { showDialog(WidgetConfigDialog.AccessPointNameEditing("", "", isBssidEditable = true)) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.edit_access_point_name))
            }
            TextButton(onClick = { importLauncher.launch(arrayOf("text/*")) }) {
                Text(stringResource(R.string.import_))
            }
            TextButton(
                onClick = { exportLauncher.launch(EXPORT_FILE_NAME) },
                enabled = !names.isEmpty
            ) {
                Text(stringResource(R.string.export))
            }
        }
    }
}

@Composable
private fun AccessPointNameRow(
    bssid: String,
    name: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = ConfigListToken.FontSize.subSetting)
            Text(
                text = bssid,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove))
        }
    }
}

/**
 * @return the BSSID of the currently connected access point, or null if not connected or if it's withheld due to missing location access.
 */
@Suppress("DEPRECATION")
private fun Context.connectedBssid(): String? =
    applicationContext
        .getSystemService(WifiManager::class.java)
        ?.connectionInfo
        ?.bssid
        ?.let(AccessPointNames::normalizeBssid)
        ?.takeUnless { it == "02:00:00:00:00:00" }

private fun Context.readText(uri: Uri): String? =
    runCatching { contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }
        .onFailure { e { "Failed reading $uri; $it" } }
        .getOrNull()

private fun Context.writeText(uri: Uri, text: String): Boolean =
    runCatching { contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(text) } != null }
        .onFailure { e { "Failed writing $uri; $it" } }
        .getOrDefault(false)
