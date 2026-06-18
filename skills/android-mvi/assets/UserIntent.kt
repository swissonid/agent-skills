/**
 * Marker interface for anything the user or environment has already triggered.
 *
 * A [UserIntent] is the only way the UI talks back to its [MVIViewModel]. Every
 * click, text change, swipe, lifecycle start, retry, dismiss action, or system
 * event the screen reacts to should be modeled as a concrete [UserIntent]
 * subtype and delivered through [MVIViewModel.onUserIntent].
 *
 * Define a feature-specific sealed interface:
 *
 * ```kotlin
 * sealed interface TaskDetailUserIntent : UserIntent {
 *     data object TaskStarted : TaskDetailUserIntent
 *     data object TaskCompleted : TaskDetailUserIntent
 *     data object TaskStopped : TaskDetailUserIntent
 *     data class TitleChanged(val value: String) : TaskDetailUserIntent
 * }
 * ```
 *
 * Rules of thumb:
 * - Name the sealed interface `<ScreenSubject>UserIntent`.
 * - Use past-tense, user-centered event names inside the sealed interface.
 * - Do not repeat the screen subject in every variant; the sealed interface
 *   already scopes the variants.
 * - Use `data object` for payload-free intents and `data class` for payloads.
 * - Keep Android and Compose types out of intent payloads; map them at the UI
 *   boundary before calling [MVIViewModel.onUserIntent].
 * - Route every UI event through [MVIViewModel.onUserIntent]; avoid extra
 *   public ViewModel action methods.
 *
 * @see UiState
 * @see SideEffect
 * @see MVIViewModel
 */
interface UserIntent {
    /**
     * Default user-intent type for screens that do not react to any events.
     *
     * This keeps simple ViewModels concise:
     *
     * ```kotlin
     * class StaticInfoViewModel :
     *     MVIViewModel<UserIntent.None, StaticInfoUiState, SideEffect.None>(
     *         initialState = StaticInfoUiState.Content,
     *     )
     * ```
     */
    data object None : UserIntent
}
