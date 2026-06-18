---
name: android-mvi
description: Use this skill when creating, migrating, or reviewing Android MVI screens in Kotlin or Jetpack Compose. Apply it to name UserIntent, UiState, SideEffect, and MVIViewModel contracts; separate persistent render state from one-shot effects; enforce exhaustive sealed-interface handling; or document shared MVI primitives.
---

# Android MVI

## Workflow

Use this skill when implementing, migrating, or reviewing an Android screen that follows project MVI conventions.

1. Inspect the existing screen, ViewModel, UI contracts, and tests before editing.
2. Read [MVI conventions](references/mvi-conventions.md) before making naming, state-shape, or migration decisions.
3. Read [base contracts](references/base-contracts.md) when creating, editing, documenting, or reviewing the shared MVI base contracts: `UserIntent`, `UiState`, `SideEffect`, or `MVIViewModel`.
4. Identify the screen subject name, for example `TaskDetail`, then align contracts to:
   - `<ScreenSubject>UserIntent`
   - `<ScreenSubject>UiState`
   - `<ScreenSubject>SideEffect`
5. Keep persistent render data in `UiState`; keep navigation, snackbar/toast, analytics, deep links, and external opens in `SideEffect`.
6. Prefer `sealed interface` hierarchies with `data object` for payload-free variants and `data class` for payload variants.
7. Update ViewModel intent handling and UI rendering with exhaustive `when` branches.
8. Add or update focused tests when behavior changes, especially for intent handling, state transitions, and side-effect emission.

## Migration Checks

For each screen, verify:

- `sealed interface <ScreenSubject>UserIntent : UserIntent` exists.
- Intent names are past tense and include a visible screen or subject prefix.
- Intent payloads avoid Android and Compose types; map those at the UI boundary.
- `sealed interface <ScreenSubject>UiState : UiState` exists.
- Each state variant carries only fields it actually needs.
- One-shot events are not stored in `UiState`.
- `sealed interface <ScreenSubject>SideEffect : SideEffect` exists when the screen has one-shot effects.
- ViewModel and UI `when` branches are exhaustive without catch-all branches that hide missing cases.

## References

- Read [MVI conventions](references/mvi-conventions.md) for the project vocabulary, examples, do/don't rules, and the per-screen checklist.
- Read [base contracts](references/base-contracts.md) for KDoc-ready guidance and examples for `UserIntent`, `UiState`, `SideEffect`, and `MVIViewModel`.
