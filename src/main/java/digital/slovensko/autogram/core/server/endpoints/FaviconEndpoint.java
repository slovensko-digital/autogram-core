package digital.slovensko.autogram.core.server.endpoints;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class FaviconEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var stream = new ByteArrayInputStream(Objects.requireNonNull(getClass().getResourceAsStream("favicon.png")).readAllBytes());

        try (exchange) {
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, 0);
            requireNonNull(stream).transferTo(exchange.getResponseBody());
        }

        stream.close();
    }
}
