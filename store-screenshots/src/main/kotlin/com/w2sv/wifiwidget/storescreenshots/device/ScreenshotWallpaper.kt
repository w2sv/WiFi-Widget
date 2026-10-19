package com.w2sv.wifiwidget.storescreenshots.device

import android.Manifest
import android.app.WallpaperManager
import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Applies a scenario wallpaper and waits for its system color extraction before capture. */
internal class ScreenshotWallpaper(private val screenshotDevice: ScreenshotDevice) {
    private val instrumentation = screenshotDevice.instrumentation
    private val wallpaperManager = WallpaperManager.getInstance(instrumentation.context)

    fun set(wallpaperResource: Int) {
        check(wallpaperManager.isSetWallpaperAllowed) { "The managed device does not allow wallpaper changes." }

        val colorsChanged = CountDownLatch(1)
        val listener = WallpaperManager.OnColorsChangedListener { colors, which ->
            if (which and WallpaperManager.FLAG_SYSTEM != 0 && colors != null) colorsChanged.countDown()
        }
        wallpaperManager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
        try {
            // Instrumentation runs in the app process, so the test APK's manifest permission alone does not grant its UID.
            val uiAutomation = instrumentation.uiAutomation
            uiAutomation.adoptShellPermissionIdentity(Manifest.permission.SET_WALLPAPER)
            val wallpaperId = try {
                wallpaperManager.setResource(wallpaperResource, WallpaperManager.FLAG_SYSTEM)
            } finally {
                uiAutomation.dropShellPermissionIdentity()
            }
            check(wallpaperId != 0 && wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM) == wallpaperId) {
                "The custom wallpaper was not installed."
            }
            check(colorsChanged.await(SYSTEM_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                "Wallpaper colors were not updated after applying resource $wallpaperResource."
            }
            check(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM) != null) {
                "Wallpaper colors are unavailable after applying resource $wallpaperResource."
            }
        } finally {
            wallpaperManager.removeOnColorsChangedListener(listener)
        }

        // System UI defers repeated wallpaper-driven theme updates until the screen turns off.
        screenshotDevice.ui.sleep()
        screenshotDevice.ui.wakeUp()
        screenshotDevice.ui.pressHome()
        screenshotDevice.waitForLauncherToSettle()
    }
}
