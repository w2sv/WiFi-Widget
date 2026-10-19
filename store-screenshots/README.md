# Play Store screenshots

This Android test module captures the real app and launcher widgets on three Gradle Managed Devices: Pixel 6 (phone), Nexus 7 (tablet), and Pixel Tablet (large tablet). `ScreenshotScenarios` supplies each widget screenshot and its paired in-app screenshot. The six numbered PNGs are copied to `app/src/main/play/listings/en-US/graphics/<device>-screenshots/` in their intended Play listing order.

Run commands from the repository root.

## Widget screenshots

Pass AGP's instrumentation class filter with a widget task so that only `WidgetLauncherScreenshotTest` runs. Each command captures, validates, and copies the three `*-widget-*.png` files in one Gradle invocation.

```bash
./gradlew :store-screenshots:generateWidgetPhoneScreenshots \
  -Pandroid.testInstrumentationRunnerArguments.class=com.w2sv.wifiwidget.storescreenshots.WidgetLauncherScreenshotTest

./gradlew :store-screenshots:generateWidgetTabletScreenshots \
  -Pandroid.testInstrumentationRunnerArguments.class=com.w2sv.wifiwidget.storescreenshots.WidgetLauncherScreenshotTest

./gradlew :store-screenshots:generateWidgetLargeTabletScreenshots \
  -Pandroid.testInstrumentationRunnerArguments.class=com.w2sv.wifiwidget.storescreenshots.WidgetLauncherScreenshotTest

./gradlew :store-screenshots:generateWidgetScreenshots \
  -Pandroid.testInstrumentationRunnerArguments.class=com.w2sv.wifiwidget.storescreenshots.WidgetLauncherScreenshotTest
```

## In-app screenshots

AGP's instrumentation class filter is a build-wide Gradle property. Pass it with an in-app task so that only `InAppScreenshotTest` runs. Each command captures, validates, and copies the three `*-app-*.png` files in one Gradle invocation.

```bash
./gradlew :store-screenshots:generateInAppPhoneScreenshots \
  -Pandroid.testInstrumentationRunnerArguments.class=com.w2sv.wifiwidget.storescreenshots.InAppScreenshotTest

./gradlew :store-screenshots:generateInAppTabletScreenshots \
  -Pandroid.testInstrumentationRunnerArguments.class=com.w2sv.wifiwidget.storescreenshots.InAppScreenshotTest

./gradlew :store-screenshots:generateInAppLargeTabletScreenshots \
  -Pandroid.testInstrumentationRunnerArguments.class=com.w2sv.wifiwidget.storescreenshots.InAppScreenshotTest

./gradlew :store-screenshots:generateInAppScreenshots \
  -Pandroid.testInstrumentationRunnerArguments.class=com.w2sv.wifiwidget.storescreenshots.InAppScreenshotTest
```

## All six screenshots

Run the full pipeline without the class-filter property. It runs both screenshot test classes and validates all six PNGs per device. Each generation mode checks its required filter before managed-device setup; full generation rejects any class filter.

```bash
./gradlew :store-screenshots:generateScreenshots
```

For one device, use `generatePhoneScreenshots`, `generateTabletScreenshots`, or `generateLargeTabletScreenshots` in the same module. The screenshot task definitions and ordered filenames are in the module-local [screenshot-tasks.gradle.kts](screenshot-tasks.gradle.kts), which `build.gradle.kts` applies after configuring the managed devices. Wallpaper attribution is in [WALLPAPERS.md](WALLPAPERS.md).
