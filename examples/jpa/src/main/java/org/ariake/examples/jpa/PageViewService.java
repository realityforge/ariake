package org.ariake.examples.jpa;

import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.time.Instant;
import org.ariake.jpa.EntityManagerProvider;
import org.ariake.server.HttpRoutingService;
import sting.Injectable;

@Injectable
public final class PageViewService implements HttpRoutingService {
    private final EntityManagerProvider entityManagerProvider;

    PageViewService(final EntityManagerProvider entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    @Override
    public void routing(final HttpRouting.Builder routing) {
        routing.get("/page-views", this::countPageViews);
        routing.post("/page-views", this::recordPageView);
    }

    private void countPageViews(final ServerRequest request, final ServerResponse response) {
        response.send("{\"count\":" + count() + "}");
    }

    private void recordPageView(final ServerRequest request, final ServerResponse response) {
        final var body = request.content().as(String.class);
        record(body.isBlank() ? "/page-views" : body);
        response.status(201);
        response.send("{\"count\":" + count() + "}");
    }

    private void record(final String path) {
        final var entityManager = entityManagerProvider.createEntityManager();
        final var transaction = entityManager.getTransaction();
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
        try (var entityManager = entityManagerProvider.createEntityManager()) {
            return entityManager
                    .createQuery("select count(pageView) from PageView pageView", Long.class)
                    .getSingleResult();
        }
    }
}
