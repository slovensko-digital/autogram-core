package digital.slovensko.autogram.core.server.endpoints;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.validation.SignatureValidator;
import digital.slovensko.autogram.core.server.dto.InfoResponse;

import java.io.IOException;

public class InfoEndpoint implements HttpHandler {
    private final String version;

    public InfoEndpoint(String version) {
        this.version = version;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var response = new InfoResponse(version, InfoResponse.getStatus(), SignatureValidator.getInstance().areTLsLoaded());
        var gson = new Gson();

        try (exchange) {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
        }
    }
}
