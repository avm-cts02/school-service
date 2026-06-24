#!/usr/bin/env node
/**
 * KT-Agent: Knowledge Transfer Document Generator
 *
 * Compatible with:
 *   - VSCode + GitHub Copilot (via GitHub Actions trigger)
 *   - VSCode + Claude (via Claude Code or MCP)
 *
 * Usage:
 *   node KT-Agent/scripts/generate-kt.js [PROJECT_ROOT]
 *
 * Environment Variables:
 *   GITHUB_TOKEN       - Required for GitHub commit
 *   GITHUB_REPO        - owner/repo  e.g. "myorg/school-service"
 *   GITHUB_BRANCH      - Branch to commit to (default: main)
 *   ANTHROPIC_API_KEY  - OPTIONAL: enables AI-powered overview paragraph
 *                        If not set, a static summary is generated instead.
 *
 * NOTE: Reviewer notification is NOT handled here.
 * The GitHub Actions workflow (.github/workflows/kt-agent.yml) watches
 * KT-Agent/output/ for new commits and handles reviewer notification.
 */

const fs = require("fs");
const path = require("path");

// ─── CONFIG ──────────────────────────────────────────────────────────────────
const PROJECT_ROOT = process.argv[2] || path.resolve(__dirname, "../..");
const OUTPUT_DIR = path.join(PROJECT_ROOT, "KT-Agent", "output");
const TIMESTAMP = new Date().toISOString().replace(/[:.]/g, "-").slice(0, 19);
const OUTPUT_FILE = path.join(OUTPUT_DIR, `KT-Doc-${TIMESTAMP}.md`);

// ─── HELPERS ─────────────────────────────────────────────────────────────────
function readFileSafe(filePath) {
  try { return fs.readFileSync(filePath, "utf8"); } catch { return null; }
}

function walkDir(dir, exts = [".java", ".xml", ".properties", ".yml", ".yaml"], ignore = ["target", "node_modules", ".git", "output"]) {
  const results = [];
  if (!fs.existsSync(dir)) return results;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (ignore.includes(entry.name)) continue;
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) results.push(...walkDir(full, exts, ignore));
    else if (exts.some(e => entry.name.endsWith(e))) results.push(full);
  }
  return results;
}

function rel(p) { return path.relative(PROJECT_ROOT, p); }

// ─── SOURCE ANALYSIS ─────────────────────────────────────────────────────────
function analyzeProject() {
  const files = walkDir(PROJECT_ROOT);

  const layers = { controller: [], service: [], repository: [], entity: [], dto: [], exception: [], config: [], other: [] };
  const endpoints = [];
  let appName = "Spring Boot Microservice";
  let appVersion = "1.0.0";
  let javaVersion = "17";
  let dependencies = [];
  let dbInfo = "H2 (in-memory)";
  let serverPort = "8080";

  for (const f of files) {
    const content = readFileSafe(f) || "";
    const name = path.basename(f);
    const relPath = rel(f);

    if (f.endsWith("pom.xml") && !f.includes("/src/")) {
      const nameMatch = content.match(/<artifactId>([^<]+)<\/artifactId>/);
      const versionMatch = content.match(/<version>([^<]+)<\/version>/);
      const javaMatch = content.match(/<java\.version>([^<]+)<\/java\.version>/);
      if (nameMatch) appName = nameMatch[1];
      if (versionMatch) appVersion = versionMatch[1];
      if (javaMatch) javaVersion = javaMatch[1];
      const depMatches = [...content.matchAll(/<artifactId>(spring-boot-starter[^<]*)<\/artifactId>/g)];
      dependencies = depMatches.map(m => m[1]);
      continue;
    }

    if (f.endsWith("application.properties") || f.endsWith("application.yml")) {
      const portMatch = content.match(/server\.port\s*=\s*(\d+)/);
      const dbMatch = content.match(/spring\.datasource\.url\s*=\s*(.+)/);
      if (portMatch) serverPort = portMatch[1];
      if (dbMatch) dbInfo = dbMatch[1].trim();
      continue;
    }

    if (!name.endsWith(".java")) continue;

    const lower = relPath.toLowerCase();
    if (lower.includes("/controller/")) layers.controller.push({ name, relPath, content });
    else if (lower.includes("/service/")) layers.service.push({ name, relPath, content });
    else if (lower.includes("/repository/")) layers.repository.push({ name, relPath, content });
    else if (lower.includes("/entity/")) layers.entity.push({ name, relPath, content });
    else if (lower.includes("/dto/")) layers.dto.push({ name, relPath, content });
    else if (lower.includes("/exception/")) layers.exception.push({ name, relPath, content });
    else if (lower.includes("/config/")) layers.config.push({ name, relPath, content });
    else layers.other.push({ name, relPath, content });

    if (lower.includes("/controller/")) {
      const classMapping = (content.match(/@RequestMapping\(["']([^"']+)["']/) || [])[1] || "";
      // Line-by-line scan — handles @Operation/@ApiResponses between annotation and method
      const lines = content.split("\n");
      let pendingVerb = null;
      let pendingPath = "";
      for (let i = 0; i < lines.length; i++) {
        const trimmed = lines[i].trim();
        const mapMatch = trimmed.match(/^@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)(\(.*)?$/);
        if (mapMatch) {
          pendingVerb = mapMatch[1].replace("Mapping", "").toUpperCase();
          const block = lines.slice(i, i + 4).join(" ");
          const pathMatch = block.match(/["']([^"']*\/[^"']*)["']/);
          pendingPath = pathMatch ? pathMatch[1] : "";
          continue;
        }
        if (pendingVerb) {
          const methodMatch = trimmed.match(/public\s+\S+\s+(\w+)\s*\(/);
          if (methodMatch) {
            endpoints.push({ verb: pendingVerb, path: classMapping + pendingPath, method: methodMatch[1] });
            pendingVerb = null;
            pendingPath = "";
          }
          if (trimmed.startsWith("@") && !trimmed.startsWith("@ApiResponse") &&
            !trimmed.startsWith("@Operation") && !trimmed.startsWith("@Parameter") &&
            !trimmed.startsWith("@Valid")) {
            pendingVerb = null;
          }
        }
      }
    }
  }

  return { appName, appVersion, javaVersion, dependencies, dbInfo, serverPort, layers, endpoints };
}

// ─── STATIC SUMMARY (used when no ANTHROPIC_API_KEY) ─────────────────────────
function buildStaticSummary(analysis) {
  const { appName, layers, endpoints, dbInfo } = analysis;
  const entityNames = layers.entity.map(e => e.name.replace(".java", "")).join(", ") || "core entities";
  const totalFiles = Object.values(layers).flat().length;
  const deptList = layers.controller.map(c => c.name.replace(".java", "")).join(", ") || "controllers";

  return `This microservice (**${appName}**) is a Spring Boot REST API that manages ${entityNames} data for a school management system. It exposes **${endpoints.length} REST endpoints** across ${deptList}, backed by a **${dbInfo}** data store.

The codebase follows a clean layered architecture with ${totalFiles} Java source files organized into controllers, services, repositories, DTOs, and exception handlers. Business logic is encapsulated in the service layer with full transaction management, while the controller layer handles HTTP concerns including input validation and standardized response wrapping.

A global exception handler provides consistent error responses across all endpoints, covering validation failures, resource-not-found scenarios, duplicate data conflicts, and unexpected runtime errors.`;
}

// ─── AI SUMMARY (optional — only if ANTHROPIC_API_KEY is set) ────────────────
async function tryGetAISummary(analysis) {
  if (!process.env.ANTHROPIC_API_KEY) {
    console.log("ℹ️  ANTHROPIC_API_KEY not set — using static summary instead.");
    return buildStaticSummary(analysis);
  }

  console.log("🧠 ANTHROPIC_API_KEY found — generating AI-powered overview...");
  try {
    const { layers, endpoints, appName, dbInfo } = analysis;
    const snippet = [
      ...layers.controller.slice(0, 1),
      ...layers.service.slice(0, 1),
      ...layers.entity.slice(0, 1),
    ].map(f => `// File: ${f.name}\n${f.content.slice(0, 800)}`).join("\n\n---\n\n");

    const prompt = `You are a senior software architect performing a knowledge transfer session.
Analyze this Spring Boot microservice project called "${appName}" and write a concise 3-5 paragraph overview covering:
1. What the service does and its business purpose
2. Key entities and business rules
3. Notable design patterns or architectural decisions
4. Any important notes for a developer onboarding to this codebase

Database: ${dbInfo}
Endpoints detected: ${endpoints.length}
Layers: ${Object.entries(analysis.layers).filter(([, v]) => v.length > 0).map(([k, v]) => `${k}(${v.length})`).join(", ")}

Sample code snippets:
${snippet}

Write in a professional but approachable tone. Do NOT use bullet points — use flowing paragraphs.`;

    const res = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-api-key": process.env.ANTHROPIC_API_KEY,
        "anthropic-version": "2023-06-01"
      },
      body: JSON.stringify({
        model: "claude-sonnet-4-6",
        max_tokens: 800,
        messages: [{ role: "user", content: prompt }]
      })
    });

    if (!res.ok) throw new Error(`API responded with status ${res.status}`);
    const data = await res.json();
    console.log("✅ AI overview generated.");
    return data.content?.[0]?.text || buildStaticSummary(analysis);

  } catch (e) {
    console.warn(`⚠️  AI summary failed (${e.message}) — falling back to static summary.`);
    return buildStaticSummary(analysis);
  }
}

// ─── DOCUMENT BUILDER ────────────────────────────────────────────────────────
async function buildDocument(analysis) {
  const { appName, appVersion, javaVersion, dependencies, dbInfo, serverPort, layers, endpoints } = analysis;

  const now = new Date();
  const readableDate = now.toLocaleString("en-IN", { timeZone: "Asia/Kolkata", dateStyle: "full", timeStyle: "long" });
  const overviewText = await tryGetAISummary(analysis);

  const entitySchemas = layers.entity.map(e => {
    const fields = [...e.content.matchAll(/private\s+([\w<>]+)\s+(\w+);/g)]
      .map(m => `| \`${m[2]}\` | \`${m[1]}\` |`)
      .join("\n");
    return `### ${e.name.replace(".java", "")}\n| Field | Type |\n|-------|------|\n${fields}`;
  }).join("\n\n");

  const endpointRows = endpoints.length
    ? endpoints.map(e => `| \`${e.verb}\` | \`${e.path}\` | \`${e.method}\` |`).join("\n")
    : "| - | No endpoints detected automatically | - |";

  const layerTable = Object.entries(layers)
    .filter(([, v]) => v.length > 0)
    .map(([k, v]) => `| **${k.charAt(0).toUpperCase() + k.slice(1)}** | ${v.map(f => `\`${f.name}\``).join(", ")} |`)
    .join("\n");

  const exceptionList = layers.exception.map(e => `- \`${e.name}\``).join("\n") || "- None detected";
  const depList = dependencies.map(d => `- \`${d}\``).join("\n") || "- See pom.xml";

  return `# 📘 Knowledge Transfer Document
## Project: \`${appName}\`

> **Generated:** ${readableDate}
> **Version:** ${appVersion} | **Java:** ${javaVersion} | **Port:** ${serverPort}
> **Generator:** KT-Agent

---

## 📋 Table of Contents
1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Layer Structure](#3-layer-structure)
4. [Data Models](#4-data-models)
5. [REST API Reference](#5-rest-api-reference)
6. [Exception Handling](#6-exception-handling)
7. [Configuration & Dependencies](#7-configuration--dependencies)
8. [Running the Project](#8-running-the-project)

---

## 1. Project Overview

${overviewText}

| Property | Value |
|----------|-------|
| Artifact ID | \`${appName}\` |
| Version | \`${appVersion}\` |
| Java Version | \`${javaVersion}\` |
| Server Port | \`${serverPort}\` |
| Database | \`${dbInfo}\` |
| Total Java Files | \`${Object.values(layers).flat().length}\` |
| Total REST Endpoints | \`${endpoints.length}\` |

---

## 2. Architecture

\`\`\`
┌─────────────────────────────────────────────────┐
│                   CLIENT / UI                    │
└───────────────────────┬─────────────────────────┘
                        │ HTTP REST
┌───────────────────────▼─────────────────────────┐
│              CONTROLLER LAYER                    │
│  • Handles HTTP requests/responses               │
│  • Input validation (@Valid)                     │
│  • Returns ApiResponseDTO<T> wrappers            │
│  Files: ${(layers.controller.map(f => f.name).join(", ")) || "none"}
└───────────────────────┬─────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────┐
│               SERVICE LAYER                      │
│  • Business logic                                │
│  • Transaction management (@Transactional)       │
│  • Orchestrates Repository + Mapper              │
│  Files: ${(layers.service.map(f => f.name).join(", ")) || "none"}
└───────────────────────┬─────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────┐
│              REPOSITORY LAYER                    │
│  • Data access via Spring Data JPA               │
│  • Custom JPQL queries                           │
│  Files: ${(layers.repository.map(f => f.name).join(", ")) || "none"}
└───────────────────────┬─────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────┐
│               DATABASE LAYER                     │
│  ${dbInfo}
└─────────────────────────────────────────────────┘

Cross-Cutting Concerns:
  ├── DTOs       : ${layers.dto.map(f => f.name).join(", ") || "none"}
  ├── Exceptions : ${layers.exception.map(f => f.name).join(", ") || "none"}
  └── Config     : ${layers.config.map(f => f.name).join(", ") || "none"}
\`\`\`

---

## 3. Layer Structure

| Layer | Files |
|-------|-------|
${layerTable}

### File Tree (src/main)
\`\`\`
src/main/java/
${Object.entries(layers)
      .filter(([, v]) => v.length > 0)
      .map(([k, v]) => `  ├── ${k}/\n${v.map(f => `  │   └── ${f.name}`).join("\n")}`)
      .join("\n")}
\`\`\`

---

## 4. Data Models

${entitySchemas || "_No entity classes detected._"}

---

## 5. REST API Reference

**Base URL:** \`http://localhost:${serverPort}\`
**Swagger UI:** \`http://localhost:${serverPort}/swagger-ui.html\`
**API Docs:** \`http://localhost:${serverPort}/api-docs\`

| Method | Endpoint | Handler |
|--------|----------|---------|
${endpointRows}

### Request / Response Pattern

**Success Response:**
\`\`\`json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2024-01-01T10:00:00"
}
\`\`\`

**Error Response:**
\`\`\`json
{
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found with id: 1",
  "path": "/api/v1/resource/1",
  "timestamp": "2024-01-01T10:00:00"
}
\`\`\`

**Validation Error Response:**
\`\`\`json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed.",
  "validationErrors": {
    "fieldName": "must not be blank"
  },
  "timestamp": "2024-01-01T10:00:00"
}
\`\`\`

---

## 6. Exception Handling

**Global Exception Handler:** \`GlobalExceptionHandler\` (\`@RestControllerAdvice\`)

${exceptionList}

| Exception | HTTP Status | Trigger |
|-----------|-------------|---------|
| \`TeacherNotFoundException\` | 404 Not Found | Resource ID does not exist |
| \`DuplicateEmailException\` | 409 Conflict | Email already registered |
| \`MethodArgumentNotValidException\` | 400 Bad Request | Bean validation failure |
| \`MethodArgumentTypeMismatchException\` | 400 Bad Request | Path/param type mismatch |
| \`Exception\` (fallback) | 500 Internal Server Error | Unhandled exceptions |

---

## 7. Configuration & Dependencies

### Spring Boot Starters
${depList}

### Key Endpoints (Actuator)
- \`GET /actuator/health\` — Service health check
- \`GET /actuator/info\` — App info
- \`GET /actuator/metrics\` — Metrics

### H2 Console (dev only)
URL: \`http://localhost:${serverPort}/h2-console\`
JDBC URL: \`${dbInfo}\`

---

## 8. Running the Project

\`\`\`bash
# Clone
git clone <repo-url>
cd ${appName}

# Build
mvn clean install

# Run
mvn spring-boot:run
# OR
java -jar target/${appName}-${appVersion}.jar
\`\`\`

**Health Check:**
\`\`\`bash
curl http://localhost:${serverPort}/actuator/health
\`\`\`

---

_This document was auto-generated by **KT-Agent** on ${readableDate}._
_To regenerate: \`node KT-Agent/scripts/generate-kt.js\`_
`;
}

// ─── GITHUB COMMIT ────────────────────────────────────────────────────────────
async function commitToGitHub(filePath, content) {
  const token = process.env.GITHUB_TOKEN;
  const repo = process.env.GITHUB_REPO;
  const branch = process.env.GITHUB_BRANCH || "main";

  if (!token || !repo) {
    console.log("ℹ️  GITHUB_TOKEN or GITHUB_REPO not set — skipping GitHub commit.");
    return null;
  }

  const repoPath = `KT-Agent/output/${path.basename(filePath)}`;
  const encoded = Buffer.from(content).toString("base64");

  let sha;
  try {
    const getRes = await fetch(`https://api.github.com/repos/${repo}/contents/${repoPath}?ref=${branch}`, {
      headers: { Authorization: `Bearer ${token}`, Accept: "application/vnd.github+json" }
    });
    if (getRes.ok) sha = (await getRes.json()).sha;
  } catch { }

  const putRes = await fetch(`https://api.github.com/repos/${repo}/contents/${repoPath}`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json", Accept: "application/vnd.github+json" },
    body: JSON.stringify({
      message: `docs(kt-agent): add KT document ${path.basename(filePath)}\n\nAuto-generated by KT-Agent at ${new Date().toISOString()}`,
      content: encoded,
      branch,
      ...(sha ? { sha } : {})
    })
  });

  if (!putRes.ok) throw new Error(`GitHub commit failed: ${putRes.status} — ${await putRes.text()}`);

  const result = await putRes.json();
  console.log(`✅ Committed to GitHub: ${result.content.html_url}`);
  console.log(`⏳ GitHub Actions will now trigger reviewer notification...`);
  return result.content.html_url;
}

// ─── MAIN ─────────────────────────────────────────────────────────────────────
async function main() {
  console.log("🤖 KT-Agent starting...");
  console.log(`📁 Analyzing project at: ${PROJECT_ROOT}`);

  const analysis = analyzeProject();
  console.log(`✅ Found: ${Object.values(analysis.layers).flat().length} Java files, ${analysis.endpoints.length} endpoints`);

  const document = await buildDocument(analysis);

  if (!fs.existsSync(OUTPUT_DIR)) fs.mkdirSync(OUTPUT_DIR, { recursive: true });
  fs.writeFileSync(OUTPUT_FILE, document, "utf8");
  console.log(`📄 KT document saved: ${OUTPUT_FILE}`);

  const githubUrl = await commitToGitHub(OUTPUT_FILE, document);
  if (githubUrl) console.log(`🔗 View on GitHub: ${githubUrl}`);

  console.log("✅ KT-Agent complete.");
}

main().catch(err => {
  console.error("❌ KT-Agent failed:", err.message);
  process.exit(1);
});