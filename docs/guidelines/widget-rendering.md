# Widget Rendering Guidelines

These guidelines apply to the Jetpack Glance home-screen widget in `core/widget`. For responsive sizing and visual layout decisions, follow [`widget-layout.md`](widget-layout.md). For the network refresh and recovery lifecycle, see [`property-refresh-and-synchronization.md`](../property-refresh-and-synchronization.md).

## Presentation and Rendering

- Keep the Glance UI declarative. Composables render an immutable `WidgetPresentationState`; they must not read repositories, fetch network data, or persist configuration.
- Build presentation snapshots in `WidgetPresentationStateProvider`. Read configuration, Wi-Fi status, route availability, and eligible cached remote data coherently for each pass; resolve properties through the shared domain presentation provider.
- Keep the `WifiWidgetProvider` responsible for Android widget callbacks and lifecycle reconciliation, `WidgetRenderCoordinator` for render requests, and `WifiGlanceWidget` for Glance composition. Do not move orchestration into UI composables.
- Rendering must not perform HTTP or schedule remote-data recovery. Keep rendering, refreshing, and recovery distinct: existing workers and observers own refresh/recovery decisions.
- The displayed update time represents preparation of the widget presentation, **not** the most recent successful HTTP refresh.

## Glance Updates and Lifecycle

- Preserve the revision-based update path: active Glance compositions observe render revisions, while `updateAll()` also updates inactive widget sessions. Do not assume `updateAll()` alone restarts an active `provideGlance()` session.
- Preserve the initial-snapshot/subscription handoff so render requests occurring during initial preparation are not lost.
- Keep lifecycle and process-recreation behavior in the provider, observer, and worker layers. Do not depend on an app-process restart causing an `APPWIDGET_UPDATE` callback.

## Content and Interactions

- Prefer native Glance elements and modifiers. Use `AndroidRemoteViews` only for functionality Glance cannot adequately express, currently the text-subscript spans in property labels; do not restore a RemoteViews-backed property collection.
- Give `LazyColumn` property rows stable IDs derived from their source `WifiProperty` and occurrence index. Do not key rows by list position, displayed text, or current value.
- Derive each displayed value and its clipboard action payload from the same presentation snapshot. Do not enable copying for unresolved or blank values.
- Keep user interactions originating inside the widget under `ui/actions`; keep app-driven widget management separate under `management`.
- Preserve configurable light/dark and dynamic/custom colors, background opacity, and readable foreground contrast. Account for floating action surfaces as well as the main background.
- Follow `widget-layout.md` for responsive breakpoints, spacing tokens, scroll clearance, and realistic minimum-size priorities.

## Testing and Previews

- Prefer representative Glance previews and manual launcher checks for visual changes. Avoid exhaustive geometry tests or pixel-sensitive assertions without a concrete regression to prevent.
- Preserve behavioral tests for rendering updates, configuration changes, action payloads, and refresh/recovery separation. Use device integration tests for behavior that depends on the real launcher host.
- Keep preview-only infrastructure separate from production code and store screenshot tests. Export Paparazzi previews with `./gradlew :core:widget:exportGlancePreviews` when needed.
