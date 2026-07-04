package org.ariake.jpa.eclipselink;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sample_entities")
public class SampleEntity {
    @Id
    @Column(name = "id", nullable = false)
    private String id = "";

    protected SampleEntity() {}

    SampleEntity(final String id) {
        this.id = id;
    }

    String id() {
        return id;
    }
}
