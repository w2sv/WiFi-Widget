# Commit Message Guidelines

- Inspect the full diff and recent commit messages before drafting. Cover all material changes without listing every file or implementation detail.
- Use `<type>(<scope>): <summary>`, omitting the scope when it adds little. Match established scopes such as `widget`, `ui`, `deps`, and `release`.
- Keep the subject concise, lowercase, and imperative, with no trailing period. Describe the resulting behavior or purpose.
- For substantial changes, follow the subject with a short paragraph explaining the benefits, especially what users will notice. Put this before technical details.
- Use focused bullets for the main implementation changes, relevant tests, and documentation. Keep useful technical descriptions, but explain why the change matters first.
- Scale detail to the change: a simple commit may need only a subject. Keep larger messages comprehensive without becoming a changelog of internal edits.
- Describe supported outcomes accurately; distinguish reduced work or earlier render requests from measured improvements in visible latency.

## Choosing the type

- `feat`: add a capability or user-facing behavior.
- `fix`: correct a bug.
- `perf`: improve runtime performance, such as reducing latency, repeated computation, or redundant requests.
- `refactor`: reorganize code while preserving behavior.
- `chore`: maintenance or tooling; use `chore(perf)` for performance measurement infrastructure that does not itself improve runtime performance.
- Use `test`, `docs`, or `build` when testing, documentation, or build configuration is the primary purpose. Supporting tests and documentation do not change the type of a feature or fix.

## Example

```text
perf(widget): speed up manual refreshes and share presentation snapshots

Show current local Wi-Fi information immediately when refreshing, without
waiting for remote requests to finish. Reduce repeated work across widgets.

- Render local data before scheduling or fetching remote updates.
- Share presentation snapshots by revision across widget instances.
- Add regression tests and refresh documentation.
```
