package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryNoteDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import java.time.Instant;
import java.util.List;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.ValidationException;

@Singleton
public class FeedEntryNoteService {

    private final FeedEntryDAO feedEntryDAO;
    private final FeedEntryNoteDAO feedEntryNoteDAO;

    public FeedEntryNoteService(FeedEntryDAO feedEntryDAO, FeedEntryNoteDAO feedEntryNoteDAO) {
        this.feedEntryDAO = feedEntryDAO;
        this.feedEntryNoteDAO = feedEntryNoteDAO;
    }

    @Transactional
    public FeedEntryNote createOrAttach(User user, Long entryId, String text, Integer rating) {
        if (user == null) {
            throw new ValidationException("User must be authenticated");
        }
        if (entryId == null) {
            throw new ValidationException("entryId is required");
        }
        if (text != null && text.length() > 500) {
            throw new ValidationException("text is too long (max 500)");
        }
        if (rating != null && (rating < 0 || rating > 5)) {
            throw new ValidationException("rating must be between 0 and 5");
        }

        FeedEntry entry = feedEntryDAO.findById(entryId);
        if (entry == null) {
            return null; // controller will handle 404
        }

        FeedEntryNote note = feedEntryNoteDAO.findByEntry(user, entry);
        Instant now = Instant.now();
        if (note == null) {
            note = new FeedEntryNote(user, entry, text, rating, now);
            feedEntryNoteDAO.persist(note);
        } else {
            note.setText(text);
            note.setRating(rating);
            note.setUpdated(now);
            feedEntryNoteDAO.merge(note);
        }
        return note;
    }

    public List<FeedEntryNote> listUserNotes(User user, int offset, int limit) {
        return feedEntryNoteDAO.findByUser(user, offset, limit);
    }
}
