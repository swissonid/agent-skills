/**
 * Marker interface for the persistent, renderable snapshot of a screen.
 *
 * A [UiState] is the immutable value that an [MVIViewModel] exposes through its
 * `state` flow. The UI reads this value and renders it. Decisions about what is
 * visible, enabled, loading, empty, or errored should live in [UiState], not in
 * scattered Composable logic.
 *
 * Prefer a feature-specific sealed interface where each state carries only the
 * fields it needs:
 *
 * ```kotlin
 * sealed interface TaskDetailUiState : UiState {
 *     data object TaskDetailLoading : TaskDetailUiState
 *
 *     data class TaskDetailContent(
 *         val title: String,
 *         val canSave: Boolean,
 *     ) : TaskDetailUiState
 *
 *     data class TaskDetailError(
 *         val message: String,
 *         val retryEnabled: Boolean,
 *     ) : TaskDetailUiState
 * }
 * ```
 *
 * Rules of thumb:
 * - Prefer immutable types: `sealed interface`, `data object`, and `data class`.
 * - Keep state variants minimal; avoid unrelated nullable fields.
 * - Keep state UI-oriented. Map domain models before exposing them.
 * - Do not put callbacks, `Flow`, `LiveData`, `Context`, `View`, or Compose
 *   runtime objects in [UiState].
 * - Do not store one-shot events such as navigation, snackbars, toasts,
 *   analytics, or deep links in [UiState]. Use [SideEffect] instead.
 *
 * @see UserIntent
 * @see SideEffect
 * @see MVIViewModel
 */
interface UiState
