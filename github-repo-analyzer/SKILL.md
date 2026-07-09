---
name: github-repo-analyzer
description: Connect to remote GitHub repositories using the GitHub REST API and a locally stored PAT to list repository contents, inspect directory structures, read file contents, generate structured analysis reports, or review a pull request's diff. Use when users ask to explore, browse, inspect, analyze, or review a GitHub repository or pull request without cloning it.
allowed-tools:
  - Bash(curl:*)
  - Bash(mkdir:*)
  - Write
---

# GitHub Repository Analyzer

## Overview

This skill helps explore **remote GitHub repositories** through the GitHub REST API to list repository contents, inspect directory structures, read file contents, generate structured analysis reports, and review pull request diffs.

## When to Use

- List repository files
- Display repository directory trees
- Read file contents
- Generate a structured analysis report
- Review a pull request's changes

## Scope
Only perform the action the user explicitly asked for, and nothing beyond it:
- "Show structure" / "explore" / "analyze the structure" means Action A (list tree) only. Do not also fetch individual file contents unless asked.
- Do not generate summary reports, diagrams, design-pattern write-ups, or any other analysis artifacts unless the user explicitly requests them (see Action C).
- For Actions A and B, print output directly in the response — do not create scratch scripts, Python helpers, or files. `curl` is the only tool those actions need.
- Action C is the sole exception to "no files" for analysis: it explicitly requires writing `output/analysis.json` and `output/report.md`. Do not write these unless Action C was explicitly triggered.
- Action D is the other exception: it explicitly requires writing `output/review.md`. Do not write it unless Action D was explicitly triggered. Action D never posts comments directly to GitHub — output is always the local `review.md` file, never a live API write to the PR.
- Do not write any files beyond what Actions C and D each specify.
- If you think a follow-up action would be useful (e.g. reading a specific file after listing the tree), ask the user first instead of doing it.

## Process

### Step 0: No Detours
This SKILL.md is the only source of instructions you need. Before doing anything else:
- Do NOT search for other skills, list your skills directory, or check permissions/tool inventories.
- Do NOT run `git status`, `git log`, `ls`, or explore the local filesystem beyond reading this file — this skill only talks to `api.github.com`, it has no dependency on local git state.
- Do NOT ask "where is the skill file" if the user already told you — use the path or reference given.
- Go straight to Step 1 and start making the API calls the requested action needs.

### Step 1: Analyze Requirements

* **Authenticate:** Always authenticate requests using a GitHub Personal Access Token.
    * Use the environment variable: `$GITHUB_TOKEN`
    * Inject into curl: `-H "Authorization: Bearer $GITHUB_TOKEN"`
    * Always include: `-H "Accept: application/vnd.github+json"`
    * If `$GITHUB_TOKEN` is missing: Stop execution, inform the user that authentication is required, and ask them to export `GITHUB_TOKEN`. Do not attempt unauthenticated requests.
* **Security Check:** Never log secrets. Never echo the value of `$GITHUB_TOKEN`. Never expose Authorization headers to the user.
* **Filesystem Check:** Ensure you are NOT using local commands (`ls`, `cat`, `find`, `git clone`) unless the user explicitly requested local file operations. You must only use `curl` to `api.github.com` for fetching data. The only local writes permitted are the `output/` files described in Action C.

### Step 2: Parse User Input

* Accept repository targets in any of these forms and extract the necessary parameters:
    * `owner/repo`
    * `https://github.com/owner/repo`
    * `https://github.com/owner/repo/tree/branch`
    * `https://github.com/owner/repo/blob/branch/path/file`
* Extract the `owner`, `repo`, `branch` (if supplied), and `file path` (if supplied).

### Step 3: Determine Target Branch

* If the user did NOT explicitly provide a branch in their request:
    * Make a request: `GET /repos/{owner}/{repo}`
    * Read the `default_branch` from the JSON response.
    * Use this default branch for all subsequent requests in Step 4.

### Step 4: Execute Requested Action

**Action A: List Repository Structure**
* *Trigger:* User asks to show structure, list files, or explore the repo.
* *API Call:* `GET /repos/{owner}/{repo}/git/trees/{branch}?recursive=1`
* *Formatting:* Display the directories and files as a readable tree structure in the console. Preserve directory hierarchy and sort directories before files.
* *Stop here.* Do not proceed to Action B or C unless the user separately asks for them.

**Action B: Read File Contents**
* *Trigger:* User asks to read, show, or output a specific file.
* *API Call:* `GET /repos/{owner}/{repo}/contents/{path}?ref={branch}`
* *Decoding:* The response contains a `content` field and an `encoding` field. If `encoding == base64`, you MUST decode the content before displaying it. Never display raw Base64 unless explicitly requested.
* *Formatting:* Display the decoded text, preserving the original formatting. If the file is extremely large, inform the user and display only the first portion.

**Action C: Generate Structured Analysis**
* *Trigger:* User explicitly asks for an analysis, report, or structured output.
* *Context to gather:* directory tree (Action A) + contents of the key files relevant to understanding the project (e.g. entry points, main classes — not every file). Fetch only what's needed to fill the schema below; don't pull the whole repo.
* *Output format:* strictly this JSON schema, no extra top-level keys:
  ```json
  {
    "summary": "1-3 sentence description of what the repo does",
    "technologies": ["list", "of", "languages/frameworks/tools detected"],
    "strengths": ["list of things the project does well"],
    "issues": ["list of potential problems, code smells, or missing pieces"],
    "recommendations": ["list of concrete suggested improvements"]
  }
  ```
* *Save:* create the `output/` directory if missing (`mkdir -p output`), then write:
    * `output/analysis.json` — the raw JSON, valid and parseable
    * `output/report.md` — the same content as readable Markdown, one heading per field (`## Summary`, `## Technologies`, `## Strengths`, `## Issues`, `## Recommendations`)
* Confirm to the user both files were written, and show the JSON inline in the response as well.

**Action D: PR Review Report**
* *Trigger:* User explicitly asks to review a pull request (e.g. "review PR #3").
* *Parse input:* accept a PR number alone (if owner/repo already established in conversation) or a full PR URL like `https://github.com/owner/repo/pull/3`.
* *API Call:* `GET /repos/{owner}/{repo}/pulls/{pr_number}/files` — returns each changed file with a `patch` field containing that file's diff. Use this field directly; do not attempt to reconstruct the diff from full file contents.
* *Analysis:* for each changed file's patch, look only at lines actually present in the diff and note, where relevant:
    * potential bugs
    * style or architecture concerns
    * optimization opportunities
    * edge cases not handled
    * concrete improvement suggestions
* *Categorize accurately, don't default everything to one label.* Use the label that matches what actually happened:
    * **Bug fix** — the diff resolves a genuine defect (e.g. an infinite loop, a null-safety issue, incorrect logic)
    * **Refactor** — the diff restructures code without changing behavior (e.g. extracting a shared base class, deduplication)
    * **Style** — the diff is a naming, formatting, or readability-only change with no structural or behavioral impact
    * **Bug** — a new finding: a defect that still exists in the code after this diff
    * **Edge case** — a scenario the code doesn't handle correctly, whether introduced or pre-existing
    * When multiple labels could apply to one change (e.g. a fix that's also a simplification), pick the most significant one rather than stacking labels.
* *Discipline:* only comment on what's visible in the diff. Do not speculate about unchanged code you haven't seen. If a file has no notable findings, say so briefly rather than omitting it or padding with filler.
* *Save:* create the `output/` directory if missing (`mkdir -p output`), then write `output/review.md`, one section per changed file:
  ```markdown
  ## path/to/File
  - **Bug fix:** ...
  - **Refactor:** ...
  - **Style:** ...
  - **Bug:** ...
  - **Edge case:** ...
  ```
* *Never* post comments to the PR via the GitHub API — this action only produces the local `review.md` file.
* Confirm to the user the file was written, and show a brief summary inline.

### Step 5: Handle Potential Errors

* If you receive an error from the API, handle it gracefully:
    * **502 Bad Gateway / 503 Service Unavailable:** These are transient GitHub-side errors, not a problem with your request. Retry the exact same request once after a short pause. If it fails again, tell the user GitHub's API is temporarily unavailable and suggest trying again shortly — don't reinterpret it as an auth or repo-existence problem.
    * **401 Unauthorized:** Inform the user that the GitHub token is invalid or expired.
    * **403 Forbidden:** Explain that the token lacks permission or GitHub rate limiting has been reached.
    * **404 Not Found:** Explain that the repository, branch, or file could not be found. Check for typos in the user's prompt.
    * **Empty Response:** Explain that no matching files or directories were found.
* **Debugging tip:** never pipe raw `curl` output into `head` or other truncating commands — this can throw a misleading "Failure writing output" error unrelated to the actual API response. Capture output to a variable first (`resp=$(curl -s ...)`), then inspect it.