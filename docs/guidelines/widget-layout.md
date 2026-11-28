# Widget Layout Guidelines

These guidelines apply to the Android home-screen widget implemented in `core/widget`.

## Architecture

- Keep responsive layout decisions centralized in the widget layout calculator.
- Use cohesive layout models to pass calculated values to rendering composables.
- Keep Glance composables focused on rendering rather than calculating layout geometry.
- Prefer simple, declarative Glance layouts over elaborate manual positioning calculations.
- Avoid unnecessary abstractions, special cases, and duplicated dimension calculations.

## Responsive Layout

### Realistic Widget Dimensions

Android launchers size widgets according to their home-screen grids, which vary across devices, orientations, and launchers.

Google provides the following example dimensions for a **2 × 1 widget** on a Pixel 4 with a 5 × 4 home-screen grid:

| Orientation | Available dimensions |
|---|---|
| Portrait | 130 × 102dp |
| Landscape | 269 × 51dp |

Google's newer widget design guidance specifies a handheld 2 × 1 size range of **109–306dp in width** and **56–130dp in height**, encompassing different devices and orientations.

These examples demonstrate that narrow widgets generally occur in portrait orientation, while short widgets generally occur in landscape orientation.

Consequently, although WiFi Widget declares `minResizeWidth="100dp"` and `minResizeHeight="40dp"`, the combination **100 × 40dp is unlikely to occur on conventional phone launchers**. These values define independent resizing constraints, not a typical widget size.

Such extreme combinations remain possible with unusual launcher configurations or custom widget hosts.

### Layout Priorities

- Prioritize correct presentation at common widget sizes and aspect ratios.
- Support narrow and short widgets independently, as both can occur in production.
- Treat combinations of extreme minimum width and height as best-effort layouts.
- Do not introduce significant complexity to accommodate dimensions where the configured content cannot physically fit.
- Use a small number of meaningful breakpoints rather than optimizing for individual widget dimensions.
- Prefer natural text wrapping and layout behavior over estimated text measurements.
- Prevent unnecessary clipping and truncation when sufficient space is available.

## Testing

- Prefer manual device verification and a small number of representative previews for visual layout validation.
- Do not exhaustively test widget dimensions, breakpoints, or internal layout calculations.
- Add automated layout tests only when they provide clear value beyond visual inspection.
- Preserve automated tests for meaningful nonvisual widget functionality.

## General Principles

- Maintain clear separation of concerns without overengineering.
- Optimize for readability, maintainability, and realistic usage rather than theoretical layout perfection.
- Avoid introducing additional RemoteViews implementations for minor visual improvements.
- Prefer straightforward solutions to actual observed problems over speculative edge-case handling.
