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
    data object TaskDetailStarted : TaskDetailUserIntent
    data object TaskDetailBackPressed : TaskDetailUserIntent
    data object TaskDetailSwipedLeft : TaskDetailUserIntent
    data object TaskDetailSwipedRight : TaskDetailUserIntent
    data class TaskDetailTitleChanged(val value: String) : TaskDetailUserIntent
}

sealed interface GlobalRetryUserIntent : UserIntent {
    data object GlobalRetryRequested : GlobalRetryUserIntent
    data object GlobalRetryDismissed : GlobalRetryUserIntent
}

sealed interface LoginUserIntent : UserIntent {
    data class LoginUsernameChanged(val value: String) : LoginUserIntent
    data class LoginPasswordChanged(val value: String) : LoginUserIntent
    data object LoginSubmitted : LoginUserIntent
}
```

Rules of thumb:

- Name the sealed interface `<ScreenSubject>UserIntent`.
- Use past tense and keep the screen or subject prefix visible, for example `TaskDetailBackPressed`, `TaskDetailSwipedLeft`, and `GlobalRetryDismissed`.
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
- Do not model one-shot events such as toast, snackbar, or navigation as `UiState`; use `SideEffect` through a separate effect stream.

## SideEffect

`SideEffect` is the marker interface for one-shot effects that must not be persisted in `UiState`.

Use `SideEffect` for fire-and-forget outputs such as navigation, snackbars, analytics tracking, opening deep links, or triggering platform APIs.

Contract:

```kotlin
interface SideEffect
```

Feature-specific shape:

```kotlin
sealed interface TaskDetailSideEffect : SideEffect {
    data object NavigatedBack : TaskDetailSideEffect
    data object NavigatedToPreviousTask : TaskDetailSideEffect
    data object NavigatedToNextTask : TaskDetailSideEffect
    data class SnackbarShown(val message: String) : TaskDetailSideEffect
    data class AnalyticsTracked(val eventName: String) : TaskDetailSideEffect
}
```

Rules of thumb:

- Model only one-time outputs that should be handled once by the UI layer.
- Keep payloads UI-safe and serializable where possible.
- Do not store `SideEffect`s in `UiState`.
- Emit effects via a separate stream, for example `SharedFlow<SE>` in the ViewModel.

Example:

```kotlin
sealed interface TaskDetailSideEffect : SideEffect {
    data object NavigatedBack : TaskDetailSideEffect
    data object NavigatedToPreviousTask : TaskDetailSideEffect
    data object NavigatedToNextTask : TaskDetailSideEffect
    data class SnackbarShown(val message: String) : TaskDetailSideEffect
}

class TaskDetailViewModel(
    private val repository: TaskRepository,
) : MVIViewModel<TaskDetailUserIntent, TaskDetailUiState>(
        initialState = TaskDetailUiState.TaskDetailLoading,
    ) {
    private val _sideEffects = MutableSharedFlow<TaskDetailSideEffect>()
    val sideEffects: SharedFlow<TaskDetailSideEffect> = _sideEffects.asSharedFlow()

    override fun onUserIntent(intent: TaskDetailUserIntent) {
        when (intent) {
            TaskDetailUserIntent.TaskDetailStarted -> loadTask()
            TaskDetailUserIntent.TaskDetailBackPressed -> navigateBack()
            TaskDetailUserIntent.TaskDetailSwipedLeft -> navigateToPreviousTask()
            TaskDetailUserIntent.TaskDetailSwipedRight -> navigateToNextTask()
            is TaskDetailUserIntent.TaskDetailTitleChanged -> updateTitle(intent.value)
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            runCatching { repository.load() }
                .onSuccess { data ->
                    updateState {
                        TaskDetailUiState.TaskDetailContent(
                            title = data.title,
                            canSave = false,
                        )
                    }
                }
                .onFailure {
                    _sideEffects.emit(TaskDetailSideEffect.SnackbarShown("Data not loaded"))
                }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _sideEffects.emit(TaskDetailSideEffect.NavigatedBack)
        }
    }

    private fun navigateToPreviousTask() {
        viewModelScope.launch {
            _sideEffects.emit(TaskDetailSideEffect.NavigatedToPreviousTask)
        }
    }

    private fun navigateToNextTask() {
        viewModelScope.launch {
            _sideEffects.emit(TaskDetailSideEffect.NavigatedToNextTask)
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
    navController: NavController,
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                TaskDetailSideEffect.NavigatedBack -> navController.popBackStack()
                TaskDetailSideEffect.NavigatedToPreviousTask -> navController.navigate("previous-task")
                TaskDetailSideEffect.NavigatedToNextTask -> navController.navigate("next-task")
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
        -> MVIViewModel updateState { ... }
        -> StateFlow<STATE>
        -> Composable renders again
```

Contract:

```kotlin
abstract class MVIViewModel<UI : UserIntent, STATE : UiState>(
    initialState: STATE,
) : ViewModel() {
    abstract fun onUserIntent(intent: UI)

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    protected fun updateState(update: (STATE) -> STATE) {
        _state.update(update)
    }
}
```

Subclass guarantees:

- Expose a read-only `state: StateFlow<STATE>` that UI can collect with `collectAsStateWithLifecycle()`.
- Mutate state only through the protected `updateState` reducer.
- Provide a single public entry point, `onUserIntent`, typically implemented as an exhaustive `when` over the intent hierarchy.

How to subclass:

1. Pick concrete types for `UI` and `STATE`.
2. Pass an `initialState`.
3. Implement `onUserIntent` exhaustively.
4. Run side effects such as network or repository work in `viewModelScope`.
5. Apply result state through `updateState`.
6. Emit one-shot effects through a separate stream, not through `state`.

Reference example:

```kotlin
class GlobalRetryViewModel(
    private val globalApiRetryManager: GlobalApiRetryManager,
) : MVIViewModel<GlobalRetryUserIntent, GlobalRetryUiState>(
        initialState = GlobalRetryHidden,
    ) {
    init {
        viewModelScope.launch {
            globalApiRetryManager.uiState.collect { newState ->
                updateState { newState }
            }
        }
    }

    override fun onUserIntent(intent: GlobalRetryUserIntent) {
        when (intent) {
            GlobalRetryRequested -> globalApiRetryManager.executeRetry()
            GlobalRetryDismissed -> globalApiRetryManager.dismiss()
        }
    }
}
```

What the example shows:

- State can be driven by a domain source; the ViewModel bridges domain state to UI state through `updateState`.
- Intents map to domain calls. The ViewModel translates user actions; it should avoid owning business logic itself.
- No public mutation API exists other than `onUserIntent`.

## Cross-Contract Rules

- `UserIntent` represents input that already happened.
- `UiState` represents persistent renderable output.
- `SideEffect` represents one-shot output.
- `MVIViewModel` connects those contracts through `onUserIntent`, `state`, and `updateState`.
- Keep Android and Compose framework types at the UI boundary unless a platform type is unavoidable for a specific platform integration.
- Prefer exhaustive sealed-interface handling over default `else` branches.
