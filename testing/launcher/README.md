# Launcher test support

`:testing:launcher` is a small Android library shared by the independent `:store-screenshots` and `:widget-integration-tests` test modules. It owns Launcher3 readiness and hierarchy diagnostics, widget pinning, discovery across workspace pages, removal, and resizing. Screenshot capture, wallpaper, demo mode, and screenshot-specific device setup stay in `:store-screenshots`.

Run the behavioral tests with `:widget-integration-tests:widgetPixel6Api35DebugAndroidTest`. The existing screenshot generation tasks in `:store-screenshots` keep their class filters and output pipeline.
