# Agent Skills

A collection of skills for AI coding agents.

Skills follow the [Agent Skills](https://agentskills.io/) format.

## Available Skills

### android-mvi

Use this skill when creating, migrating, or reviewing Android MVI screens in Kotlin or Jetpack Compose. Apply it to name `UserIntent`, `UiState`, `SideEffect`, and `MVIViewModel` contracts; separate persistent render state from one-shot effects; enforce exhaustive sealed-interface handling; or document shared MVI primitives.

### android-clean-architecture

Use this skill when creating, migrating, or reviewing Android Kotlin features with clean architecture boundaries, suspend use case or action classes, optional suspend repositories, Result-based failure handling, no thrown exceptions above repositories, and TDD-first implementation.

#### Install

<details open>
<summary>bun</summary>
List available skills:

```bash
bunx skills add swissonid/agent-skills --list
```

Install `android-mvi`:

```bash
bunx skills add swissonid/agent-skills --skill android-mvi
```

Install `android-clean-architecture`:

```bash
bunx skills add swissonid/agent-skills --skill android-clean-architecture
```
</details>

<details>
<summary>pnpm</summary>
List available skills:

```bash
pnpm dlx skills add swissonid/agent-skills --list
```

Install `android-mvi`:

```bash
pnpm dlx skills add swissonid/agent-skills --skill android-mvi
```

Install `android-clean-architecture`:

```bash
pnpm dlx skills add swissonid/agent-skills --skill android-clean-architecture
```

</details>

<details>
<summary>npm</summary>

```bash
npx skills add swissonid/agent-skills --list
```

Install `android-mvi`:

```bash
npx skills add swissonid/agent-skills --skill android-mvi
```

Install `android-clean-architecture`:

```bash
npx skills add swissonid/agent-skills --skill android-clean-architecture
```
</details>
