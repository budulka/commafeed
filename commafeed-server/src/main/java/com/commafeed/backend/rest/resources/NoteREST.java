package com.commafeed.backend.rest.resources;

import com.commafeed.backend.auth.AuthenticationContext;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryNoteDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import com.commafeed.backend.rest.request.NoteRequest;
import com.commafeed.backend.rest.response.NoteResponse;
import com.commafeed.backend.service.FeedEntryNoteService;
import com.commafeed.backend.util.Roles;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    public NoteREST(
            AuthenticationContext authenticationContext,
            FeedEntryNoteDAO feedEntryNoteDAO,
            FeedEntryDAO feedEntryDAO,
            FeedEntryNoteService feedEntryNoteService) {
        this.authenticationContext = authenticationContext;
        this.feedEntryNoteDAO = feedEntryNoteDAO;
        this.feedEntryDAO = feedEntryDAO;
        this.feedEntryNoteService = feedEntryNoteService;
    }

    @POST
    @Transactional
    public Response createNote(@Valid NoteRequest req) {
        User user = authenticationContext.getCurrentUser();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        FeedEntry entry = feedEntryDAO.findById(req.getEntryId());
        if (entry == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean existed = feedEntryNoteDAO.findByEntry(user, entry) != null;
        FeedEntryNote note =
                feedEntryNoteService.createOrAttach(
                        user, req.getEntryId(), req.getText(), req.getRating());
        if (note == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        NoteResponse resp = NoteResponse.from(note);
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
