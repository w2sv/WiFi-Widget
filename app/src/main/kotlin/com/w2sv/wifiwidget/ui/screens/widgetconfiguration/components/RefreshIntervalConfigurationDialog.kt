package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.w2sv.common.utils.minutes
import com.w2sv.wheelpicker.WheelPicker
import com.w2sv.wheelpicker.WheelPickerState
import com.w2sv.wheelpicker.rememberWheelPickerState
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import slimber.log.i
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun RefreshIntervalConfigurationDialog(
    interval: Duration,
    setInterval: (Duration) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var configuredInterval by remember(interval) {
        mutableStateOf(interval)
    }
    var isInvalidSelection by rememberSaveable {
        mutableStateOf(false)
    }

    ConfigurationDialog(
        onDismissRequest = onDismissRequest,
        onApplyButtonPress = remember {
            {
                setInterval(configuredInterval)
                onDismissRequest()
            }
        },
        applyButtonEnabled = !isInvalidSelection && configuredInterval != interval,
        title = stringResource(R.string.refresh_interval),
        modifier = modifier
    ) {
        val hourPickerState = rememberWheelPickerState(
            itemCount = 24,
            startIndex = configuredInterval.inWholeHours.toInt(),
            unfocusedItemCountToEitherSide = 2
        )
        val minutePickerState =
            rememberWheelPickerState(
                itemCount = 60,
                startIndex = configuredInterval.minutes,
                unfocusedItemCountToEitherSide = 2
            )

        LaunchedEffect(hourPickerState.snappedIndex, minutePickerState.snappedIndex) {
            i { "${hourPickerState.snappedIndex} ${minutePickerState.snappedIndex}" }

            isInvalidSelection =
                hourPickerState.snappedIndex == 0 && minutePickerState.snappedIndex < 15
            if (!isInvalidSelection) {
                configuredInterval =
                    hourPickerState.snappedIndex.hours + minutePickerState.snappedIndex.minutes
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WheelPickerRow(
                hourWheelPickerState = hourPickerState,
                minuteWheelPickerState = minutePickerState,
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(visible = isInvalidSelection) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.interval_shorter_than_15_minutes_not_possible),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun WheelPickerRow(
    hourWheelPickerState: WheelPickerState,
    minuteWheelPickerState: WheelPickerState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            WheelPicker(
                state = hourWheelPickerState,
                itemSize = itemSize
            ) {
                Text(text = it.toString())
            }
            Text(text = "h")
            Spacer(modifier = Modifier.width(14.dp))
            WheelPicker(
                state = minuteWheelPickerState,
                itemSize = itemSize
            ) {
                Text(text = it.toString())
            }
            Text(text = "m")
        }
        Box(
            modifier = Modifier
                .width(102.dp)
                .height(42.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
        )
    }
}

private val itemSize = DpSize(24.dp, 42.dp)