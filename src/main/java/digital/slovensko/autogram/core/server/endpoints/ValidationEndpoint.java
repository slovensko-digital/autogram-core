package digital.slovensko.autogram.core.server.endpoints;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import digital.slovensko.autogram.core.validation.SignatureValidator;
import digital.slovensko.autogram.core.server.EndpointUtils;
import digital.slovensko.autogram.core.server.dto.Document;
import digital.slovensko.autogram.core.server.dto.ErrorResponse;
import digital.slovensko.autogram.core.server.dto.ValidationResponseBody;
import digital.slovensko.autogram.core.server.errors.MalformedBodyException;

public class ValidationEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, Document.class);
            if (body.content() == null)
                throw new MalformedBodyException("Document content is null", "Document content is null");

            var document = body.getDecodedContent();
            try (var stream = document.openStream()) {
                if (stream.readAllBytes().length < 1)
                    throw new MalformedBodyException("Document content is empty", "Document content is empty");
            }

            var reportsAndValidator = SignatureValidator.getInstance().validate(document);
            if (reportsAndValidator == null || reportsAndValidator.reports() == null)
                throw new DocumentNotSignedYetException();

            var responseBody = ValidationResponseBody.build(reportsAndValidator.reports(), reportsAndValidator.validator(), document);
            EndpointUtils.respondWith(responseBody, exchange);

        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
