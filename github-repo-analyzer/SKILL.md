---
name: github-repo-analyzer
description: Connect to remote GitHub repositories using the GitHub REST API and a locally stored PAT to list repository contents, inspect directory structures, read file contents, or generate structured analysis reports. Use when users ask to explore, browse, inspect, or analyze a GitHub repository without cloning it.
allowed-tools:
  - Bash(curl:*)
  - Bash(jq:*)
  - Bash(mkdir:*)
  - Write
---

# GitHub Repository Analyzer

## Overview

This skill helps explore **remote GitHub repositories** through the GitHub REST API to list repository contents, inspect directory structures, read file contents, and generate structured analysis reports.

## Tooling constraint (hard rule)
This skill uses **only `curl`, `jq`, `mkdir`, `sort`, `awk`, and standard shell pipes** for all data fetching, parsing, and formatting. Never write a Python script, Node script, or any other scratch program to fetch data, parse JSON, decode base64, or format output — `curl` for HTTP, `jq` for JSON, and shell text tools for formatting cover every action in this skill. If a formatting task seems to need more than that, simplify the output instead of reaching for another language. This applies even when the task looks awkward in pure shell — awkward shell is still preferred over stepping outside the declared tool set.

## No local exploration
Do not list, browse, or inspect the local project directory (e.g. `ListDir`, `ls`, `find` on the workspace) before acting on a request. This skill's job is entirely remote — everything it needs comes from the GitHub API via `curl`. The only local filesystem interaction this skill ever performs is writing the specific `output/` files Action C describes. Go straight from reading the user's request to Step 1 (authenticate) — do not inspect the workspace first "to understand the project."

## When to Use

- List repository files
- Display repository directory trees
- Read file contents
- Generate a structured analysis report

## Ambiguous requests
If the user's request doesn't clearly match one action's trigger phrase (e.g. "analyze the repo" could mean Action A's structure listing or Action C's structured analysis), ask which one they mean before proceeding. Do not invent new behavior, fetch additional files beyond what the matched action specifies (e.g. don't independently decide to pull `README.md` or `pom.xml`), or write scratch scripts to figure out what to do.

## Scope
Only perform the action the user explicitly asked for, and nothing beyond it:
- "Show structure" / "explore" / "analyze the structure" means Action A (list tree) only. Do not also fetch individual file contents unless asked.
- Do not generate summary reports, diagrams, design-pattern write-ups, or any other analysis artifacts unless the user explicitly requests them (see Action C).
- For Actions A and B, print output directly in the response — do not create scratch scripts, Python helpers, or files. `curl` is the only tool those actions need.
- Action C is the sole exception to "no files": it explicitly requires writing `output/analysis.json` and `output/report.md`. Do not write these unless Action C was explicitly triggered.
- Do not write any files beyond what Action C specifies.
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
* *Formatting:* pipe the response through `jq` and shell text tools — do not write a script in another language for this:
  ```bash
  resp=$(curl -s -H "Authorization: Bearer $GITHUB_TOKEN" -H "Accept: application/vnd.github+json" \
    "https://api.github.com/repos/$owner/$repo/git/trees/$branch?recursive=1")
  echo "$resp" | jq -r '.tree[] | (if .type=="tree" then .path+"/" else .path end)' \
    | sort \
    | awk -F/ '{depth=NF-1; name=$NF; if (name=="") {depth=NF-2; name=$(NF-1)"/"}; printf "%*s%s\n", depth*2, "", name}'
  ```
  This sorts paths and indents by directory depth. If the output looks off for a particular repo, adjust the `awk` logic directly rather than switching tools.
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

### Step 5: Handle Potential Errors

* If you receive an error from the API, handle it gracefully:
    * **502 Bad Gateway / 503 Service Unavailable:** These are transient GitHub-side errors, not a problem with your request. Retry the exact same request once after a short pause. If it fails again, tell the user GitHub's API is temporarily unavailable and suggest trying again shortly — don't reinterpret it as an auth or repo-existence problem.
    * **401 Unauthorized:** Inform the user that the GitHub token is invalid or expired.
    * **403 Forbidden:** Explain that the token lacks permission or GitHub rate limiting has been reached.
    * **404 Not Found:** Explain that the repository, branch, or file could not be found. Check for typos in the user's prompt.
    * **Empty Response:** Explain that no matching files or directories were found.
* **Debugging tip:** never pipe raw `curl` output into `head` or other truncating commands — this can throw a misleading "Failure writing output" error unrelated to the actual API response. Capture output to a variable first (`resp=$(curl -s ...)`), then inspect it.