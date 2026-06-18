# Skill Authoring Checklist

Use this reference when creating, editing, validating, or preparing an Agent Skill for publication.

## Table Of Contents

- [Plan The Skill](#plan-the-skill)
- [Write Evals First](#write-evals-first)
- [Create The Skill Folder](#create-the-skill-folder)
- [Write SKILL.md](#write-skillmd)
- [Use Progressive Disclosure](#use-progressive-disclosure)
- [Add Metadata](#add-metadata)
- [Validate](#validate)
- [Forward-Test](#forward-test)
- [Common Mistakes](#common-mistakes)

## Plan The Skill

- Confirm the skill is reusable across tasks or projects.
- Prefer a short, active, hyphen-case name such as `skill-write`.
- Place the folder under `skills/<skill-name>` for skills.sh-compatible repositories.
- Avoid one-off narratives, project diary notes, and broad generic advice.
- Decide whether the skill is a technique, pattern, reference, or tool-backed workflow.

## Write Evals First

Create evals before declaring the skill done:

- `evals/evals.json`: realistic output-quality prompts with expected output and assertions.
- `eval_queries.json`: positive and negative trigger queries.

For discipline-enforcing skills, include pressure scenarios that tempt the agent to skip required steps. Capture expected rationalizations in assertions.

## Create The Skill Folder

Preferred layout:

```text
skills/<skill-name>/
  SKILL.md
  agents/openai.yaml
  references/
  evals/evals.json
  eval_queries.json
```

Only include `scripts/` or `assets/` when they directly support execution. Do not add README, changelog, install guide, or extra documentation inside a skill folder.

## Write SKILL.md

Frontmatter:

- Include only `name` and `description`.
- Keep `name` lowercase hyphen-case.
- Start `description` with `Use when`.
- Describe trigger conditions and symptoms, not the workflow.
- Keep the description specific enough for discovery and under 1024 characters.

Body:

- Start with a one- or two-sentence overview.
- Use imperative steps.
- Keep core instructions concise.
- Link to one-level-deep references with Markdown links.
- Include a quick quality bar or checklist when useful.

## Use Progressive Disclosure

- Keep `SKILL.md` small and high-signal.
- Move long examples, API details, schemas, policies, and checklists into `references/`.
- Add a table of contents to reference files longer than about 100 lines.
- Avoid duplicating the same rule in `SKILL.md` and references unless it is truly essential.

## Add Metadata

For Codex UI metadata, add `agents/openai.yaml`:

```yaml
interface:
  display_name: "Skill Write"
  short_description: "Write and verify agent skills"
  default_prompt: "Use $skill-write to create a tested Agent Skill from this idea."
```

For skills.sh repositories, add or update root `skills.sh.json`:

```json
{
  "groupings": [
    {
      "title": "Skill Authoring",
      "description": "Skills for creating, testing, and publishing Agent Skills.",
      "skills": ["skill-write"]
    }
  ]
}
```

## Validate

Run the checks available in the environment:

```bash
python3 -m json.tool skills/<skill-name>/evals/evals.json
python3 -m json.tool skills/<skill-name>/eval_queries.json
python3 -m json.tool skills.sh.json
```

If available, run official validation:

```bash
skills-ref validate skills/<skill-name>
```

If official validation is unavailable, state that limitation and record the checks that did pass.

## Forward-Test

Use fresh agents when available:

1. Run at least one baseline scenario without the skill.
2. Run the same scenario with the skill.
3. Compare behavior against assertions.
4. Patch the skill where agents miss steps, overread, skip evals, or treat the description as enough.

Use prompts like:

```text
Use $skill-write at /path/to/skills/skill-write to create a skill from this idea: ...
```

Do not leak the expected answer into forward-test prompts.

## Common Mistakes

| Mistake | Fix |
| --- | --- |
| Description summarizes workflow | Describe when to use the skill only |
| Giant SKILL.md | Move long detail into `references/` |
| No evals | Add output-quality and trigger evals |
| Untested metadata | Validate JSON and install/list commands |
| Project-specific story | Extract reusable convention or technique |
| Too many optional files | Keep only files that directly support use |
