# Android Clean Architecture Conventions

Use this reference when creating, migrating, or reviewing Android Kotlin clean architecture code.

## Table Of Contents

- [Core Rules](#core-rules)
- [Layer Shape](#layer-shape)
- [Action Or UseCase](#action-or-usecase)
- [Repository And DataSource Naming](#repository-and-datasource-naming)
- [Repository Is Optional](#repository-is-optional)
- [Result-Based Errors](#result-based-errors)
- [TDD Workflow](#tdd-workflow)
- [Examples](#examples)
- [Migration Checklist](#migration-checklist)

## Core Rules

- Always put feature behavior behind an action or use case.
- Prefer `Action` for app/technical operations and `UseCase` for business workflows.
- Do not create both an action and a use case for the same responsibility.
- Always implement `suspend operator fun invoke(...)` on use cases/actions.
- Treat use cases/actions and repositories as asynchronous by default.
- Add repositories only when they provide a useful data boundary.
- Code above repositories must not throw exceptions for expected failures.
- Model expected success/failure with `com.github.kittinunf.result.Result`.
- Name repositories and data sources by role, with a technology prefix for concrete implementations.
- Write tests first for new behavior and bug fixes.

## Layer Shape

Recommended dependency direction:

```text
UI / ViewModel
    -> Action
    -> UseCase -> Action(s), when the use case is a business workflow
        -> Repository, if useful
            -> Data source / API / database
```

The ViewModel should depend on actions or use cases. It should not call repositories, APIs, databases, or data sources directly.

## Action Or UseCase

Both names are valid, but they should mean different things.

Use `Action` for a concrete app or technical operation:

- Loading data for a screen.
- Refreshing a session.
- Syncing an offline queue.
- Requesting a platform permission.
- Tracking analytics.
- Opening an external target.

Use `UseCase` for a business workflow or product capability:

- Submitting an order.
- Purchasing a ticket.
- Checking in a passenger.
- Calculating compensation.
- Completing a domain workflow that coordinates multiple meaningful steps.

A `UseCase` may depend on zero or more `Action`s. An `Action` should not depend on a `UseCase`.

Before adding a new class:

1. Decide whether the operation is an app/technical action or a business workflow.
2. Search for existing `*Action` and `*UseCase` classes in the feature/module.
3. Follow the semantic distinction already used by the project.
4. If the distinction is missing, introduce it deliberately and keep it consistent.

Start with an `Action` for single app operations. Promote to a `UseCase` only when the operation represents a business workflow or coordinates multiple meaningful domain/application steps.

Action classes should be invokable:

```kotlin
class LoadTaskAction(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: TaskId): Result<Task, LoadTaskError> {
        return repository.loadTask(taskId)
    }
}
```

Use case example:

```kotlin
class SubmitOrderUseCase(
    private val validateOrderAction: ValidateOrderAction,
    private val submitPaymentAction: SubmitPaymentAction,
    private val persistOrderAction: PersistOrderAction,
) {
    suspend operator fun invoke(order: Order): Result<OrderConfirmation, SubmitOrderError> {
        return validateOrderAction(order)
            .flatMap { submitPaymentAction(order) }
            .flatMap { persistOrderAction(order) }
    }
}
```

Do not create both `LoadTaskAction` and `LoadTaskUseCase` for the same loading responsibility.

## Repository And DataSource Naming

Use role-based names for domain contracts and technology-prefixed names for concrete infrastructure. Follow the project's existing suffix style: use either `Repo` or `Repository`, but do not mix both styles in one project.

General shape:

- Domain repository contract: `<Subject>Repository` or `<Subject>Repo`
- Technology-backed repository implementation: `<Tech><Subject>Repository` or `<Tech><Subject>Repo`
- Fallback implementation when no technology prefix is meaningful: `Default<Subject>Repository` or `Default<Subject>Repo`
- Technology-backed data source: `<Tech><Subject>DataSource`

Examples:

```kotlin
interface TaskRepository {
    suspend fun loadTask(taskId: TaskId): Result<Task, LoadTaskError>
}

class ApiTaskRepository(
    private val dataSource: ApiTaskDataSource,
) : TaskRepository

class FirestoreTaskRepository(
    private val dataSource: FirestoreTaskDataSource,
) : TaskRepository

class DefaultTaskRepository(
    private val apiDataSource: ApiTaskDataSource,
    private val cacheDataSource: SqliteTaskDataSource,
) : TaskRepository

class SqliteTaskDataSource
class ApiTaskDataSource
class FirestoreTaskDataSource
```

If the project uses `Repo` suffixes, the same examples become `TaskRepo`, `ApiTaskRepo`, `FirestoreTaskRepo`, and `SqliteTaskDataSource`.

Prefer concrete technology prefixes users can recognize from the dependency:

- `Api`
- `Firestore`
- `Sqlite`
- `Room`
- `InMemory`
- `Fake`

Use `Default<Subject>Repository` or `Default<Subject>Repo` only when no single technology prefix explains the implementation better, for example orchestration across multiple data sources.

Avoid vague implementation names:

- `TaskRepositoryImpl`
- `TaskRepoImpl`
- `RemoteTaskRepository` when `ApiTaskRepository` or `FirestoreTaskRepository` is more precise.
- `LocalTaskRepository` when `SqliteTaskRepository`, `RoomTaskRepository`, or `InMemoryTaskRepository` is more precise.

## Repository Is Optional

Use a repository when it adds a real boundary:

- Remote/local coordination.
- Caching.
- Persistence.
- Data mapping shared by multiple callers.
- Multiple data sources.
- A stable abstraction over changing infrastructure.

Skip the repository when the use case/action can perform simple local domain work directly without hiding meaningful infrastructure:

```kotlin
class ValidateTaskTitleUseCase {
    suspend operator fun invoke(title: String): Result<Unit, ValidateTaskTitleError> {
        return if (title.isBlank()) {
            Result.failure(ValidateTaskTitleError.EmptyTitle)
        } else {
            Result.success(Unit)
        }
    }
}
```

## Result-Based Errors

Use `com.github.kittinunf.result.Result` for expected failures. The library models `Result<V : Any?, E : Throwable>`, where success carries a value and failure carries a `Throwable` subtype.

Recommended dependency:

```kotlin
implementation("com.github.kittinunf.result:result-jvm:<version>")
```

For Kotlin Multiplatform, use:

```kotlin
implementation("com.github.kittinunf.result:result:<version>")
```

Project rule:

- Repository and lower layers may catch or create exceptions when adapting platform/API failures.
- Repository outputs should convert expected failures into `Result`.
- Repository functions are suspend by default.
- Use cases/actions return `Result` from suspend `invoke` functions and should not throw for expected failures.
- ViewModels consume `Result` with `fold`, `success`/`failure`, `map`, or `flatMap`.
- Do not call `Result.get()` above repositories because the library documents it as throwing on failure.

Prefer typed error classes:

```kotlin
sealed class LoadTaskError(message: String? = null) : Throwable(message) {
    data object NotFound : LoadTaskError()
    data object Offline : LoadTaskError()
    data class Unknown(val cause: Throwable) : LoadTaskError(cause.message)
}
```

## TDD Workflow

For every behavior change:

1. Write a failing test for the use case/action behavior.
2. Verify the test fails for the expected reason.
3. Implement the smallest use case/action and repository code needed.
4. Verify the test passes.
5. Add failure-path tests.
6. Refactor while tests stay green.

## Examples

Action with repository:

```kotlin
class LoadTaskAction(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: TaskId): Result<Task, LoadTaskError> {
        return repository.loadTask(taskId)
    }
}

interface TaskRepository {
    suspend fun loadTask(taskId: TaskId): Result<Task, LoadTaskError>
}
```

ViewModel handling:

```kotlin
suspend fun load(taskId: TaskId) {
    loadTaskAction(taskId).fold(
        success = { task ->
            updateState { TaskDetailUiState.Content(task.title) }
        },
        failure = { error ->
            updateState { TaskDetailUiState.Error(error.message ?: "Task not loaded") }
        },
    )
}
```

Repository adapting exceptions:

```kotlin
class ApiTaskRepository(
    private val api: TaskApi,
) : TaskRepository {
    override suspend fun loadTask(taskId: TaskId): Result<Task, LoadTaskError> {
        return Result.of {
            api.loadTask(taskId.value).toDomain()
        }.mapError { throwable ->
            when (throwable) {
                is HttpNotFoundException -> LoadTaskError.NotFound
                is IOException -> LoadTaskError.Offline
                else -> LoadTaskError.Unknown(throwable)
            }
        }
    }
}
```

## Migration Checklist

- A use case/action exists for the feature behavior.
- `Action` is used for app/technical operations.
- `UseCase` is used for business workflows.
- No action and use case duplicate the same responsibility.
- Every use case/action has `suspend operator fun invoke`.
- Repository functions are suspend by default.
- Repository contracts use `<Subject>Repository` or `<Subject>Repo`; concrete implementations use `<Tech><Subject>Repository`, `<Tech><Subject>Repo`, `Default<Subject>Repository`, or `Default<Subject>Repo`.
- Data sources use `<Tech><Subject>DataSource`.
- Repository exists only if it adds a meaningful boundary.
- Code above repositories returns `Result` for expected failures.
- No `throw` for expected failures above repositories.
- No `Result.get()` above repositories.
- Tests cover success and failure paths before implementation is marked done.
