---
name: github-repo-analyzer
description: Connect to remote GitHub repositories using the GitHub REST API using locally-stored PAT to list repository contents, inspect directory structures, and read file contents. Use when users ask to explore, browse, or inspect a GitHub repository without cloning it.
allowed-tools:
  - Bash(curl:*)
---

# GitHub Repository Analyzer

## Overview

This skill helps exploring **remote GitHub repositories** through the GitHub REST API to look and list repository contents, directory structures, and file contents.

## When to Use

- List repository files
- Display repository directory trees
- Read file contents

## Scope
Only perform the action the user explicitly asked for, and nothing beyond it:
- "Show structure" / "explore" / "analyze the structure" means Action A (list tree) only. Do not also fetch individual file contents unless asked.
- Do not generate summary reports, diagrams, design-pattern write-ups, or any other analysis artifacts unless the user explicitly requests them.
- Print output directly in the response. Do not create scratch scripts, Python helpers, or files to produce this skill's output — `curl` is the only tool this skill needs.
- If you think a follow-up action would be useful (e.g. reading a specific file after listing the tree), ask the user first instead of doing it.

## Process

### Step 1: Analyze Requirements

* **Authenticate:** Always authenticate requests using a GitHub Personal Access Token.
    * Use the environment variable: `$GITHUB_TOKEN`
    * Inject into curl: `-H "Authorization: Bearer $GITHUB_TOKEN"`
    * Always include: `-H "Accept: application/vnd.github+json"`
    * If `$GITHUB_TOKEN` is missing: Stop execution, inform the user that authentication is required, and ask them to export `GITHUB_TOKEN`. Do not attempt unauthenticated requests.
* **Security Check:** Never log secrets. Never echo the value of `$GITHUB_TOKEN`. Never expose Authorization headers to the user.
* **Filesystem Check:** Ensure you are NOT using local commands (`ls`, `cat`, `find`, `git clone`) unless the user explicitly requested local file operations. You must only use `curl` to `api.github.com`.

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
* *Stop here.* Do not proceed to Action B unless the user separately asks to read a file.
**Action B: Read File Contents**
* *Trigger:* User asks to read, show, or output a specific file.
* *API Call:* `GET /repos/{owner}/{repo}/contents/{path}?ref={branch}`
* *Decoding:* The response contains a `content` field and an `encoding` field. If `encoding == base64`, you MUST decode the content before displaying it. Never display raw Base64 unless explicitly requested.
* *Formatting:* Display the decoded text, preserving the original formatting. If the file is extremely large, inform the user and display only the first portion.

### Step 5: Handle Potential Errors

* If you receive an error from the API, handle it gracefully:
    * **401 Unauthorized:** Inform the user that the GitHub token is invalid or expired.
    * **403 Forbidden:** Explain that the token lacks permission or GitHub rate limiting has been reached.
    * **404 Not Found:** Explain that the repository, branch, or file could not be found. Check for typos in the user's prompt.
    * **Empty Response:** Explain that no matching files or directories were found.