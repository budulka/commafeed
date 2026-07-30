package com.commafeed.frontend.resource;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.LLMService;
import com.commafeed.frontend.model.GenerateAlternativeResponse;
import com.commafeed.frontend.model.request.GenerateAlternativeRequest;
import com.commafeed.backend.model.User;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import com.google.common.base.Preconditions;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/rest/entry/{id}/generate-alternative")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
@Tag(name = "Feed entries")
public class GenerateAlternativeREST {

    private final AuthenticationContext authenticationContext;
    private final FeedEntryService feedEntryService;
    private final LLMService llmService;

    @POST
    @Transactional
    @Operation(summary = "Generate alternative for entry title or content", description = "Rewrite or rephrase an entry's title or content using an LLM")
    public Response generateAlternative(@PathParam("id") Long id, @Valid GenerateAlternativeRequest req) {
        Preconditions.checkNotNull(req);
        Preconditions.checkNotNull(req.getTarget());
        Preconditions.checkNotNull(req.getPrompt());

        if (!"title".equals(req.getTarget()) && !"content".equals(req.getTarget())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "target must be 'title' or 'content'"))
                    .build();
        }

        User user = authenticationContext.getCurrentUser();
        FeedEntry entry = feedEntryService.getById(id);
        if (entry == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "entry not found")).build();
        }

        FeedEntryContent content = entry.getContent();
        String source = "title".equals(req.getTarget()) ? (content == null ? "" : content.getTitle()) : (content == null ? "" : content.getContent());

        try {
            String alt = llmService.generateAlternative(source, req.getPrompt());

            GenerateAlternativeResponse resp = new GenerateAlternativeResponse();
            GenerateAlternativeResponse.OriginalEntry original =
                    new GenerateAlternativeResponse.OriginalEntry(String.valueOf(entry.getId()), content == null ? null : content.getTitle(), content == null ? null : content.getContent());
            resp.setOriginal(original);
            resp.setTarget(req.getTarget());
            resp.setPrompt(req.getPrompt());
            resp.setAlternative(alt);

            return Response.ok(resp).build();
        } catch (LLMService.LLMException e) {
            return Response.status(Response.Status.BAD_GATEWAY).entity(Map.of("error", "LLM service failed")).build();
        }
    }
}
