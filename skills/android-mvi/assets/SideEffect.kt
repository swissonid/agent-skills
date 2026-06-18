/**
 * Marker interface for one-shot outputs that must not be persisted in [UiState].
 *
 * Use [SideEffect] for fire-and-forget outputs such as navigation, snackbars,
 * toasts, analytics tracking, opening deep links, launching external targets,
 * or triggering platform APIs.
 *
 * Define a feature-specific sealed interface when a screen has effects:
 *
 * ```kotlin
 * sealed interface TaskDetailSideEffect : SideEffect {
 *     data object CompletionCelebrated : TaskDetailSideEffect
 *     data object StopConfirmed : TaskDetailSideEffect
 *     data class SnackbarShown(val message: String) : TaskDetailSideEffect
 * }
 * ```
 *
 * Then expose it through [MVIViewModel]'s base effect stream:
 *
 * ```kotlin
 * class TaskDetailViewModel :
 *     MVIViewModel<TaskDetailUserIntent, TaskDetailUiState, TaskDetailSideEffect>(
 *         initialState = TaskDetailUiState.TaskDetailLoading,
 *     ) {
 *     private fun completeTask() {
 *         viewModelScope.launch {
 *             emitSideEffect(TaskDetailSideEffect.CompletionCelebrated)
 *         }
 *     }
 * }
 * ```
 *
 * Screens without one-shot effects should use [None] as the third
 * [MVIViewModel] type argument.
 *
 * Rules of thumb:
 * - Model only outputs that should be handled once by the UI layer.
 * - Keep payloads UI-safe and serializable where possible.
 * - Do not store [SideEffect]s in [UiState].
 * - Emit effects with `emitSideEffect(...)`; collect them from `sideEffects`.
 *
 * @see UserIntent
 * @see UiState
 * @see MVIViewModel
 */
interface SideEffect {
    /**
     * Default side-effect type for screens that do not emit one-shot effects.
     *
     * This keeps simple ViewModels concise:
     *
     * ```kotlin
     * class StaticInfoViewModel :
     *     MVIViewModel<StaticInfoUserIntent, StaticInfoUiState, SideEffect.None>(
     *         initialState = StaticInfoUiState.Content,
     *     )
     * ```
     */
    data object None : SideEffect
}
