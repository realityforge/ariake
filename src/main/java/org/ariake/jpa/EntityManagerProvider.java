package org.ariake.jpa;

import jakarta.persistence.EntityManager;

@FunctionalInterface
public interface EntityManagerProvider {
    EntityManager createEntityManager();
}
