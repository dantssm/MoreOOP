---
name: github-repo-analyzer
description: Connect to remote GitHub repositories using the GitHub REST API and a locally stored PAT to list repository contents, inspect directory structures, read file contents, or generate structured analysis reports. Use when users ask to explore, browse, inspect, or analyze a GitHub repository without cloning it.
allowed-tools:
  - Bash(curl:*)
  - Bash(mkdir:*)
  - Write
---

# GitHub Repository Analyzer

## Overview

This skill helps explore **remote GitHub repositories** through the GitHub REST API to list repository contents, inspect directory structures, read file contents, and generate structured analysis reports.

## When to Use

- List repository files
- Display repository directory trees
- Read file contents
- Generate a structured analysis report

## Scope
Only perform the action the user explicitly asked for, and nothing beyond it:
- "Show structure" / "explore" / "analyze the structure" means Action A (list tree) only. Do not also fetch individual file contents unless asked.
- Do not generate summary reports, diagrams, design-pattern write-ups, or any other analysis artifacts unless the user explicitly requests them (see Action C).
- For Actions A and B, print output directly in the response — do not create scratch scripts, Python helpers, or files. `curl` is the only tool those actions need.
- Action C is the sole exception to "no files": it explicitly requires writing `output/analysis.json` and `output/report.md`. Do not write any other files, and do not write these two unless Action C was explicitly triggered.
- If you think a follow-up action would be useful (e.g. reading a specific file after listing the tree), ask the user first instead of doing it.

## Process

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
* *Reviewer stance:* Act as a strict, skeptical senior reviewer, not a friendly assistant. Default to finding problems rather than praising. Do not soften findings with hedging language ("might", "could potentially") — state issues directly. A repo with no real issues is rare; if `issues` would otherwise be empty, look harder at error handling, test coverage, naming, and edge cases before concluding there are none.
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
* *Minimums:* `issues` and `recommendations` must each contain at least 3 items. `strengths` is capped at 3 items even if more could be listed — strict mode is not about withholding praise, but about not over-crediting. Each entry must be specific (name a file, class, or pattern) — reject vague entries like "code could be cleaner" in favor of "Knight.java has no null check on `weapon` before use in `attack()`".
* *Save:* create the `output/` directory if missing (`mkdir -p output`), then write:
    * `output/analysis.json` — the raw JSON, valid and parseable
    * `output/report.md` — the same content as readable Markdown, one heading per field (`## Summary`, `## Technologies`, `## Strengths`, `## Issues`, `## Recommendations`)
* Confirm to the user both files were written, and show the JSON inline in the response as well.

### Step 5: Handle Potential Errors

* If you receive an error from the API, handle it gracefully:
    * **401 Unauthorized:** Inform the user that the GitHub token is invalid or expired.
    * **403 Forbidden:** Explain that the token lacks permission or GitHub rate limiting has been reached.
    * **404 Not Found:** Explain that the repository, branch, or file could not be found. Check for typos in the user's prompt.
    * **Empty Response:** Explain that no matching files or directories were found.