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

## [2026-07-29 11:16] - Saved-entry-notes plan adjusted to task requirements

**Proposal:** Implement four endpoints (POST, GET list, GET by entry, DELETE) in the saved-entry-notes plan.

**Issue:** The task required only two endpoints: POST to create/attach a note and GET to list the current user's notes. Adding extra endpoints deviated from the user's specification and risked scope creep.

**Decision:** Updated the saved-entry-notes plan to include only the two required endpoints: POST `/rest/note` (create/attach) and GET `/rest/note` (list current user's notes). Removed GET by entry and DELETE from the plan.

**Rationale:** Follow the user's explicit requirements to avoid scope creep and implement minimal, required functionality. This keeps the implementation aligned with the task and prevents unnecessary extra work.

---

## [2026-07-30 13:45] - LLM Rewrite Implementation

**Proposal:** Call `FeedEntryDAO.findById(id)` directly in `GenerateAlternativeREST` to retrieve the entry.

**Issue:** Calling the DAO directly bypasses the service layer, which is inconsistent with the project's architecture where REST resources typically interact with services.

**Decision:** Added `getById(Long id)` to `FeedEntryService` which delegates to the DAO.

**Rationale:** Maintains architectural consistency and separation of concerns by ensuring all business logic and data access for feed entries go through `FeedEntryService`.

---

