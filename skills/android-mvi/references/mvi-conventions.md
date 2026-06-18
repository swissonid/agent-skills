# MVI Conventions

This guideline defines the project-wide MVI vocabulary before gradually migrating screens.

## Table Of Contents

- [Goals](#goals)
- [1) UserIntent](#1-userintent)
- [2) UiState](#2-uistate)
- [3) SideEffect](#3-sideeffect)
- [4) Interaction In The ViewModel](#4-interaction-in-the-viewmodel)
- [5) Migration Checklist Per Screen](#5-migration-checklist-per-screen)

## Goals

- Consistent naming and structure across all screens.
- Exhaustive `when` branches over `sealed interface` hierarchies.
- Clear separation between persistent `UiState` and one-shot `SideEffect`s.

## 1) UserIntent

`UserIntent` models what the user or system has already triggered.

Conventions:

- Feature type: `<ScreenSubject>UserIntent`
- Intent events: past tense, with a visible screen/subject prefix
- Example: `TaskDetailStarted`, `TaskDetailBackPressed`, `TaskDetailSwipedLeft`, `GlobalRetryDismissed`

```kotlin
sealed interface TaskDetailUserIntent : UserIntent {
    data object TaskDetailStarted : TaskDetailUserIntent
    data object TaskDetailBackPressed : TaskDetailUserIntent
    data object TaskDetailSwipedLeft : TaskDetailUserIntent
    data object TaskDetailSwipedRight : TaskDetailUserIntent
    data class TaskDetailTitleChanged(
        val value: String,
    ) : TaskDetailUserIntent
}
```

Do:

- Use user-centered events (`...Started`, `...Dismissed`, `...Submitted`).
- Use `data object` without payload, `data class` with payload.

Don't:

- Use imperative or reducer names like `SetLoadingTrue`.
- Put Android/Compose types in intent payloads; map them at the UI boundary.

## 2) UiState

`UiState` is the persistent, renderable snapshot.

Recommended shape:

- `sealed interface <ScreenSubject>UiState`
- Each state only carries the fields it actually needs.

```kotlin
sealed interface TaskDetailUiState : UiState {
    data object TaskDetailLoading : TaskDetailUiState

    data class TaskDetailContent(
        val title: String,
        val canSave: Boolean,
    ) : TaskDetailUiState

    data class TaskDetailError(
        val message: String,
        val retryEnabled: Boolean,
    ) : TaskDetailUiState
}
```

Do:

- Use explicit, typed states instead of one large all-purpose state with many optional fields.
- Use immutable types (`data object`, `data class`).

Don't:

- Store one-shot events such as navigation or snackbar messages in `UiState`.

## 3) SideEffect

`SideEffect` models one-shot effects that should not remain in state.

Use cases:

- Navigation
- Snackbar/Toast
- Analytics tracking
- Deep link or open external targets

```kotlin
sealed interface TaskDetailSideEffect : SideEffect {
    data object NavigatedBack : TaskDetailSideEffect
    data object NavigatedToPreviousTask : TaskDetailSideEffect
    data object NavigatedToNextTask : TaskDetailSideEffect
    data class SnackbarShown(val message: String) : TaskDetailSideEffect
    data class AnalyticsTracked(val eventName: String) : TaskDetailSideEffect
}
```

Emission recommendation:

- Use a separate stream in the ViewModel, for example `SharedFlow<TaskDetailSideEffect>`.

## 4) Interaction In The ViewModel

```kotlin
class TaskDetailViewModel :
    MVIViewModel<TaskDetailUserIntent, TaskDetailUiState>(
        initialState = TaskDetailUiState.TaskDetailLoading,
    ) {
    override fun onUserIntent(intent: TaskDetailUserIntent) {
        when (intent) {
            TaskDetailUserIntent.TaskDetailStarted -> {
                // load data
            }
            TaskDetailUserIntent.TaskDetailBackPressed -> {
                // reduce state or emit side effect
            }
            TaskDetailUserIntent.TaskDetailSwipedLeft -> {
                // emit previous-task side effect
            }
            TaskDetailUserIntent.TaskDetailSwipedRight -> {
                // emit next-task side effect
            }
            is TaskDetailUserIntent.TaskDetailTitleChanged -> {
                // reduce state
            }
        }
    }
}
```

## 5) Migration Checklist Per Screen

- `sealed interface <ScreenSubject>UserIntent` exists.
- Intent names are in past tense.
- `sealed interface <ScreenSubject>UiState` exists.
- Each state contains only required properties.
- `sealed interface <ScreenSubject>SideEffect` exists for one-shot effects.
- Exhaustive `when` handling exists in `onUserIntent` and in the UI.
