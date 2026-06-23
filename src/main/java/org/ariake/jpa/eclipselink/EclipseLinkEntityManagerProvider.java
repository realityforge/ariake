package org.ariake.jpa.eclipselink;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.Map;
import org.ariake.jpa.EntityManagerProvider;

public final class EclipseLinkEntityManagerProvider implements EntityManagerProvider, AutoCloseable {
    private final EntityManagerFactory entityManagerFactory;

    private EclipseLinkEntityManagerProvider(final EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public static EclipseLinkEntityManagerProvider create(final String persistenceUnit) {
        return new EclipseLinkEntityManagerProvider(Persistence.createEntityManagerFactory(persistenceUnit));
    }

    public static EclipseLinkEntityManagerProvider create(
            final String persistenceUnit, final Map<String, ?> properties) {
        return new EclipseLinkEntityManagerProvider(
                Persistence.createEntityManagerFactory(persistenceUnit, properties));
    }

    @Override
    public EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }
}
