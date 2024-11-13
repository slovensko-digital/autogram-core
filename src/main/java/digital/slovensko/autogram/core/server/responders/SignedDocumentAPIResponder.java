package digital.slovensko.autogram.core.server.responders;

import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.server.EndpointUtils;
import digital.slovensko.autogram.core.server.dto.ErrorResponse;
import digital.slovensko.autogram.core.server.dto.SignedDocumentResponse;

public class SignedDocumentAPIResponder implements Responder {
    private final HttpExchange exchange;

    public SignedDocumentAPIResponder(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onSuccess(SignedDocument signedDocument) throws AutogramException {
        var signer = signedDocument.certificateToken().getSubject().getPrincipal().toString();
        var issuer = signedDocument.certificateToken().getIssuer().getPrincipal().toString();

        EndpointUtils.respondWith(SignedDocumentResponse.buildFormDSS(signedDocument.dssDocument(), signer, issuer), exchange);
    }

    @Override
    public void onError(AutogramException error) {
        EndpointUtils.respondWithError(ErrorResponse.buildFromException(error), exchange);
    }
}
