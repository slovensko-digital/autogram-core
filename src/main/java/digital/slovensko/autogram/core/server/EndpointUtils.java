package digital.slovensko.autogram.core.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.server.dto.ErrorResponse;
import digital.slovensko.autogram.core.server.errors.EmptyBodyException;
import digital.slovensko.autogram.core.server.errors.MalformedBodyException;
import digital.slovensko.autogram.core.server.errors.ResponseNetworkErrorException;

import java.io.IOException;

public class EndpointUtils {
    private final static Gson gson = new Gson();

    public static void respondWithError(ErrorResponse error, HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(error.statusCode(), 0);
            if (error.statusCode() != 204)
                exchange.getResponseBody().write(gson.toJson(error.body()).getBytes());
            exchange.getResponseBody().close();
        } catch (IOException e) {
            throw new ResponseNetworkErrorException("Externá aplikácia nečakala na odpoveď", e);
        }
    }

    public static void respondWith(Object response, HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
            exchange.getResponseBody().close();
        } catch (IOException e) {
            throw new ResponseNetworkErrorException("Externá aplikácia nečakala na odpoveď", e);
        }
    }

    public static <T> T loadFromJsonExchange(HttpExchange exchange, Class<T> classOfT) throws AutogramException {
        String content;
        try {
            content = new String(exchange.getRequestBody().readAllBytes());
        } catch (IOException e) {
            throw new MalformedBodyException("Failed to load request body.", "");
        }

        if (content.isEmpty())
            throw new EmptyBodyException("Empty body");

        try {
            var ret = gson.fromJson(content, classOfT);

            if (ret == null)
                throw new MalformedBodyException("Failed to parse JSON body", "");

            return ret;

        } catch (JsonSyntaxException e) {
            throw new MalformedBodyException("Failed to parse JSON body", e);
        }
    }
}
