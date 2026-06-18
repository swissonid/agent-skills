---
name: android-clean-architecture
description: Use when creating, migrating, or reviewing Android Kotlin features with clean architecture boundaries, suspend use case or action classes, optional suspend repositories, Result-based failure handling, no thrown exceptions above repositories, and TDD-first implementation.
---

# Android Clean Architecture

## Overview

Apply project clean architecture conventions for Android features: every feature gets a suspend use case or action boundary, repository layers are optional, and failures above repositories are modeled with `com.github.kittinunf.result.Result` instead of thrown exceptions.

## Workflow

1. Use TDD before implementation: write the failing test, verify it fails for the expected reason, implement the minimum code, then refactor.
2. Read [clean architecture conventions](references/clean-architecture-conventions.md) before adding or changing domain, data, or presentation boundaries.
3. Choose `Action` or `UseCase` by semantics:
   - Use `Action` for app/technical operations such as loading, syncing, refreshing, tracking, or platform work.
   - Use `UseCase` for business workflows or product capabilities that may coordinate actions.
   - Do not create both an action and a use case for the same responsibility.
4. Always introduce an action or use case for feature behavior, even when there is no repository.
5. Add a repository only when it creates a useful boundary for data access, caching, remote/local coordination, or shared data behavior.
6. Ensure everything above repositories returns `Result<Success, ErrorThrowable>` or an equivalent project alias and does not throw.
7. Treat use cases/actions and repositories as asynchronous by default; use `suspend operator fun invoke(...)` and suspend repository functions.
8. Keep UI and ViewModels dependent on use cases/actions, not repositories or data sources.

## Quality Checks

- Tests prove the use case/action success and failure paths.
- No presentation or domain code catches broad exceptions from lower layers unless converting them into `Result`.
- Use case/action naming is consistent across the project.
- Use cases/actions and repositories are suspend by default.
- Repository absence is intentional, not accidental.
- `Result.get()` is not used above repositories because it can throw.

## References

- Read [clean architecture conventions](references/clean-architecture-conventions.md) for layer rules, Result usage, examples, and migration checks.
