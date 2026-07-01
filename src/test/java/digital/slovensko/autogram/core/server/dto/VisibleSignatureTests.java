package digital.slovensko.autogram.core.server.dto;

import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.server.errors.RequestValidationException;
import com.google.gson.Gson;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

public class VisibleSignatureTests {
    private static final Gson gson = new Gson();

    @Test
    void buildsDssParametersWithFieldIdAndText() {
        var visibleSignature = gson.fromJson(
                "{\"fieldId\":\"signature-field-123\",\"text\":\"Electronically signed by\\nJane Doe\"}",
                VisibleSignature.class);

        var imageParameters = visibleSignature.toDssParameters();

        Assertions.assertEquals("signature-field-123", imageParameters.getFieldParameters().getFieldId());
        Assertions.assertEquals("Electronically signed by\nJane Doe", imageParameters.getTextParameters().getText());
        Assertions.assertNull(imageParameters.getImage());
    }

    @Test
    void buildsDssParametersWithImage() {
        var content = Base64.getEncoder().encodeToString("image-bytes".getBytes());
        var visibleSignature = gson.fromJson(
                "{\"fieldId\":\"field-1\",\"image\":{\"filename\":\"stamp.png\",\"content\":\"" + content
                        + "\",\"mimeType\":\"image/png;base64\"}}",
                VisibleSignature.class);

        var imageParameters = visibleSignature.toDssParameters();

        Assertions.assertEquals("field-1", imageParameters.getFieldParameters().getFieldId());
        Assertions.assertNotNull(imageParameters.getImage());
    }

    @Test
    void validateRejectsMissingFieldId() {
        var visibleSignature = gson.fromJson("{\"text\":\"hello\"}", VisibleSignature.class);

        Assertions.assertThrows(RequestValidationException.class,
                () -> visibleSignature.validate("Parameters.VisibleSignature"));
    }

    @Test
    void validateRejectsMissingImageAndText() {
        var visibleSignature = gson.fromJson("{\"fieldId\":\"field-1\"}", VisibleSignature.class);

        Assertions.assertThrows(RequestValidationException.class,
                () -> visibleSignature.validate("Parameters.VisibleSignature"));
    }

    @Test
    void appliesVisibleSignatureToPadesParameters() {
        var pdf = new InMemoryDocument("%PDF-1.4".getBytes(), "document.pdf", MimeTypeEnum.PDF);
        var signingParameters = SigningParameters.buildParameters(
                SignatureLevel.PAdES_BASELINE_B, null, null, null, false, null, null, null,
                null, false, null, false, 640, pdf);

        var visibleSignature = gson.fromJson(
                "{\"fieldId\":\"field-1\",\"text\":\"signed\"}", VisibleSignature.class);
        signingParameters.setPadesVisibleSignatureParameters(visibleSignature.toDssParameters());

        var padesParameters = signingParameters.getPAdESSignatureParameters();

        Assertions.assertNotNull(padesParameters.getImageParameters());
        Assertions.assertEquals("field-1",
                padesParameters.getImageParameters().getFieldParameters().getFieldId());
    }

    @Test
    void padesParametersHaveNoImageWhenVisibleSignatureAbsent() {
        var pdf = new InMemoryDocument("%PDF-1.4".getBytes(), "document.pdf", MimeTypeEnum.PDF);
        var signingParameters = SigningParameters.buildParameters(
                SignatureLevel.PAdES_BASELINE_B, null, null, null, false, null, null, null,
                null, false, null, false, 640, pdf);

        var padesParameters = signingParameters.getPAdESSignatureParameters();

        var imageParameters = padesParameters.getImageParameters();
        Assertions.assertTrue(imageParameters == null
                || imageParameters.getFieldParameters().getFieldId() == null);
    }
}
