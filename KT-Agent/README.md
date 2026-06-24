# KT-Agent 🤖

**Knowledge Transfer Document Generator** for Spring Boot Microservices.

Analyzes your Spring Boot project source code, generates a timestamped KT document, commits it to GitHub, and then GitHub Actions automatically notifies the reviewer.

---

## How it works

```
You run: node KT-Agent/scripts/generate-kt.js
         │
         ▼
Agent analyzes src/ → generates KT-Doc-<timestamp>.md
         │
         ▼
Agent commits the file to GitHub via REST API
         │
         ▼
GitHub detects push to KT-Agent/output/*.md
         │
         ▼
GitHub Actions workflow triggers (.github/workflows/kt-agent.yml)
         │
         ▼
Workflow creates a GitHub Issue → assigns to KT_REVIEWER
         │
         ▼
Reviewer gets GitHub email notification ✅
```

The agent script and the reviewer notification are **cleanly separated**:
- The **script** only generates + commits
- **GitHub Actions** handles the notification

---

## Prerequisites

- Node.js 18+
- A GitHub repository with push access
- `ANTHROPIC_API_KEY` is **not required**

---

## Setup

### 1. Configure the agent (local)

```bash
cp KT-Agent/.env.example KT-Agent/.env
```

Edit `KT-Agent/.env` — only 3 things needed:

```env
GITHUB_TOKEN=ghp_your_token_here
GITHUB_REPO=your-org/your-repo
GITHUB_BRANCH=main
```

**GitHub Token permissions needed:** `contents: write` only.

### 2. Configure GitHub Actions (for reviewer notification)

In your repo → **Settings → Secrets and variables → Actions**:

**Secrets tab:**

| Name | Value |
|------|-------|
| `GH_PAT` | Your GitHub PAT (contents + issues write) |

**Variables tab:**

| Name | Value |
|------|-------|
| `KT_REVIEWER` | GitHub username to assign the review issue to |

> If `KT_REVIEWER` is not set, the issue is still created — just not assigned to anyone.

---

## Running the agent

```bash
# macOS / Linux — from project root
export $(cat KT-Agent/.env | xargs) && node KT-Agent/scripts/generate-kt.js .

# Windows PowerShell
Get-Content KT-Agent\.env | ForEach-Object {
  $name, $value = $_ -split '=', 2
  [System.Environment]::SetEnvironmentVariable($name, $value)
}
node KT-Agent\scripts\generate-kt.js .
```

Or via VSCode: `Ctrl+Shift+P` → **Tasks: Run Task** → **KT-Agent: Generate KT Document**

---

## AI vs Static summary

| Mode | When | Result |
|------|------|--------|
| **Static** (default) | No `ANTHROPIC_API_KEY` | Auto-generated 3-paragraph summary from code analysis |
| **AI-powered** | `ANTHROPIC_API_KEY` set in `.env` | Claude writes a narrative overview from actual source snippets |

Both modes produce an equally complete KT document.

---

## Output

Saved to: `KT-Agent/output/KT-Doc-<YYYY-MM-DDTHH-MM-SS>.md`

Sections included:
1. Project overview
2. Architecture ASCII diagram
3. Layer structure + file tree
4. Data model schemas (entity fields)
5. REST API reference table
6. Exception handling map
7. Dependencies & configuration
8. How to run

---

## Making it generic

Drop `KT-Agent/` into the root of any Spring Boot project. It works with any project that follows standard package conventions (`controller/`, `service/`, `repository/`, `entity/`, `dto/`).
