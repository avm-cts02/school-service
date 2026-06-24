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
         │
         ▼
Reviewer ticks all checklist boxes → closes issue
         │
         ▼
kt-review-closed workflow triggers → notifies KT doc creator ✅
```

The agent script and the notifications are **cleanly separated**:
- The **script** only generates + commits
- **GitHub Actions** handles all notifications

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

Edit `KT-Agent/.env` with your values:

```env
GITHUB_TOKEN=ghp_your_token_here
GITHUB_REPO=your-org/your-repo
GITHUB_BRANCH=main
KT_CREATOR=your-github-username
NODE_TLS_REJECT_UNAUTHORIZED=0
```

> `NODE_TLS_REJECT_UNAUTHORIZED=0` is needed on corporate networks (e.g. Cognizant)
> where a company SSL certificate is installed. Safe to use locally.

> `KT_CREATOR` is your GitHub username — embedded in the commit message so the
> review-closed workflow knows who to notify when review is complete.

**GitHub Token permissions needed:** `contents: write` + `issues: write`

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

Every time you open a **new terminal**, run these commands in order:

### Step 1 — Load environment variables

**Windows PowerShell:**
```powershell
Get-Content KT-Agent\.env | Where-Object { $_ -notmatch '^\s*#' -and $_ -match '=' } | ForEach-Object { $name, $value = $_ -split '=', 2; [System.Environment]::SetEnvironmentVariable($name, $value.Trim()) }
```

**macOS / Linux:**
```bash
export $(cat KT-Agent/.env | grep -v '^#' | xargs)
```

> ⚠️ This step is required every time you open a new terminal.
> Environment variables are not persisted between terminal sessions.

### Step 2 — Set SSL bypass (corporate network only)

**Windows PowerShell:**
```powershell
$env:NODE_TLS_REJECT_UNAUTHORIZED="0"
```

**macOS / Linux:**
```bash
export NODE_TLS_REJECT_UNAUTHORIZED=0
```

> Skip this step if `NODE_TLS_REJECT_UNAUTHORIZED=0` is already in your `.env` —
> Step 1 will have loaded it automatically.

### Step 3 — Run the agent

**Windows PowerShell:**
```powershell
node KT-Agent\scripts\generate-kt.js .
```

**macOS / Linux:**
```bash
node KT-Agent/scripts/generate-kt.js .
```

### Expected output
```
🤖 KT-Agent starting...
📁 Analyzing project at: .
✅ Found: 18 Java files, 10 endpoints
ℹ️  ANTHROPIC_API_KEY not set — using static summary instead.
📄 KT document saved: KT-Agent/output/KT-Doc-<timestamp>.md
✅ Committed to GitHub: https://github.com/your-org/your-repo/blob/main/KT-Agent/output/...
⏳ GitHub Actions will now trigger reviewer notification...
✅ KT-Agent complete.
```

---

## Workflows

Two GitHub Actions workflows handle notifications:

| File | Triggers on | Does |
|------|------------|------|
| `kt-agent.yml` | Push to `KT-Agent/output/*.md` | Creates review issue, assigns to `KT_REVIEWER` |
| `kt-review-closed.yml` | Issue closed | Enforces checklist, notifies `KT_CREATOR` on completion |

### Checklist enforcement
If the reviewer closes the issue **without ticking all boxes**:
- Issue is **automatically reopened**
- Warning comment posted tagging the reviewer

If closed **with all boxes ticked**:
- Completion comment posted tagging `@KT_CREATOR`
- Creator gets GitHub email notification

---

## AI vs Static summary

| Mode | When | Result |
|------|------|--------|
| **Static** (default) | No `ANTHROPIC_API_KEY` | Auto-generated summary from code analysis |
| **AI-powered** | `ANTHROPIC_API_KEY` set in `.env` | Claude writes a narrative overview |

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

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `GITHUB_TOKEN or GITHUB_REPO not set` | Run Step 1 (load env vars) before running agent |
| `fetch failed` | Run Step 2 (SSL bypass) — corporate network issue |
| `0 endpoints detected` | Ensure `generate-kt.js` is the latest version |
| `git push rejected` | Run `git pull origin main --rebase` then push again |
| Issue not created | Check `GH_PAT` secret is set in GitHub Actions secrets |
| Reviewer not notified | Check `KT_REVIEWER` variable is set in GitHub Actions variables |

---

## Making it generic

Drop `KT-Agent/` into the root of any Spring Boot project. It works with any project
that follows standard package conventions (`controller/`, `service/`, `repository/`, `entity/`, `dto/`).