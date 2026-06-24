# Helidon native static content

The static content example serves files with Helidon's native static-content module instead of reading files through
`HttpExchange.send(byte[])`.

`StaticContentRoutes` registers three native Helidon handlers under `/static`:

1. A cache-control route that sets cache headers for `.cache.` and `.nocache.` resources, then calls
   `ServerResponse.next()`.
2. A Brotli route that checks `Accept-Encoding`, verifies that a `.br` sidecar exists, and streams that sidecar through
   `ServerResponse.outputStream()`.
3. `StaticContentFeature.createService(FileSystemHandlerConfig)` for the normal filesystem-backed static content path.

This keeps large ordinary files on Helidon's static-content path. Brotli sidecars are also streamed and are not loaded into
heap as a complete byte array.

The filters are Helidon route handlers, not Ariake `HttpFilter` instances. They are registered before the static-content
service and either send the response or call `next()` so Helidon can continue routing. Header mutation happens before
`next()` or before opening the response output stream.

## Adapter layer

Ariake currently has a small adapter layer:

- `AriakeHttpService`, `HttpRoutes`, `HttpExchange`, and `HttpFilter` describe framework-neutral HTTP behavior.
- `HelidonAriakeServer` translates those routes into Helidon WebServer routes.
- `HelidonRoutingService` is the explicit escape hatch for features that should use Helidon directly.

The adapter is useful while Ariake is trying to keep application code independent from Helidon. It also centralizes server
startup, config, WebSocket registration, and route translation.

Moving directly to Helidon is viable if Ariake is Helidon-only. Sting can compose Helidon-native objects the same way it
currently composes Ariake services: application fragments can produce `HttpService`, route handlers, server features, or
typed registration objects that a Helidon server assembly consumes.

If the project commits to Helidon as the only HTTP runtime, the cleaner end state is to remove the framework-neutral HTTP
types and make Helidon the application API:

- replace `AriakeHttpService` with Helidon `HttpService` or route registration providers;
- replace `HttpExchange` handlers with Helidon `ServerRequest`/`ServerResponse` handlers;
- replace Ariake filters with Helidon route handlers or Helidon filters;
- keep Sting as the composition mechanism that assembles the Helidon server.

Until that decision is made, the adapter plus `HelidonRoutingService` keeps ordinary examples portable while allowing static
content to use Helidon's file streaming support.
