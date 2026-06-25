# KT-Agent 🤖

**Knowledge Transfer Document Generator** for Spring Boot Microservices.

Analyzes your Spring Boot project source code, generates a timestamped KT document, commits it to GitHub, and then GitHub Actions automatically notifies the reviewer.

---

## Two ways to run

### Option A — Claude Code Agent (recommended)
If you have [Claude Code](https://claude.ai/code) installed:

1. Open the project in VSCode
2. Open Claude Code (`Ctrl+Shift+P` → "Claude: Open" or click the Claude icon)
3. Type any of these:
   ```
   run the KT agent
   generate KT documentation
   /kt
   ```
4. Claude reads `KT-Agent/CLAUDE.md`, runs the generator, and reports results

**No manual env loading needed** — the agent auto-loads `KT-Agent/.env`.

---

### Option B — Direct command (terminal)
```powershell
# Windows PowerShell — from project root
node KT-Agent\scripts\generate-kt.js .

# macOS / Linux
node KT-Agent/scripts/generate-kt.js .
```

**No manual env loading needed** — the script auto-loads `KT-Agent/.env` on startup.

---

## Setup (one-time)

### 1. Create your .env file
```bash
cp KT-Agent/.env.example KT-Agent/.env
```

Edit `KT-Agent/.env`:
```env
GITHUB_TOKEN=ghp_your_token_here
GITHUB_REPO=your-org/your-repo
GITHUB_BRANCH=main
KT_CREATOR=your-github-username
NODE_TLS_REJECT_UNAUTHORIZED=0
```

> `NODE_TLS_REJECT_UNAUTHORIZED=0` is needed on corporate networks (e.g. Cognizant).

### 2. Set GitHub Actions secrets & variables

In your repo → **Settings → Secrets and variables → Actions**:

**Secrets tab:**
| Name | Value |
|------|-------|
| `GH_PAT` | Your GitHub PAT (contents + issues write) |

**Variables tab:**
| Name | Value |
|------|-------|
| `KT_REVIEWER` | GitHub username(s) of reviewer(s) — comma-separated for multiple |

---

## How it works

```
You say "run the KT agent" (or type /kt)
        │
        ▼
Claude reads KT-Agent/CLAUDE.md (role + instructions)
        │
        ▼
Claude runs: node KT-Agent/scripts/generate-kt.js .
        │
        ▼
Script auto-loads KT-Agent/.env
        │
        ▼
Analyzes 18 Java files → 10 endpoints detected
        │
        ▼
Generates KT-Doc-<timestamp>.md
        │
        ▼
Single commit to GitHub (delete old + add new)
        │
        ▼
GitHub Actions triggers → review issue created → reviewer notified
        │
        ▼
Reviewer ticks checklist → closes issue → creator notified
```

---

## Agent files

| File | Purpose |
|------|---------|
| `CLAUDE.md` | Agent definition — name, role, model, instructions for Claude Code |
| `.claude/commands/kt.md` | Slash command definition — enables `/kt` shortcut |
| `scripts/generate-kt.js` | Core generator — analyzes code, builds doc, commits to GitHub |
| `.env` | Local secrets — never committed to git |
| `.env.example` | Template for .env — committed to repo |

---

## Expected output
```
ℹ️  Loaded env from KT-Agent/.env
🤖 KT-Agent starting...
📁 Analyzing project at: .
✅ Found: 18 Java files, 10 endpoints
ℹ️  ANTHROPIC_API_KEY not set — using static summary instead.
📄 KT document saved: KT-Agent\output\KT-Doc-<timestamp>.md
🗑️  Will remove 1 old KT doc(s) in same commit
✅ Committed to GitHub (single commit): https://github.com/...
⏳ GitHub Actions will now trigger reviewer notification...
🧹 Local KT doc removed (committed to GitHub — no local copy needed)
✅ KT-Agent complete.
```

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `fetch failed` | Add `NODE_TLS_REJECT_UNAUTHORIZED=0` to `.env` |
| `GITHUB_TOKEN not set` | Check `.env` file exists and has correct values |
| `0 endpoints detected` | Ensure latest `generate-kt.js` is in place (check line 106) |
| `git push rejected` | Run `git pull origin main --rebase` then push |
| Issue not created | Check `GH_PAT` secret in GitHub Actions settings |
| Reviewer not notified | Check `KT_REVIEWER` variable in GitHub Actions settings |

---

## Making it generic

Drop `KT-Agent/` into any Spring Boot project root. Works with any project following standard package conventions (`controller/`, `service/`, `repository/`, `entity/`, `dto/`).
