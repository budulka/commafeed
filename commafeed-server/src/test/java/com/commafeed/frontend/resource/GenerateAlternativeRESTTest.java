package com.commafeed.frontend.resource;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.LLMService;
import com.commafeed.frontend.model.request.GenerateAlternativeRequest;
import com.commafeed.security.AuthenticationContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GenerateAlternativeRESTTest {

    @Test
    public void testSuccess() throws Exception {
        AuthenticationContext auth = Mockito.mock(AuthenticationContext.class);
        FeedEntryService feedEntryService = Mockito.mock(FeedEntryService.class);
        LLMService llmService = Mockito.mock(LLMService.class);

        FeedEntry entry = new FeedEntry();
        entry.setId(123L);
        FeedEntryContent content = new FeedEntryContent();
        content.setTitle("Original Title");
        content.setContent("Original content body");
        entry.setContent(content);

        Mockito.when(feedEntryService.getById(123L)).thenReturn(entry);
        Mockito.when(llmService.generateAlternative(Mockito.anyString(), Mockito.anyString()))
                .thenReturn("Rewritten text");

        GenerateAlternativeREST rest =
                new GenerateAlternativeREST(auth, feedEntryService, llmService);

        GenerateAlternativeRequest req = new GenerateAlternativeRequest();
        req.setTarget("title");
        req.setPrompt("Rewrite for technical audience");

        Response resp = rest.generateAlternative(123L, req);
        Assertions.assertEquals(200, resp.getStatus());
        Object entity = resp.getEntity();
        Assertions.assertNotNull(entity);
        // basic checks on the response object's fields
        Assertions.assertTrue(
                entity.toString().contains("Rewritten text")
                        || entity.toString().contains("alternative"));
    }

    @Test
    public void testBadTarget() {
        AuthenticationContext auth = Mockito.mock(AuthenticationContext.class);
        FeedEntryService feedEntryService = Mockito.mock(FeedEntryService.class);
        LLMService llmService = Mockito.mock(LLMService.class);

        GenerateAlternativeREST rest =
                new GenerateAlternativeREST(auth, feedEntryService, llmService);

        GenerateAlternativeRequest req = new GenerateAlternativeRequest();
        req.setTarget("invalid");
        req.setPrompt("p");

        Response resp = rest.generateAlternative(1L, req);
        Assertions.assertEquals(400, resp.getStatus());
    }

    @Test
    public void testNotFound() {
        AuthenticationContext auth = Mockito.mock(AuthenticationContext.class);
        FeedEntryService feedEntryService = Mockito.mock(FeedEntryService.class);
        LLMService llmService = Mockito.mock(LLMService.class);

        Mockito.when(feedEntryService.getById(999L)).thenReturn(null);

        GenerateAlternativeREST rest =
                new GenerateAlternativeREST(auth, feedEntryService, llmService);

        GenerateAlternativeRequest req = new GenerateAlternativeRequest();
        req.setTarget("content");
        req.setPrompt("p");

        Response resp = rest.generateAlternative(999L, req);
        Assertions.assertEquals(404, resp.getStatus());
    }

    @Test
    public void testLLMFailure() throws Exception {
        AuthenticationContext auth = Mockito.mock(AuthenticationContext.class);
        FeedEntryService feedEntryService = Mockito.mock(FeedEntryService.class);
        LLMService llmService = Mockito.mock(LLMService.class);

        FeedEntry entry = new FeedEntry();
        entry.setId(5L);
        FeedEntryContent content = new FeedEntryContent();
        content.setTitle("t");
        content.setContent("c");
        entry.setContent(content);

        Mockito.when(feedEntryService.getById(5L)).thenReturn(entry);
        Mockito.when(llmService.generateAlternative(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new LLMService.LLMException("fail"));

        GenerateAlternativeREST rest =
                new GenerateAlternativeREST(auth, feedEntryService, llmService);
        GenerateAlternativeRequest req = new GenerateAlternativeRequest();
        req.setTarget("content");
        req.setPrompt("p");

        Response resp = rest.generateAlternative(5L, req);
        Assertions.assertEquals(502, resp.getStatus());
    }
}
