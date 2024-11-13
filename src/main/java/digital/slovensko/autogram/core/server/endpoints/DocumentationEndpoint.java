package digital.slovensko.autogram.core.server.endpoints;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class DocumentationEndpoint implements HttpHandler {
    private final byte[] serverYml;
    public DocumentationEndpoint(byte[] serverYml) {
        this.serverYml = serverYml;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var isYaml = exchange.getRequestURI().getPath().endsWith("server.yml");
        var mimeType = isYaml ? "text/yaml" : "text/html";
        var stream = isYaml ? new ByteArrayInputStream(serverYml) : getClass().getResourceAsStream("index.html");

        try (exchange) {
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, 0);
            requireNonNull(stream).transferTo(exchange.getResponseBody());
        }

        stream.close();
    }
}
