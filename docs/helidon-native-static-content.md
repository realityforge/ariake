# Helidon-native routing

The static content example serves files with Helidon's native static-content module instead of reading files through
application-managed byte arrays.

`StaticContentRoutes` registers three native Helidon handlers under `/static`:

1. A cache-control route that sets cache headers for `.cache.` and `.nocache.` resources, then calls
   `ServerResponse.next()`.
2. A Brotli route that checks `Accept-Encoding`, verifies that a `.br` sidecar exists, and streams that sidecar through
   `ServerResponse.outputStream()`.
3. `StaticContentFeature.createService(FileSystemHandlerConfig)` for the normal filesystem-backed static content path.

This keeps large ordinary files on Helidon's static-content path. Brotli sidecars are also streamed and are not loaded into
heap as a complete byte array.

The filters are Helidon route handlers. They are registered before the static-content service and either send the response
or call `next()` so Helidon can continue routing. Header mutation happens before `next()` or before opening the response
output stream.

## Server assembly

Ariake uses Helidon as its HTTP runtime and application HTTP API. Application services implement `HttpRoutingService` and
receive Helidon's `HttpRouting.Builder` directly. WebSocket services implement `WebSocketRoutingService` and receive
Helidon's `WsRouting.Builder` directly.

`AriakeServer` centralizes configuration-driven server startup while leaving route handlers on Helidon's native
`ServerRequest`, `ServerResponse`, and `WsListener` APIs. Sting continues to compose route providers and application
dependencies.
