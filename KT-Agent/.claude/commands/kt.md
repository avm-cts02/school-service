# /kt — Generate KT Document

Triggers the KT-Agent to analyze the current Spring Boot project and generate a Knowledge Transfer document.

## What this does
1. Scans all Java source files and classifies them by layer
2. Extracts REST endpoints, entity schemas, exception handlers
3. Generates a timestamped Markdown KT document
4. Commits the document to GitHub (single atomic commit)
5. Triggers GitHub Actions → reviewer is notified via issue

## Usage
Just type `/kt` in Claude Code and press Enter.

## Prerequisites
- `KT-Agent/.env` must exist with GITHUB_TOKEN and GITHUB_REPO set
- Node.js 18+ must be installed

## Expected output
```
✅ Found: 18 Java files, 10 endpoints
📄 KT document saved
✅ Committed to GitHub (single commit)
⏳ GitHub Actions will trigger reviewer notification
🧹 Local copy removed
✅ KT-Agent complete
```
