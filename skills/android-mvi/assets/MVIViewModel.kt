import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base class for every MVI-style `ViewModel` in the app.
 *
 * It enforces the project-wide unidirectional data flow:
 *
 * ```
 * Composable renders STATE and emits UI intents
 *         -> onUserIntent(UI)
 *         -> MVIViewModel updateState { ... }, emitSideEffect(...), or launchSideEffect(...)
 *         -> StateFlow<STATE> or SharedFlow<SE>
 *         -> Composable renders state or handles effect once
 * ```
 *
 * Subclasses get these guarantees:
 *
 * 1. A read-only [state] `StateFlow` for persistent render state.
 * 2. A read-only [sideEffects] `SharedFlow` for one-shot UI effects.
 * 3. A protected [updateState] reducer, which is the only way to mutate state.
 * 4. Protected [emitSideEffect] and [launchSideEffect] helpers, which are
 *    the only ways to emit one-shot effects.
 * 5. A single public entry point, [onUserIntent], which should usually be an
 *    exhaustive `when` over a sealed [UserIntent] hierarchy.
 *
 * ## How to subclass
 *
 * Pick concrete feature contracts and pass an initial state:
 *
 * ```kotlin
 * class TaskDetailViewModel(
 *     private val loadTaskAction: LoadTaskAction,
 * ) : MVIViewModel<TaskDetailUserIntent, TaskDetailUiState, TaskDetailSideEffect>(
 *         initialState = TaskDetailUiState.TaskDetailLoading,
 *     ) {
 *     override fun onUserIntent(intent: TaskDetailUserIntent) {
 *         when (intent) {
 *             TaskDetailUserIntent.TaskStarted -> loadTask()
 *             TaskDetailUserIntent.TaskCompleted -> completeTask()
 *             TaskDetailUserIntent.TaskStopped -> stopTask()
 *             is TaskDetailUserIntent.TitleChanged -> updateTitle(intent.value)
 *         }
 *     }
 *
 *     private fun updateTitle(value: String) {
 *         updateState { current ->
 *             when (current) {
 *                 is TaskDetailUiState.TaskDetailContent -> current.copy(
 *                     title = value,
 *                     canSave = value.isNotBlank(),
 *                 )
 *                 else -> current
 *             }
 *         }
 *     }
 *
 *     private fun completeTask() {
 *         launchSideEffect(TaskDetailSideEffect.CompletionCelebrated)
 *     }
 * }
 * ```
 *
 * Screens without one-shot effects should use [SideEffect.None] explicitly:
 *
 * ```kotlin
 * class StaticInfoViewModel :
 *     MVIViewModel<StaticInfoUserIntent, StaticInfoUiState, SideEffect.None>(
 *         initialState = StaticInfoUiState.Content,
 *     ) {
 *     override fun onUserIntent(intent: StaticInfoUserIntent) {
 *         when (intent) {
 *             StaticInfoUserIntent.Started -> Unit
 *         }
 *     }
 * }
 * ```
 *
 * ## UI collection
 *
 * Collect [state] with lifecycle-aware state collection, for example
 * `collectAsStateWithLifecycle()` in Compose. Collect [sideEffects] in a
 * lifecycle-aware coroutine and handle each emitted effect once.
 *
 * @param USER_INTENT the sealed [UserIntent] type accepted by [onUserIntent].
 * @param STATE the [UiState] type emitted by [state].
 * @param SIDE_EFFECT the [SideEffect] type emitted by [sideEffects].
 * @param initialState the state value emitted before any intent is processed.
 *
 * @see UserIntent
 * @see UiState
 * @see SideEffect
 */
abstract class MVIViewModel<
    USER_INTENT : UserIntent,
    STATE : UiState,
    SIDE_EFFECT : SideEffect,
>(
    initialState: STATE,
) : ViewModel() {
    /**
     * Handles a single [UserIntent] coming from the UI or environment.
     *
     * Implementations should usually be a single exhaustive `when` over the
     * feature's sealed intent hierarchy. Long-running work must be launched in
     * `viewModelScope`; resulting render data is applied with [updateState].
     * One-shot outputs are emitted with [emitSideEffect] from an existing
     * coroutine or [launchSideEffect] for simple fire-and-forget effects.
     */
    abstract fun onUserIntent(intent: USER_INTENT)

    private val _state = MutableStateFlow(initialState)

    /**
     * Read-only stream of the current [UiState].
     *
     * The UI collects this value and renders it. Nothing outside the ViewModel
     * can mutate the backing state flow.
     */
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SIDE_EFFECT>()

    /**
     * Read-only stream of one-shot [SideEffect]s.
     *
     * The UI should collect this flow in a lifecycle-aware coroutine and handle
     * each effect once. Effects must not be stored in [UiState].
     */
    val sideEffects: SharedFlow<SIDE_EFFECT> = _sideEffects.asSharedFlow()

    /**
     * Atomically updates [state] by applying [update] to the current value.
     *
     * Reducers should be pure: calculate and return the next state without
     * triggering navigation, snackbars, action/use case calls, analytics, or other
     * side effects.
     */
    protected fun updateState(update: (STATE) -> STATE) {
        _state.update(update)
    }

    /**
     * Emits a one-shot [SideEffect].
     *
     * Call this from `viewModelScope` or another coroutine owned by the
     * ViewModel. Use it for navigation, snackbars, analytics, deep links, and
     * other outputs that should be handled once rather than persisted in
     * [state].
     */
    protected suspend fun emitSideEffect(sideEffect: SIDE_EFFECT) {
        _sideEffects.emit(sideEffect)
    }

    /**
     * Launches [viewModelScope] and emits a one-shot [SideEffect].
     *
     * Use this for simple intent handlers that only need to emit one effect.
     * Prefer [emitSideEffect] when already inside a coroutine that is doing
     * action/use case work before deciding which effect to emit.
     */
    protected fun launchSideEffect(sideEffect: SIDE_EFFECT) {
        viewModelScope.launch {
            emitSideEffect(sideEffect)
        }
    }
}
