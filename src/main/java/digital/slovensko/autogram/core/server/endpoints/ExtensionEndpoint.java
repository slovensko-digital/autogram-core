package digital.slovensko.autogram.core.server.endpoints;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.SignatureExtender;
import digital.slovensko.autogram.core.server.EndpointUtils;
import digital.slovensko.autogram.core.server.dto.ErrorResponse;
import digital.slovensko.autogram.core.server.dto.ExtensionRequestBody;
import digital.slovensko.autogram.core.server.responders.DocumentAPIResponder;

public class ExtensionEndpoint implements HttpHandler {
    private final SignatureExtender signatureExtender;

    public ExtensionEndpoint(SignatureExtender signatureExtender) {
        this.signatureExtender = signatureExtender;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, ExtensionRequestBody.class);
            signatureExtender.extendDocument(body.document().getDecodedContent(), body.targetLevel(), new DocumentAPIResponder(exchange));

        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
