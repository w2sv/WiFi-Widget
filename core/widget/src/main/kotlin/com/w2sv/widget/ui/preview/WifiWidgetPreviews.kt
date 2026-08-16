package com.w2sv.widget.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.preview.Preview
import com.w2sv.widget.ui.content.WifiWidgetContent

@Preview(widthDp = 160, heightDp = 100)
@Preview(widthDp = 250, heightDp = 140)
@Preview(widthDp = 320, heightDp = 220)
@Composable
private fun ConnectedWifiWidgetPreview() {
    WifiWidgetContent(connectedWifiWidgetPreviewState())
}

@Preview(widthDp = 250, heightDp = 140)
@Composable
private fun DisabledWifiWidgetPreview() {
    WifiWidgetContent(disabledWifiWidgetPreviewState())
}

@Preview(widthDp = 250, heightDp = 140)
@Composable
private fun NotConnectedWifiWidgetPreview() {
    WifiWidgetContent(notConnectedWifiWidgetPreviewState())
}

internal val wifiWidgetPreviewSizes = setOf(
    DpSize(160.dp, 100.dp),
    DpSize(250.dp, 140.dp),
    DpSize(320.dp, 220.dp)
)
