# Agent Skills

A collection of skills for AI coding agents.

Skills follow the [Agent Skills](https://agentskills.io/) format.

## Available Skills

### android-mvi (thx to @soracel for co-authoring)

Use this skill when creating, migrating, or reviewing Android MVI screens in Kotlin or Jetpack Compose. Apply it to name `UserIntent`, `UiState`, `SideEffect`, and `MVIViewModel` contracts; separate persistent render state from one-shot effects; enforce exhaustive sealed-interface handling; or document shared MVI primitives.

#### Install android-mvi

<details open>
<summary>bun</summary>

```bash
bunx skills add swissonid/agent-skills --skill android-mvi
```

</details>

<details>
<summary>pnpm</summary>

```bash
pnpm dlx skills add swissonid/agent-skills --skill android-mvi
```

</details>

<details>
<summary>npm</summary>

```bash
npx skills add swissonid/agent-skills --skill android-mvi
```

</details>

### android-clean-architecture (thx to @soracel for co-authoring)

Use this skill when creating, migrating, or reviewing Android Kotlin features with clean architecture boundaries, suspend use case or action classes, optional suspend repositories, Result-based failure handling, no thrown exceptions above repositories, and TDD-first implementation.

#### Install android-clean-architecture

<details open>
<summary>bun</summary>

```bash
bunx skills add swissonid/agent-skills --skill android-clean-architecture
```

</details>

<details>
<summary>pnpm</summary>

```bash
pnpm dlx skills add swissonid/agent-skills --skill android-clean-architecture
```

</details>

<details>
<summary>npm</summary>

```bash
npx skills add swissonid/agent-skills --skill android-clean-architecture
```

</details>

### android-clean-mvi (thx to @soracel for co-authoring)

Use this skill when creating, migrating, or reviewing a complete Android Kotlin or Jetpack Compose screen that combines MVI presentation contracts with clean architecture boundaries, including `UserIntent`, `UiState`, `SideEffect`, `MVIViewModel`, `Action` or `UseCase` dependencies, `Result`-based errors, and no ViewModel access to repositories or data sources.

#### Install android-clean-mvi

<details open>
<summary>bun</summary>

```bash
bunx skills add swissonid/agent-skills --skill android-clean-mvi
```

</details>

<details>
<summary>pnpm</summary>

```bash
pnpm dlx skills add swissonid/agent-skills --skill android-clean-mvi
```

</details>

<details>
<summary>npm</summary>

```bash
npx skills add swissonid/agent-skills --skill android-clean-mvi
```

</details>
