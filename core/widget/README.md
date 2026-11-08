# Widget module

`:core:widget` owns the Android home-screen widget, including its Jetpack Glance UI, interactions, presentation state, lifecycle handling, and background refresh integration.

See the [rendering guidelines](../../docs/guidelines/widget-rendering.md), [responsive layout guidelines](../../docs/guidelines/widget-layout.md), and [property refresh and synchronization guide](../../docs/property-refresh-and-synchronization.md) for implementation details.

## Glance previews

[WifiWidgetPreviews.kt](src/debug/kotlin/com/w2sv/widget/ui/preview/WifiWidgetPreviews.kt) defines the Glance `@Preview` composables. [WifiGlancePaparazziPreviewTest.kt](src/test/kotlin/com/w2sv/widget/ui/WifiGlancePaparazziPreviewTest.kt) renders representative previews through Paparazzi. These tests are excluded from ordinary JVM test runs and execute through the preview-export workflow.

From the repository root, run:

```bash
./gradlew :core:widget:exportGlancePreviews
```

The task writes PNGs to `core/widget/build/generated/glance-previews/`. It runs the selected Paparazzi preview tests, which produce images and snapshot metadata. `PreviewImageRenamer` uses the metadata to export each PNG under its preview composable's name, and the Gradle `Sync` task removes obsolete exported files.

These previews are for local visual inspection, separate from launcher-based Play Store screenshot generation and widget integration tests.
