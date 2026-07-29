package com.commafeed.backend.rest.response;

import com.commafeed.backend.model.FeedEntryNote;
import java.io.Serializable;
import java.time.Instant;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema(description = "Note Response")
@Data
public class NoteResponse implements Serializable {

    private Long id;
    private Long entryId;
    private String text;
    private Integer rating;
    private Instant created;
    private Instant updated;

    public static NoteResponse from(FeedEntryNote note) {
        if (note == null) {
            return null;
        }
        NoteResponse r = new NoteResponse();
        r.setId(note.getId());
        r.setEntryId(note.getEntry().getId());
        r.setText(note.getText());
        r.setRating(note.getRating());
        r.setCreated(note.getCreated());
        r.setUpdated(note.getUpdated());
        return r;
    }
}
