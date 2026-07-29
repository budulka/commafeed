package com.commafeed.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FEEDENTRYNOTES")
@Getter
@Setter
public class FeedEntryNote extends AbstractModel {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id")
    private FeedEntry entry;

    @Column(length = 500)
    private String text;

    private Integer rating;

    private Instant created;

    private Instant updated;

    public FeedEntryNote() {}

    public FeedEntryNote(User user, FeedEntry entry, String text, Integer rating, Instant now) {
        this.user = user;
        this.entry = entry;
        this.text = text;
        this.rating = rating;
        this.created = now;
        this.updated = now;
    }
}
