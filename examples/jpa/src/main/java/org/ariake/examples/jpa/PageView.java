package org.ariake.examples.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "page_views")
public class PageView {
    @Id
    @Column(name = "id", nullable = false)
    private String id = "";

    @Column(name = "path", nullable = false)
    private String path = "";

    @Column(name = "viewed_at", nullable = false)
    private Instant viewedAt = Instant.EPOCH;

    protected PageView() {}

    PageView(final String path, final Instant viewedAt) {
        id = UUID.randomUUID().toString();
        this.path = path;
        this.viewedAt = viewedAt;
    }

    String id() {
        return id;
    }

    String path() {
        return path;
    }

    Instant viewedAt() {
        return viewedAt;
    }
}
