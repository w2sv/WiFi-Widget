# Kotlin Guidelines

## Modeling

- Prefer explicit, cohesive types and names that match the domain and persisted schema.
- Distinguish Android framework representations from application models when necessary. Use Platform prefixes only when they clarify that distinction, rather than systematically prefixing Android-specific types.
- Name the default internal implementation of a contract <Contract>Impl. Do not use System merely to mean the standard or production implementation.
- Use nullability for genuine absence instead of sentinel values.
- Prefer @JvmInline value class when a single value has a meaningful identity or prevents realistic type-mixing mistakes. Do not wrap values without adding meaning.
- Model parameterless, function-like providers as fun interface types with operator fun invoke() when they expose a single operation.
- Keep shallow sealed hierarchies nested when that improves discovery and call-site naming.
- Put derived properties and behavior on the data class, enum, sealed type, or other type that owns them. Use file-level extensions only when a strong visibility, dependency, or responsibility boundary makes ownership by the type inappropriate.
- Prefer a flat when to nested conditionals for mutually exclusive cases.
- When control flow establishes an invariant, enforce it directly with a cast, check, or checkNotNull. Do not add fallback UI, silent no-ops, or recovery behavior for states that should be impossible unless the product explicitly requires recovery.
- Represent genuinely unavailable or indeterminate network properties explicitly. Do not conflate missing information with disconnected, disabled, or unsupported states.
- Respect Android API-level differences and permission requirements when modeling platform capabilities. Do not assume that APIs expose identical information across supported Android versions.
- Remove obsolete parameters, extension points, and state when their use case disappears.

## Packages and files

- Place contracts and implementations in the narrow package and module that owns their responsibility; do not leave networking, persistence, widget, or similar infrastructure types in broad module-root packages.
- Respect existing module boundaries between app, core:common, core:domain, core:networking, core:datastore, and core:widget. Avoid introducing dependencies that reverse their intended responsibilities.
- Keep Android framework interactions and implementation details separate from domain models and contracts where practical.
- Give substantial contracts and implementations their own files named after the primary declaration. A small contract and its tightly coupled internal implementation may share a responsibility-named file.
- Use UpperCamelCase Kotlin filenames that describe the primary declaration or concept. Do not copy lower-camel casing from a top-level function or property into the filename.
- Keep implementation-specific helpers close to their consumers. Promote utilities to shared packages or modules only when they serve a genuine cross-feature responsibility.

## Documentation

- KDoc should explain responsibility, invariants, ownership boundaries, non-obvious side effects, and persistence or serialization contracts.
- Document Android-specific lifecycle assumptions, API restrictions, permission requirements, and background-execution limitations when they influence behavior.
- Document the circumstances under which cached network data remains valid, particularly when its correctness depends on the identity of the connected network.
- Do not restate property names or document self-explanatory accessors and one-line helpers.
- Add concise inline comments for non-obvious lifecycle behavior, invariants, or defensive logic. Describe the effect being preserved rather than narrating the implementation.

## Testing

- Name test functions with descriptive backtick-quoted phrases rather than camel-case identifiers.
- Prefer focused unit tests for domain logic, state transformations, and networking decisions.
- Use Robolectric for Android framework interactions when it provides meaningful coverage without requiring an instrumented device.
- Test relevant Android API-level differences, permission states, and connectivity transitions where behavior depends on them.
- For widget refreshing and background operations, cover cancellation, unavailable network information, repeated invocations, and relevant lifecycle transitions.
- Prefer deterministic coroutine and Flow tests over timing-based assertions. Avoid unnecessary delays and dependence on real network services.
- Test observable behavior and meaningful invariants rather than implementation details.

## Logging

- Use the existing slimber.log facade with lazy message lambdas. Do not introduce a separate logging abstraction or inject loggers unless a concrete need arises.
- Rely on the existing logging infrastructure for tagging rather than manually supplying tags to individual log calls.
- Logging must remain observational: it must not own state, alter control flow, or throw from a failure path.
- Use stable natural-language event descriptions followed by consistent key=value context.
- Log meaningful lifecycle, connectivity, external-operation, state-transition, widget-refresh, and unexpected-failure boundaries in the layer that owns them.
- Use d for detailed transitions and expected cancellation, i for meaningful successful operations, w for recoverable anomalies, and e for unexpected failures that prevent completion.
- Do not log routine recompositions, high-frequency network callbacks, tight loops, or expected validation failures at info or above.
