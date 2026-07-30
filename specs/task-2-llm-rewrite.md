Task: LLM 'rewrite this entry' endpoint (Level 2)

Problem statement
- Add an endpoint that takes an existing feed entry by ID and uses an LLM to produce an alternative version of its title or content. The endpoint must accept a target ("title" or "content") and a free-text prompt (instruction), return the original entry and the generated alternative, and surface missing-entry and LLM failures with correct HTTP status codes and no stack traces.

Scope
- Implement only the plan after user approval. Create/update the REST endpoint, DTOs, a small LLM client service, and unit/integration tests.
- Require explicit LLM provider selection via an LLM_PROVIDER environment variable (e.g., "groq"). Operators must configure provider-specific settings such as LLM_API_URL and LLM_API_KEY in the environment or external config. Do NOT commit secrets; read keys from env/config at runtime.
- Follow the project's DTO and exception-mapping style: reuse existing request/response DTO conventions and rely on the centralized exception-to-HTTP mapping for surface errors.

Provider details (Groq)
- When LLM_PROVIDER is set to "groq", LLM_API_URL must point to the Groq generate endpoint (operator responsibility). The LLMService implementation will send a JSON payload with the combined prompt and source and authenticate with LLM_API_KEY via Bearer Authorization header. The service will not log or persist API keys.


Approach / Solution strategy
1. API Design
   - POST /rest/entry/{id}/generate-alternative
   - Request body: { "target": "title"|"content", "prompt": "..." }
   - Response: { original: { id, title, content }, target, prompt, alternative }
   - Errors: 400 for invalid target; 404 if entry not found; 502 if LLM call fails.

2. Implementation plan
   - Add request DTO: GenerateAlternativeRequest (target, prompt)
   - Add response DTO: GenerateAlternativeResponse (original, target, prompt, alternative)
   - Add REST resource GenerateAlternativeREST under /rest/entry/{id}/generate-alternative
     - Validate inputs, load entry via FeedEntryService.getById(id)
     - Extract source text depending on target
     - Call LLMService.generateAlternative(source, prompt)
     - Return structured response
   - Add LLMService: provider-abstracted client that supports LLM_PROVIDER (e.g., 'groq'). Implement a provider for Groq that sends prompt+source to the configured LLM_API_URL and authenticates with LLM_API_KEY supplied via env. Do not hardcode keys or include them in code or plan. Implement sensible HTTP timeouts and retries where appropriate, and throw a checked LLMException on non-2xx responses or network failures.
   - Error mapping: rely on the project's centralized exception-mapping to convert LLMException into an HTTP 502 with a minimal JSON error message; entry null -> 404; bad target -> 400.

3. Tests
   - Unit tests mocking LLMService to verify response shape and error handling
   - Integration test: create an entry and call the endpoint; mock LLM via local test HTTP server or environment variable.

Key files to modify / create
- specs/task-2-llm-rewrite.md (this plan)
- backend/service/LLMService.java (new)
- frontend/resource/GenerateAlternativeREST.java (new)
- frontend/model/request/GenerateAlternativeRequest.java (new)
- frontend/model/GenerateAlternativeResponse.java (new)
- backend/service/FeedEntryService.java (add getById if missing)
- tests under server/src/test/java/... to cover success and LLM failure

Acceptance criteria
- Endpoint exists and matches the API design
- Returns 404 for nonexistent entries, 400 for invalid target, 502 for LLM failures
- No stack traces or internal exceptions sent to client (use centralized exception mapping)
- LLM client requires LLM_PROVIDER and reads provider settings from env (LLM_API_URL, LLM_API_KEY). Do not hardcode secrets.
- Tests covering success and failure paths and provider-specific behavior (mock Groq provider)

Risks and mitigations
- LLM latency/timeouts: set sensible HTTP timeouts and return 502 on timeout
- Cost/abuse: require operator to configure an external LLM; include fallback for local dev
- Sensitive data leakage: do not log API keys or full LLM responses; only surface minimal errors

Rollout notes
- Feature enabled immediately when deployed; operator controls LLM backend via env vars

Estimate
- Plan review: now
- Implementation & unit tests: 1–2 hours
- Integration testing and docs: +1 hour

Next action
- Wait for user approval. After approval, proceed to implement per this plan (or revert any prior changes if you prefer).