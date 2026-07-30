## My AI Workflow

For this project, I used a combination of AI agents:
- **GitHub Copilot** was used for **Level 1 (Saved Entry Notes)** to quickly scaffold the vertical slice (Entity, DAO, Service, REST) following existing patterns.
- **Junie** (powered by Gemini-3-Flash) was used for **Level 2 (LLM 'Rewrite This Entry')** to handle the more complex integration with external APIs, error mapping, and architectural refinements.

My workflow followed these principles:

1. **Plan-First Approach**: Before writing any code, I generated and refined implementation plans in the `specs/` folder. This ensured that both the AI and the user were aligned on the architectural direction.
2. **Context Management**: Instead of pasting the whole codebase, I used tools to explore the project structure and selectively read relevant files (Service, DAO, Model, Resource) to build a mental map of the project's patterns.
3. **Architectural Alignment**: I strictly followed CommaFeed's layered architecture (REST -> Service -> DAO -> JPA Entity). When the AI suggested bypassing a layer (e.g., calling DAO directly from REST), I intervened to maintain consistency.
4. **Iterative Refinement**: I used unit tests to verify each vertical slice immediately after implementation. This caught compilation errors and logic bugs early.
5. **Decision Logging**: I maintained `DECISIONS.md` to track where I had to override or redirect the AI's proposals, providing a clear audit trail of the development process.

## New Endpoints - Samples

### Level 1: Saved Entry Notes

**Create or update a note:**
```bash
curl -u admin:admin -X POST http://localhost:8083/rest/note \
  -H "Content-Type: application/json" \
  -d '{"entryId": 123, "text": "This is a great article!", "rating": 5}'
```
Response: `201 Created` or `200 OK` with JSON:
```json
{
  "id": 1,
  "entryId": 123,
  "text": "This is a great article!",
  "rating": 5,
  "created": "2026-07-30T13:00:00Z",
  "updated": "2026-07-30T13:00:00Z"
}
```

**List user notes:**
```bash
curl -u admin:admin "http://localhost:8083/rest/note?limit=10"
```
Response: `200 OK` with a list of notes.

### Level 2: LLM 'Rewrite This Entry'

**Generate alternative title:**
```bash
curl -u admin:admin -X POST http://localhost:8083/rest/entry/123/generate-alternative \
  -H "Content-Type: application/json" \
  -d '{"target": "title", "prompt": "Rewrite this headline to be more professional"}'
```
Response: `200 OK` with JSON:
```json
{
  "original": {
    "id": "123",
    "title": "Old Title",
    "content": "..."
  },
  "target": "title",
  "prompt": "Rewrite this headline to be more professional",
  "alternative": "New Professional Title"
}
```

## Translation

Files for internationalization are
located [here](https://github.com/Athou/commafeed/tree/master/commafeed-client/src/locales).

To add a new language:

- add the new locale to the `locales` array in:
    - `commafeed-client/.linguirc`
    - `commafeed-client/src/i18n.ts`
- run `npm run i18n:extract`
- add translations to the newly created `commafeed-client/src/locales/[locale]/messages.po` file

The name of the locale should be the
two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).

## Local development

### Backend

- Open `commafeed-server` in your preferred Java IDE.
    - CommaFeed uses Lombok, you need the Lombok plugin for your IDE.
- run `./mvnw quarkus:dev`

### Frontend

- Open `commafeed-client` in your preferred JavaScript IDE.
- run `npm install`
- run `npm run dev`

The frontend server is now running at http://localhost:8082 and is proxying REST requests to the backend running on
port 8083
<img width="1838" height="499" alt="image" src="https://github.com/user-attachments/assets/1e336aa4-4318-43ac-8bda-74ef526d9727" />


