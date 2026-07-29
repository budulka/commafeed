# Saved Entry Notes - Implementation Plan

## Problem Statement
Add a new "Saved Entry Notes" capability to CommaFeed alongside existing feeds and feed entries. Users should be able to attach short notes (comment text + star rating) to feed entries they've read, creating a vertical slice following the existing REST → Service → DAO → Entity architecture.

## Approach / Solution Strategy

We will replicate the existing `FeedEntryTag` vertical slice pattern but adapt it for notes with the following structure:

### Architecture Layers

#### 1. **JPA Entity** (`FeedEntryNote.java`)
- Extend `AbstractModel` (provides auto-incrementing ID via `@TableGenerator`)
- Relationships:
  - `@ManyToOne` to `User` (who created the note)
  - `@ManyToOne` to `FeedEntry` (what entry the note is about)
- Fields:
  - `user: User` (owner of the note)
  - `entry: FeedEntry` (linked entry)
  - `text: String` (max 500 chars - short comment)
  - `rating: Integer` (0-5 stars, optional/nullable)
  - `created: Instant` (when note was created)
  - `updated: Instant` (when note was last modified)
- Table: `FEEDENTRYNOTES`

#### 2. **DAO** (`FeedEntryNoteDAO.java`)
- Extend `GenericDAO<FeedEntryNote>`
- Uses QueryDSL (via `QFeedEntryNote` generated class)
- Methods:
  - `findByEntry(User user, FeedEntry entry)` → FeedEntryNote or null
  - `findByUser(User user)` → List<FeedEntryNote> (all notes for user)
  - `findByUser(User user, int offset, int limit)` → paginated results

#### 3. **Service** (`FeedEntryNoteService.java`)
- Business logic for managing notes
- Injected: `FeedEntryDAO`, `FeedEntryNoteDAO`, `UserDAO`
- Methods:
  - `createOrUpdate(User user, Long entryId, String text, Integer rating)` → FeedEntryNote
    - Validate: entryId exists, text not empty/max 500 chars, rating 0-5 if provided
    - Create new note if doesn't exist; update if exists
  - `deleteNote(User user, Long entryId)` → void
  - `findUserNote(User user, Long entryId)` → FeedEntryNote or null

#### 4. **REST Endpoints** (`NoteREST.java`)
- Path: `/rest/note`
- Secured: `@RolesAllowed(Roles.USER)`, uses `AuthenticationContext`
- Endpoints:

  **POST `/rest/note`** - Create/attach a note to an entry
  - Request: `NoteRequest { entryId: Long, text: String, rating: Integer? }`
  - Validation: `@Valid`, `@NotNull entryId`, `@Size(max=500) text`
  - Behavior: Validates input and creates or attaches the note to the specified entry for the current user
  - Response: `Response.status(Status.CREATED).entity(noteDTO).build()` on creation, or `Response.ok(noteDTO).build()` on update
  - Errors: 400 BAD_REQUEST for validation; 404 NOT_FOUND if entry doesn't exist

  **GET `/rest/note`** - List current user's notes
  - Query params: `offset` (default 0), `limit` (default 50)
  - Response: `Response.ok(List<NoteDTO>).build()`
  - Returns: notes owned by the current user ordered by created DESC with associated entry metadata


#### 5. **DTOs**
- `NoteRequest { entryId: Long, text: String, rating: Integer? }`
- `NoteResponse { id: Long, entryId: Long, text: String, rating: Integer?, created: Instant, updated: Instant }`

#### 6. **Exception Handling**
- Use existing `ValidationException` for validation errors
- Controller catches and returns appropriate status codes (400, 404, 500)
- No new exception types needed; leverage existing mappers

## Key Files to Modify

### Create:
```
commafeed-server/src/main/java/com/commafeed/backend/model/FeedEntryNote.java
commafeed-server/src/main/java/com/commafeed/backend/dao/FeedEntryNoteDAO.java
commafeed-server/src/main/java/com/commafeed/backend/service/FeedEntryNoteService.java
commafeed-server/src/main/java/com/commafeed/backend/rest/resources/NoteREST.java
commafeed-server/src/main/java/com/commafeed/backend/rest/request/NoteRequest.java
commafeed-server/src/main/java/com/commafeed/backend/rest/response/NoteResponse.java
```

### May need to modify:
- `Entry.java` (response DTO) - add `note` field so clients can retrieve note data with entry
- Existing Entry fetch methods (optional enhancement)

## Acceptance Criteria

1. ✅ **Entity**: `FeedEntryNote` extends `AbstractModel`, has proper relationships and table mapping
2. ✅ **DAO**: `FeedEntryNoteDAO` extends `GenericDAO`, uses QueryDSL, implements find methods
3. ✅ **Service**: `FeedEntryNoteService` handles creation/attachment and listing with validation
4. ✅ **POST /rest/note**: Creates/attaches note to an entry, returns 201 Created on new note or 200 OK on update, validates input
5. ✅ **GET /rest/note**: Lists current user's notes with pagination, returns 200 OK with List<NoteDTO>
6. ✅ **Proper status codes**: 400 (validation), 401 (auth), 404 (not found), 200/201 (success)
7. ✅ **Reuses existing patterns**: DTOs with `@Schema`, validation with `@Valid`/`@NotNull`/`@Size`, transactions with `@Transactional`
8. ✅ **Database layer**: QueryDSL queries follow existing patterns (QFeedEntryNote)

## Potential Risks & Edge Cases

1. **Concurrency**: Two users simultaneously creating/updating notes on same entry
   - *Mitigation*: Database constraints + optimistic locking (if needed) in service
   
2. **Entry deletion**: What happens to notes when entry is deleted?
   - *Strategy*: Add `@OneToMany(cascade = CascadeType.REMOVE)` on FeedEntry → notes
   - *Or*: Soft delete notes, hide from UI
   
3. **Stale rating/text**: User updates note but old data cached on client
   - *Mitigation*: Return updated timestamp, client can refresh
   
4. **Large note text**: Need to validate max length before persist
   - *Solution*: `@Size(max = 500)` on DTO + service-level validation
   
5. **QueryDSL code generation**: Need to ensure `QFeedEntryNote` is generated
   - *Verification*: Run `mvn clean compile` to trigger APT processor

## Implementation Order

1. Create `FeedEntryNote.java` entity
2. Create `FeedEntryNoteDAO.java` DAO
3. Create `FeedEntryNoteService.java` service with creation and listing logic
4. Create DTOs: `NoteRequest.java`, `NoteResponse.java`
5. Create `NoteREST.java` controller with POST (create/attach) and GET (list user's notes) endpoints
6. Test endpoints manually (curl, Postman)
7. Verify database schema changes
8. (Optional) Update `Entry.java` to include note data

## Success Metrics

- Both required REST endpoints functional (POST to create/attach, GET to list current user's notes)
- Proper validation and error handling for inputs and missing entries
- Database queries follow QueryDSL patterns
- Code matches existing style and architecture
- Manual testing confirms create/attach and listing work end-to-end
