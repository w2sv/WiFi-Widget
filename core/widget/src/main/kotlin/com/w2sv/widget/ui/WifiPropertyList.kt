import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults.defaultTextStyle
import com.w2sv.domain.model.WifiProperty
import com.w2sv.widget.ui.RoundedScrollingLazyColumn

@Composable
internal fun WifiPropertyList(viewData: List<WifiProperty.ViewData>, modifier: GlanceModifier = GlanceModifier) {
    RoundedScrollingLazyColumn(
        modifier = modifier.background(GlanceTheme.colors.secondaryContainer),
        items = viewData,
        itemContentProvider = {
            PropertyRow(
                it,
                GlanceModifier.fillMaxWidth()
            )
        }
    )
}

@Composable
private fun PropertyRow(viewData: WifiProperty.ViewData, modifier: GlanceModifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            viewData.label,
            style = defaultTextStyle.copy(color = GlanceTheme.colors.primary),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(viewData.value)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 200)
@Preview(widthDp = 300, heightDp = 200)
@Composable
private fun Prev() {
    WifiPropertyList(
        viewData = listOf(
            WifiProperty.ViewData.NonIP("SSID", "YourSSID"),
            WifiProperty.ViewData.NonIP("Link Speed", "390 Mbps"),
        ),
        modifier = GlanceModifier.fillMaxSize()
    )
}
