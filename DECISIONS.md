# DECISIONS.md

A running log of key decisions, overridden proposals, and reasoning redirects during development sessions.

---

## [2026-07-28 19:48] - Documentation Setup: Root Directory Focus

**Proposal:** Store AGENTS.md, DECISIONS.md, and specs/ folder in `.copilot/session-state/` for session isolation.

**Issue:** 
- Session folder is temporary and ephemeral (specific to one session/checkpoint)
- Documentation and planning artifacts should persist in the project repository
- Team members and future sessions need access to decisions and plans
- Root directory is the canonical location for project governance files

**Decision:** 
- Moved AGENTS.md to project root (`/home/budulka/Documents/testJava/commafeed/`)
- Created `specs/` folder in project root for task plans
- Created DECISIONS.md in project root as the decision audit trail
- Session folder cleanup: removed temporary copies

**Rationale:** 
- Project root is the source of truth for governance, planning, and decision history
- Repository persistence ensures continuity across checkpoints and team handoffs
- DECISIONS.md in root is discoverable and maintains institutional memory

---

