package digital.slovensko.autogram.core.server.endpoints;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import digital.slovensko.autogram.core.server.EndpointUtils;
import digital.slovensko.autogram.core.server.dto.CompareRequestBody;
import digital.slovensko.autogram.core.server.dto.ErrorResponse;
import digital.slovensko.autogram.core.validation.SignedVersionValidator;

public class SignedVersionValidationEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, CompareRequestBody.class);
            body.validate();

            var responseBody = SignedVersionValidator.compare(body.getOriginalDSSDocuments(), body.getSignedDSSDocument());
            EndpointUtils.respondWith(responseBody, exchange);

        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
