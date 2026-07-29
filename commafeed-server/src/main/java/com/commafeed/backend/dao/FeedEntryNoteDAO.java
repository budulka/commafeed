package com.commafeed.backend.dao;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import com.querydsl.jpa.impl.JPAQuery;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.List;

@Singleton
public class FeedEntryNoteDAO extends GenericDAO<FeedEntryNote> {

    private static final QFeedEntryNote NOTE = QFeedEntryNote.feedEntryNote;

    public FeedEntryNoteDAO(EntityManager entityManager) {
        super(entityManager, FeedEntryNote.class);
    }

    public FeedEntryNote findByEntry(User user, FeedEntry entry) {
        return query().selectFrom(NOTE)
                .where(NOTE.user.eq(user).and(NOTE.entry.eq(entry)))
                .fetchOne();
    }

    public List<FeedEntryNote> findByUser(User user, int offset, int limit) {
        JPAQuery<FeedEntryNote> q = query().selectFrom(NOTE)
                .where(NOTE.user.eq(user))
                .orderBy(NOTE.created.desc());
        if (offset > 0) { q.offset(offset); }
        if (limit > 0) { q.limit(limit); }
        setTimeout(q, Duration.ZERO);
        return q.fetch();
    }
}
