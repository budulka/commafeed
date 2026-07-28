# AGENTS.md - Project Guidelines

This document describes the project characteristics, architecture, key modules, and instructions for agents/developers.

## Project Overview
The "commafeed" project is a web application for reading news feeds/streams (RSS/Atom). It includes a Java backend (Maven) and a frontend. The goal is to collect, aggregate, and display feeds from user subscriptions.

## Technologies
- Language: Java
- Build System: Maven (pom.xml)
- Database: (see configuration in the project)
- Web: Servlets/Framework (details in code)

## Main Modules
- Core/Backend: handling subscriptions, feed parsing, update logic.
- Data: models and storage access (repositories, DAOs).
- API: REST/HTTP endpoints for the client.
- UI: static resources/frontend (if present).

## Features
- Periodic feed updates: scheduler/thread pool for polling sources.
- Parsing various feed formats (RSS, Atom).
- Caching and duplicate handling.
- User settings (filters, tags).

## Deployment and Run
1. Ensure JDK and Maven are installed.
2. Build the project: `mvn clean package`.
3. Configure database connection (configuration file in resources).
4. Run the application (e.g., via embedded server or container deployment).

## For Agents/Developers
- API requests are documented in the corresponding controllers.
- To add a new feed parser: implement the parser interface and register it in the parser factory/update service.
- Logs: use the existing logging configuration for diagnostics.

---

## Conventional Commits

All commits must follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

**Format:** `<type>(<scope>): <subject>`

**Types:**
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes (formatting, missing semicolons, etc.)
- `refactor:` Code refactoring without feature/fix
- `perf:` Performance improvements
- `test:` Test additions or modifications
- `chore:` Build, dependency, or tooling changes
- `ci:` CI/CD configuration changes

**Examples:**
```
feat(feed-parser): add support for JSON Feed format
fix(api): handle null feed titles correctly
docs(readme): update deployment instructions
chore(deps): upgrade Maven dependencies
```

**Breaking changes:** Add `BREAKING CHANGE:` footer or append `!` before colon:
```
feat(api)!: redesign subscription endpoint response
```

---

## 2. Planning Requirements

**Before starting work on any task:**
- Create a task plan document in the `specs/` folder
- Format: `specs/<task-name>.md` (kebab-case task name)
- The plan should include:
  - Problem statement / Task description
  - Approach / Solution strategy
  - Key files to modify
  - Acceptance criteria / Success metrics
  - Potential risks or edge cases

**Plan discipline:**
- Review the plan with the user before implementation
- Update the plan as discovery progresses
- Keep plans concise and actionable

---

## 3. Decision Logging

**After each user interaction regarding a plan:**
- Record decisions in `DECISIONS.md` (in the project root)
- Log moments where:
  - AI proposed something that was overridden or corrected
  - An alternative approach was chosen
  - Implementation direction was redirected
  - Key trade-offs were made

**Format for DECISIONS.md:**
```
## [Date/Timestamp] - [Task/Context]

**Proposal:** [What the AI suggested]
**Issue:** [Why it was wrong, risky, or suboptimal]
**Decision:** [What was chosen instead]
**Rationale:** [Why this was better]
```

Keep 3-7 entries. This is the most valuable artifact for tracking session reasoning.

---

## Additional Resources
- README.md — main documentation (see repository root).
- pom.xml — dependency and plugin information.

> This file is maintained manually. Update with new project details or deployment instructions as needed.
