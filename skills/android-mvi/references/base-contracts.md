# MVI Base Contracts

Use this reference when creating, editing, documenting, or reviewing the shared MVI primitives:

- `UserIntent`
- `UiState`
- `SideEffect`
- `MVIViewModel`

## Table Of Contents

- [UserIntent](#userintent)
- [UiState](#uistate)
- [SideEffect](#sideeffect)
- [MVIViewModel](#mviviewmodel)
- [Cross-Contract Rules](#cross-contract-rules)

## UserIntent

`UserIntent` is the marker interface for anything the user or environment wants the ViewModel to do.

Use it as the only way the UI talks back to its `MVIViewModel`. Every click, text change, swipe, and system event the screen reacts to should be modeled as a concrete `UserIntent` subtype and delivered through `MVIViewModel.onUserIntent`.

Contract:

```kotlin
interface UserIntent
```

Feature-specific shape:

```kotlin
sealed interface TaskDetailUserIntent : UserIntent {
    data object TaskStarted : TaskDetailUserIntent
    data object TaskCompleted : TaskDetailUserIntent
    data object TaskStopped : TaskDetailUserIntent
    data class TitleChanged(val value: String) : TaskDetailUserIntent
}

sealed interface LoginUserIntent : UserIntent {
    data class UsernameChanged(val value: String) : LoginUserIntent
    data class PasswordChanged(val value: String) : LoginUserIntent
    data object Submitted : LoginUserIntent
}
```

Rules of thumb:

- Name the sealed interface `<ScreenSubject>UserIntent`.
- Use past tense inside the sealed interface, for example `TaskStarted`, `TaskCompleted`, and `TaskStopped`; do not repeat the screen subject in every variant.
- Name intents in user language, not reducer language; avoid names like `SetStateToHidden`.
- Use `data object` for payload-free intents and `data class` for intents with payloads.
- Keep intents small and self-describing.
- Do not leak Compose or Android types such as `TextFieldValue`, `Context`, or `View` into intents; convert them at the call site.
- Route every UI event through `MVIViewModel.onUserIntent`; avoid adding extra public ViewModel action methods.

## UiState

`UiState` is the marker interface for everything the UI needs to render a screen or feature.

A `UiState` is the single immutable snapshot that an `MVIViewModel` exposes to its Composable. The UI reads the current `UiState` and renders it. Decisions about what should be visible should live in state, not in scattered Composable logic.

Contract:

```kotlin
interface UiState
```

Preferred feature-specific shape:

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

Rules of thumb:

- Prefer immutable types: `data class`, `data object`, and `sealed interface`.
- Keep each state variant minimal; avoid optional or unrelated properties.
- Keep the shape UI-oriented, not domain-oriented. Map domain models in the ViewModel before exposing them.
- Do not put callbacks, `Flow`s, `LiveData`, or Android framework objects into `UiState`.
- Do not model one-shot events such as toast, snackbar, or navigation as `UiState`; use `SideEffect` through the base `MVIViewModel.sideEffects` stream.

## SideEffect

`SideEffect` is the marker interface for one-shot effects that must not be persisted in `UiState`.

Use `SideEffect` for fire-and-forget outputs such as navigation, snackbars, analytics tracking, opening deep links, or triggering platform APIs.

Contract:

```kotlin
interface SideEffect {
    data object None : SideEffect
}
```

Feature-specific shape:

```kotlin
sealed interface TaskDetailSideEffect : SideEffect {
    data object CompletionCelebrated : TaskDetailSideEffect
    data object StopConfirmed : TaskDetailSideEffect
    data class SnackbarShown(val message: String) : TaskDetailSideEffect
    data class AnalyticsTracked(val eventName: String) : TaskDetailSideEffect
}
```

Rules of thumb:

- Model only one-time outputs that should be handled once by the UI layer.
- Keep payloads UI-safe and serializable where possible.
- Do not store `SideEffect`s in `UiState`.
- Emit effects through the base `MVIViewModel.emitSideEffect(...)` helper.
- Collect effects from the base `MVIViewModel.sideEffects` stream.

Example:

```kotlin
sealed interface TaskDetailSideEffect : SideEffect {
    data object CompletionCelebrated : TaskDetailSideEffect
    data object StopConfirmed : TaskDetailSideEffect
    data class SnackbarShown(val message: String) : TaskDetailSideEffect
}

class TaskDetailViewModel(
    private val loadTaskAction: LoadTaskAction,
) : MVIViewModel<TaskDetailUserIntent, TaskDetailUiState, TaskDetailSideEffect>(
        initialState = TaskDetailUiState.TaskDetailLoading,
    ) {
    override fun onUserIntent(intent: TaskDetailUserIntent) {
        when (intent) {
            TaskStarted -> loadTask()
            TaskCompleted -> completeTask()
            TaskStopped -> stopTask()
            is TitleChanged -> updateTitle(intent.value)
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            loadTaskAction().fold(
                success = { task ->
                    updateState {
                        TaskDetailUiState.TaskDetailContent(
                            title = task.title,
                            canSave = false,
                        )
                    }
                },
                failure = { error ->
                    updateState {
                        TaskDetailUiState.TaskDetailError(
                            message = error.message ?: "Task not loaded",
                            retryEnabled = true,
                        )
                    }
                },
            )
        }
    }

    private fun completeTask() {
        viewModelScope.launch {
            emitSideEffect(TaskDetailSideEffect.CompletionCelebrated)
        }
    }

    private fun stopTask() {
        viewModelScope.launch {
            emitSideEffect(TaskDetailSideEffect.StopConfirmed)
        }
    }

    private fun updateTitle(value: String) {
        updateState { current ->
            when (current) {
                is TaskDetailUiState.TaskDetailContent -> current.copy(
                    title = value,
                    canSave = value.isNotBlank(),
                )
                else -> current
            }
        }
    }
}

@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                TaskDetailSideEffect.CompletionCelebrated ->
                    snackbarHostState.showSnackbar("Task completed")
                TaskDetailSideEffect.StopConfirmed ->
                    snackbarHostState.showSnackbar("Task stopped")
                is TaskDetailSideEffect.SnackbarShown ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
}
```

## MVIViewModel

`MVIViewModel` is the base class for every MVI-style ViewModel in the app.

It enforces unidirectional data flow:

```text
Composable renders STATE and emits UI intents
        -> onUserIntent(UI)
        -> MVIViewModel updateState { ... } or emitSideEffect(...)
        -> StateFlow<STATE> or SharedFlow<SE>
        -> Composable renders state or handles effect once
```

Contract:

```kotlin
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class MVIViewModel<UI : UserIntent, STATE : UiState, SE : SideEffect>(
    initialState: STATE,
) : ViewModel() {
    abstract fun onUserIntent(intent: UI)

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SE>()
    val sideEffects: SharedFlow<SE> = _sideEffects.asSharedFlow()

    protected fun updateState(update: (STATE) -> STATE) {
        _state.update(update)
    }

    protected suspend fun emitSideEffect(sideEffect: SE) {
        _sideEffects.emit(sideEffect)
    }
}
```

Screens without one-shot effects should use `SideEffect.None` explicitly:

```kotlin
class StaticInfoViewModel :
    MVIViewModel<StaticInfoUserIntent, StaticInfoUiState, SideEffect.None>(
        initialState = StaticInfoUiState.Content,
    ) {
    override fun onUserIntent(intent: StaticInfoUserIntent) {
        when (intent) {
            StaticInfoUserIntent.Started -> Unit
        }
    }
}
```

Subclass guarantees:

- Expose a read-only `state: StateFlow<STATE>` that UI can collect with `collectAsStateWithLifecycle()`.
- Expose a read-only `sideEffects: SharedFlow<SE>` that UI collects from a lifecycle-aware effect collector.
- Mutate state only through the protected `updateState` reducer.
- Emit one-shot effects only through the protected `emitSideEffect` helper.
- Provide a single public entry point, `onUserIntent`, typically implemented as an exhaustive `when` over the intent hierarchy.

How to subclass:

1. Pick concrete types for `UI`, `STATE`, and `SE`; use `SideEffect.None` when there are no one-shot effects.
2. Pass an `initialState`.
3. Implement `onUserIntent` exhaustively.
4. Run action/use case work in `viewModelScope`.
5. Apply result state through `updateState`.
6. Emit one-shot UI effects through `emitSideEffect`, not through `state`.

Reference example:

```kotlin
class TaskDetailViewModel(
    private val loadTaskAction: LoadTaskAction,
) : MVIViewModel<TaskDetailUserIntent, TaskDetailUiState, TaskDetailSideEffect>(
        initialState = TaskDetailLoading,
    ) {
    override fun onUserIntent(intent: TaskDetailUserIntent) {
        when (intent) {
            TaskStarted -> loadTask()
            TaskCompleted -> completeTask()
            TaskStopped -> stopTask()
            is TitleChanged -> updateTitle(intent.value)
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            loadTaskAction().fold(
                success = { task ->
                    updateState {
                        TaskDetailContent(
                            title = task.title,
                            canSave = false,
                        )
                    }
                },
                failure = { error ->
                    updateState {
                        TaskDetailError(
                            message = error.message ?: "Task not loaded",
                            retryEnabled = true,
                        )
                    }
                },
            )
        }
    }

    private fun completeTask() {
        viewModelScope.launch {
            emitSideEffect(CompletionCelebrated)
        }
    }

    private fun stopTask() {
        viewModelScope.launch {
            emitSideEffect(StopConfirmed)
        }
    }
}
```

What the example shows:

- State changes go through `updateState`.
- Intents map to state reductions, action/use case calls, or `emitSideEffect`.
- One-shot outputs stay in `SideEffect`, not `UiState`.
- No public mutation API exists other than `onUserIntent`.

## Cross-Contract Rules

- `UserIntent` represents input that already happened.
- `UiState` represents persistent renderable output.
- `SideEffect` represents one-shot output.
- `MVIViewModel` connects those contracts through `onUserIntent`, `state`, `sideEffects`, `updateState`, and `emitSideEffect`.
- Keep Android and Compose framework types at the UI boundary unless a platform type is unavoidable for a specific platform integration.
- Prefer exhaustive sealed-interface handling over default `else` branches.
