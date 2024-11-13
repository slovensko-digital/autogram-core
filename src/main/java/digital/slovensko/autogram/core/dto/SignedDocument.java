package digital.slovensko.autogram.core.dto;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;

public record SignedDocument (DSSDocument dssDocument, CertificateToken certificateToken) {
}
