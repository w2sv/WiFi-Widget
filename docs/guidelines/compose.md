# Compose Guidelines

These guidelines apply to the application's Compose UI. Follow [Kotlin guidelines](kotlin.md) for general modeling, naming, documentation, and testing conventions. Follow the separate widget guidelines for Glance rendering and layout.

## Screen architecture

- Keep routes responsible for ViewModel integration, navigation, and platform interactions. Rendering composables should receive state and callbacks rather than depend directly on ViewModels or Android-specific services.
- Prefer immutable screen state and explicit callbacks. Group related inputs or interactions into cohesive models when doing so simplifies a component's API; don't introduce state or actions bundles by default.
- Derive component presentation from existing screen state. Extract a dedicated presentation model when several related display decisions would otherwise clutter rendering code. Avoid redundant observable state.

## Component organization

- Keep parent layouts focused on the arrangement and ordering of meaningful child components. Children should own their internal styling and behavior.
- Avoid unnecessary layout nesting. Remove containers that add no distinct layout responsibility, and prefer a single layout with appropriate alignment, spacing, and modifiers over wrappers used only to group children. Use a scrollable Column for a small static content block rather than a LazyColumn containing one item. Preserve meaningful component boundaries, measurement, spacing, insets, scrolling, and accessibility when flattening layouts.
- Extract substantial, cohesive visual components into separate files when their implementations obscure the parent layout. Keep supporting composables private unless reused elsewhere.
- Preserve component boundaries when refactoring. Avoid flattening cohesive components or introducing generic UI abstractions solely to reduce code duplication.

## State and lifecycle

- Keep transient UI state local unless it must be shared, persisted, or coordinated with application logic.
- Avoid duplicating state already owned by a ViewModel, repository, or platform capability. Derive presentation from the existing authoritative source instead.

## UI refactoring

- Preserve existing appearance, interactions, accessibility, animations, and navigation behavior during structural refactors unless changes are explicitly requested.
- Preserve established component responsibilities and public APIs where practical. Refactor internal implementations without unnecessarily expanding the scope of changes.
