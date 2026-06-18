---
name: skill-write
description: Use when creating, editing, validating, evaluating, or preparing Agent Skills for publication; especially when a skill needs concise SKILL.md instructions, trigger-focused frontmatter, progressive disclosure, evals, forward-testing, or skills.sh metadata.
---

# Skill Write

## Overview

Create skills as tested process documentation: first define realistic eval pressure, then write only the guidance and resources needed for future agents to succeed.

## Workflow

1. Identify the concrete skill task, target agent, repository location, and expected install surface.
2. Read [authoring checklist](references/authoring-checklist.md) before creating or editing skill files.
3. Define eval cases before or alongside the skill:
   - `evals/evals.json` for output quality.
   - `eval_queries.json` for trigger behavior.
4. Create or update the skill folder using the standard layout:
   - `SKILL.md`
   - `agents/openai.yaml` when UI metadata is useful.
   - `references/`, `scripts/`, or `assets/` only when they directly support the skill.
5. Write `SKILL.md` with trigger-focused frontmatter and concise procedural instructions.
6. Move detailed examples, heavy references, schemas, or long checklists into one-level-deep reference files.
7. Validate structure, JSON, links, and eval files before saying the skill is ready.
8. Forward-test with fresh agents when possible. If not possible, state that limitation and leave runnable eval prompts.

## Quality Bar

- Description starts with `Use when` and explains trigger conditions, not workflow.
- Skill name uses lowercase letters, digits, and hyphens only.
- `SKILL.md` stays small enough to scan quickly; prefer references for long details.
- Examples are realistic, reusable, and not project diary entries.
- Evals cover both successful use and common failure modes.
- Publishing metadata groups the skill where users would expect to find it.

## References

- Read [authoring checklist](references/authoring-checklist.md) for the combined `skill-creator`, `writing-skills`, and agentskills.io conventions.
