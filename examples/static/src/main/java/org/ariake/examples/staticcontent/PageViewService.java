package org.ariake.examples.staticcontent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.Instant;
import org.ariake.http.AriakeHttpService;
import org.ariake.http.HttpRoutes;
import org.ariake.jpa.EntityManagerProvider;
import sting.Injectable;

@Injectable
public final class PageViewService implements AriakeHttpService {
    private final EntityManagerProvider entityManagerProvider;

    PageViewService(final EntityManagerProvider entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    @Override
    public void routes(final HttpRoutes routes) {
        routes.get("/views", exchange -> exchange.send("{\"count\":" + count() + "}"));
    }

    void record(final String path) {
        final EntityManager entityManager = entityManagerProvider.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(new PageView(path, Instant.now()));
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
        }
    }

    private long count() {
        final EntityManager entityManager = entityManagerProvider.createEntityManager();
        try {
            return entityManager
                    .createQuery("select count(pageView) from PageView pageView", Long.class)
                    .getSingleResult();
        } finally {
            entityManager.close();
        }
    }
}
