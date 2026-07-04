package org.ariake.jpa.eclipselink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.persistence.EntityManager;
import java.util.Map;
import org.junit.Test;

public final class EclipseLinkEntityManagerProviderTest {
    private static final String PERSISTENCE_UNIT = "eclipselink-provider-test";

    @Test
    public void createsEntityManagersWithPersistenceUnitDefaults() {
        try (EclipseLinkEntityManagerProvider provider = EclipseLinkEntityManagerProvider.create(PERSISTENCE_UNIT)) {
            final EntityManager entityManager = provider.createEntityManager();
            try {
                assertTrue(entityManager.isOpen());
            } finally {
                entityManager.close();
            }
        }
    }

    @Test
    public void createsEntityManagersWithOverriddenProperties() {
        final var properties =
                Map.of("jakarta.persistence.jdbc.url", "jdbc:h2:mem:ariake_eclipselink_provider_override");

        try (EclipseLinkEntityManagerProvider provider =
                EclipseLinkEntityManagerProvider.create(PERSISTENCE_UNIT, properties)) {
            final EntityManager entityManager = provider.createEntityManager();
            try {
                entityManager.getTransaction().begin();
                entityManager.persist(new SampleEntity("sample"));
                entityManager.getTransaction().commit();
                entityManager.clear();

                assertEquals(
                        "sample",
                        entityManager.find(SampleEntity.class, "sample").id());
            } finally {
                rollbackIfActive(entityManager);
                entityManager.close();
            }
        }
    }

    private static void rollbackIfActive(final EntityManager entityManager) {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }
}
