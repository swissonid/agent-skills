# Agent Skills

A collection of skills for AI coding agents.

Skills follow the [Agent Skills](https://agentskills.io/) format.

## Available Skills

### android-mvi

Use this skill when creating, migrating, or reviewing Android MVI screens in Kotlin or Jetpack Compose. Apply it to name `UserIntent`, `UiState`, `SideEffect`, and `MVIViewModel` contracts; separate persistent render state from one-shot effects; enforce exhaustive sealed-interface handling; or document shared MVI primitives.

### skill-write

Use this skill when creating, editing, validating, evaluating, or preparing Agent Skills for publication; especially when a skill needs concise `SKILL.md` instructions, trigger-focused frontmatter, progressive disclosure, evals, forward-testing, or skills.sh metadata.

#### Install

<details open>
<summary>bun</summary>
List available skills:

```bash
bunx skills add swissonid/agent-skills --list
```

Install `android-mvi` for Codex globally:

```bash
bunx skills add swissonid/agent-skills --skill android-mvi
```

Install `skill-write`:

```bash
bunx skills add swissonid/agent-skills --skill skill-write
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

Install `skill-write`:

```bash
pnpm dlx skills add swissonid/agent-skills --skill skill-write
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

Install `skill-write`:

```bash
npx skills add swissonid/agent-skills --skill skill-write
```
</details>
