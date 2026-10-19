package com.w2sv.wifiwidget.storescreenshots.scenario

import javax.inject.Inject
import javax.inject.Singleton

/** Shares the selected scenario with asynchronous widget rendering callbacks. */
@Singleton
class ActiveScreenshotScenario @Inject constructor() {
    @Volatile
    private var selected: ScreenshotScenario? = null

    fun select(scenario: ScreenshotScenario) {
        selected = scenario
    }

    val current: ScreenshotScenario
        get() = checkNotNull(selected) {
            "No screenshot scenario has been selected before resolving widget properties."
        }
}
