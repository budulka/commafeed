package com.commafeed.backend.rest.resources;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryNoteDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.User;
import com.commafeed.backend.rest.request.NoteRequest;
import com.commafeed.backend.rest.response.NoteResponse;
import com.commafeed.backend.service.FeedEntryNoteService;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/rest/note")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class NoteREST {

    private final AuthenticationContext authenticationContext;
    private final FeedEntryNoteDAO feedEntryNoteDAO;
    private final FeedEntryDAO feedEntryDAO;
    private final FeedEntryNoteService feedEntryNoteService;
    private final FeedEntryService feedEntryService;

    public NoteREST(
            AuthenticationContext authenticationContext,
            FeedEntryNoteDAO feedEntryNoteDAO,
            FeedEntryDAO feedEntryDAO,
            FeedEntryNoteService feedEntryNoteService,
            FeedEntryService feedEntryService) {
        this.authenticationContext = authenticationContext;
        this.feedEntryNoteDAO = feedEntryNoteDAO;
        this.feedEntryDAO = feedEntryDAO;
        this.feedEntryNoteService = feedEntryNoteService;
        this.feedEntryService = feedEntryService;
    }

    @POST
    @Transactional
    public Response createNote(@Valid NoteRequest req) {
        User user = authenticationContext.getCurrentUser();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        FeedEntryStatus status = feedEntryService.getStatusById(req.getEntryId());
        if (status == null || !status.getUser().getId().equals(user.getId())) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        FeedEntry entry = status.getEntry();
        boolean existed = feedEntryNoteDAO.findByEntry(user, entry) != null;
        FeedEntryNote note =
                feedEntryNoteService.createOrAttach(
                        user, entry.getId(), req.getText(), req.getRating());
        if (note == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        NoteResponse resp = NoteResponse.from(note);
        resp.setEntryId(status.getId());
        if (existed) {
            return Response.ok(resp).build();
        }
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    @Transactional
    public Response listNotes(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        User user = authenticationContext.getCurrentUser();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<NoteResponse> notes =
                feedEntryNoteService.listUserNotes(user, offset, limit).stream()
                        .map(NoteResponse::from)
                        .collect(Collectors.toList());
        return Response.ok(notes).build();
    }
}
