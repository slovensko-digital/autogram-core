package digital.slovensko.autogram.core.server.responders;

import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.server.EndpointUtils;
import digital.slovensko.autogram.core.server.dto.Document;
import digital.slovensko.autogram.core.server.dto.ErrorResponse;

public class DocumentAPIResponder implements Responder {
    private final HttpExchange exchange;

    public DocumentAPIResponder(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onSuccess(SignedDocument signedDocument) throws AutogramException {
        EndpointUtils.respondWith(Document.buildFromDSS(signedDocument.dssDocument()), exchange);
    }

    @Override
    public void onError(AutogramException error) {
        EndpointUtils.respondWithError(ErrorResponse.buildFromException(error), exchange);
    }
}
