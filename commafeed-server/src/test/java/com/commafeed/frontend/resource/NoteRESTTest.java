package com.commafeed.frontend.resource;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryNoteDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import com.commafeed.backend.rest.request.NoteRequest;
import com.commafeed.backend.rest.resources.NoteREST;
import com.commafeed.backend.service.FeedEntryNoteService;
import com.commafeed.security.AuthenticationContext;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class NoteRESTTest {

    private AuthenticationContext auth;
    private FeedEntryNoteDAO feedEntryNoteDAO;
    private FeedEntryDAO feedEntryDAO;
    private FeedEntryNoteService feedEntryNoteService;
    private NoteREST noteREST;
    private User user;

    @BeforeEach
    public void init() {
        auth = Mockito.mock(AuthenticationContext.class);
        feedEntryNoteDAO = Mockito.mock(FeedEntryNoteDAO.class);
        feedEntryDAO = Mockito.mock(FeedEntryDAO.class);
        feedEntryNoteService = Mockito.mock(FeedEntryNoteService.class);
        noteREST = new NoteREST(auth, feedEntryNoteDAO, feedEntryDAO, feedEntryNoteService);

        user = new User();
        user.setId(1L);
        Mockito.when(auth.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void testCreateNoteSuccess() {
        NoteRequest req = new NoteRequest();
        req.setEntryId(123L);
        req.setText("My note");
        req.setRating(5);

        FeedEntry entry = new FeedEntry();
        entry.setId(123L);
        Mockito.when(feedEntryDAO.findById(123L)).thenReturn(entry);
        
        FeedEntryNote note = new FeedEntryNote();
        note.setId(1L);
        note.setUser(user);
        note.setEntry(entry);
        note.setText("My note");
        note.setRating(5);
        
        Mockito.when(feedEntryNoteService.createOrAttach(Mockito.eq(user), Mockito.eq(123L), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(note);

        Response resp = noteREST.createNote(req);
        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testListNotes() {
        FeedEntry entry = new FeedEntry();
        entry.setId(123L);

        FeedEntryNote note = new FeedEntryNote();
        note.setId(1L);
        note.setUser(user);
        note.setEntry(entry);
        note.setText("My note");

        Mockito.when(feedEntryNoteService.listUserNotes(user, 0, 50))
                .thenReturn(Collections.singletonList(note));

        Response resp = noteREST.listNotes(0, 50);
        Assertions.assertEquals(200, resp.getStatus());
        List<?> entity = (List<?>) resp.getEntity();
        Assertions.assertEquals(1, entity.size());
    }
}
