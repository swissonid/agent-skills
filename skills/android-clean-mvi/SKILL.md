---
name: android-clean-mvi
description: Use when creating, migrating, or reviewing a complete Android Kotlin or Jetpack Compose screen that combines MVI presentation contracts with clean architecture boundaries, including UserIntent, UiState, SideEffect, MVIViewModel, Action or UseCase dependencies, Result-based errors, and no ViewModel access to repositories or data sources.
---

# Android Clean MVI

Use this orchestration skill when a screen or feature spans both presentation MVI and clean architecture boundaries. Keep this skill focused on how the layers connect; use more specific Android MVI or clean architecture guidance when the task only touches one side.

## Companion Skills

When available, also use:

- `android-mvi` for detailed `UserIntent`, `UiState`, `SideEffect`, and `MVIViewModel` conventions.
- `android-clean-architecture` for detailed `Action`, `UseCase`, repository, `Result`, and TDD conventions.

## Workflow

1. Start with tests for behavior that changes:
   - Action/use case success and failure paths.
   - ViewModel intent handling, state transitions, and side-effect emission.
2. Identify the screen subject, for example `TaskDetail`, and create or align:
   - `TaskDetailUserIntent`
   - `TaskDetailUiState`
   - `TaskDetailSideEffect`, when the screen has one-shot outputs
   - `TaskDetailViewModel : MVIViewModel<Intent, State, SideEffect>`
3. Put feature behavior behind an `Action` or `UseCase`:
   - Use `Action` for app or technical operations such as loading, refreshing, syncing, tracking, or platform work.
   - Use `UseCase` for business workflows or product capabilities that coordinate meaningful steps.
   - Do not create both an action and a use case for the same responsibility.
4. Keep the ViewModel dependent on actions or use cases only. It must not call repositories, APIs, databases, or data sources directly.
5. Return expected failures from actions/use cases as `Result<Success, ErrorThrowable>` or the project equivalent. Do not throw expected failures above repositories.
6. Map action/use case results in the ViewModel:
   - Success becomes persistent render data in `UiState`.
   - Recoverable render failures become error or empty `UiState` variants.
   - Navigation, snackbar/toast, analytics, deep links, and external opens become `SideEffect`.
7. Use `updateState { ... }` for state changes.
8. Use `launchSideEffect(...)` for simple fire-and-forget side effects, and `emitSideEffect(...)` when already inside a coroutine doing action/use case work.
9. Keep ViewModel and UI `when` branches exhaustive over sealed interfaces.

## Boundary Shape

Recommended dependency direction:

```text
Composable
    -> MVIViewModel<UserIntent, UiState, SideEffect>
        -> Action or UseCase
            -> Repository, only when it adds a useful data boundary
                -> Data source / API / database
```

Repositories are optional. Add one only for data access, caching, remote/local coordination, shared data mapping, multiple data sources, or a stable abstraction over infrastructure.

## Review Checklist

- MVI contracts use the screen subject consistently.
- `UiState` contains persistent render data only.
- One-shot events are emitted as `SideEffect`, not stored in `UiState`.
- The ViewModel exposes one public intent entry point through `onUserIntent`.
- The ViewModel depends on an action or use case, not a repository or data source.
- Actions/use cases are `suspend operator fun invoke(...)`.
- Expected failures are modeled as `Result` failures and typed errors.
- No `Result.get()` is used above repositories.
- Repositories exist only when they add a meaningful data boundary.
- Tests cover action/use case behavior and ViewModel state/effect behavior.

