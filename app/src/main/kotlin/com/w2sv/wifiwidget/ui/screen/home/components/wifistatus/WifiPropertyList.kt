package com.w2sv.wifiwidget.ui.screen.home.components.wifistatus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.wifiwidget.ui.designsystem.SecondLevelElevatedCard
import com.w2sv.wifiwidget.ui.util.toAnnotatedString
import kotlinx.collections.immutable.ImmutableList

@Composable
fun WifiPropertyList(presentations: ImmutableList<WifiPropertyPresentation>, modifier: Modifier = Modifier) {
    val onClickScope = rememberPropertyOnClickScope()
    val lastIndex = presentations.lastIndex

    SecondLevelElevatedCard(modifier = modifier) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            itemsIndexed(presentations) { i, presentation ->
                val scope = rememberCoroutineScope()
                val renderedLabel = remember(presentation) {
                    presentation.label.toAnnotatedString(12.sp)
                }
                PropertyDisplay(
                    presentation = presentation,
                    label = renderedLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 26.dp)
                        .clickable {
                            onClickScope.run {
                                onPropertyClick(
                                    scope = scope,
                                    label = renderedLabel,
                                    value = presentation.value,
                                    resolutionError = presentation.resolutionError
                                )
                            }
                        }
                        .padding(horizontal = 8.dp)
                )
                if (i != lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = CardDefaults.elevatedCardColors().containerColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyDisplay(
    presentation: WifiPropertyPresentation,
    label: AnnotatedString,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth(0.4f)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column {
            Text(
                text = presentation.value,
                color = Color.Unspecified.takeUnless { presentation.resolutionError != null } ?: MaterialTheme.colorScheme.error
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
                presentation.subValues.forEach {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}
