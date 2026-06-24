package org.ariake.examples.websocket;

import java.nio.charset.StandardCharsets;
import org.ariake.http.AriakeHttpService;
import org.ariake.http.HttpExchange;
import org.ariake.http.HttpRoutes;
import sting.Injectable;

@Injectable
public final class WebSocketPageService implements AriakeHttpService {
    private static final String CONTENT_TYPE = "text/html; charset=utf-8";
    private static final String INDEX_HTML = """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Ariake WebSocket Echo</title>
            <style>
              body { font-family: system-ui, sans-serif; margin: 2rem; max-width: 42rem; }
              form { display: flex; gap: 0.5rem; }
              input { flex: 1; padding: 0.6rem; }
              button { padding: 0.6rem 0.9rem; }
              #echoes { margin-top: 1rem; padding: 1rem; border: 1px solid #d0d7de; min-height: 8rem; }
              .echo { padding: 0.35rem 0; border-bottom: 1px solid #eaeef2; }
            </style>
          </head>
          <body>
            <h1>Ariake WebSocket Echo</h1>
            <form id="echo-form">
              <input id="message" name="message" autocomplete="off" placeholder="Message" required>
              <button type="submit">Send</button>
            </form>
            <section id="echoes" aria-live="polite"></section>
            <script>
              const socketUrl = `${location.protocol === "https:" ? "wss" : "ws"}://${location.host}/ws/echo`;
              const socket = new WebSocket(socketUrl);
              const form = document.querySelector("#echo-form");
              const input = document.querySelector("#message");
              const echoes = document.querySelector("#echoes");

              socket.addEventListener("message", event => {
                const line = document.createElement("div");
                line.className = "echo";
                line.textContent = event.data;
                echoes.append(line);
              });

              form.addEventListener("submit", event => {
                event.preventDefault();
                socket.send(input.value);
                input.value = "";
                input.focus();
              });
            </script>
          </body>
        </html>
        """;

    @Override
    public void routes(final HttpRoutes routes) {
        routes.get("/", this::serveIndex);
        routes.get("/index.html", this::serveIndex);
    }

    private void serveIndex(final HttpExchange exchange) {
        exchange.send(INDEX_HTML.getBytes(StandardCharsets.UTF_8), CONTENT_TYPE);
    }
}
