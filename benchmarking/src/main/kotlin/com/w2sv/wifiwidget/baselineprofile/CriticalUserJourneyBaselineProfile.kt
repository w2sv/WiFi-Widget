package com.w2sv.wifiwidget.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.w2sv.wifiwidget.criticalUserJourney
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CriticalUserJourneyBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(packageName = "com.w2sv.wifiwidget") {
            startActivityAndWait()
            device.criticalUserJourney()
        }
    }
}
