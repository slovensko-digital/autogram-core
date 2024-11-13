package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.IOException;

public class XDCValidatorTests {
    @Test
    void testBuildXDCValidatorFromInvalidDocument() {
        var content = "";
        var document =  new InMemoryDocument(content.getBytes(), null);

        Assertions.assertThrows(XMLValidationException.class, () -> XDCValidator.validateXml(null, null, document, CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, false));
    }

    @Test
    void testGetDigestValueElementNotFound() throws IOException, XMLValidationException {
        var content = new String(this.getClass().getResourceAsStream("document-content-no-UsedXSDReference.xml").readAllBytes());
        var document =  new InMemoryDocument(content.getBytes(), null);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";

        Assertions.assertThrows(XMLValidationException.class, () -> XDCValidator.validateXsdDigest(schema, EFormUtils.getXmlFromDocument(document).getDocumentElement(), CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256));
    }

    @Test
    void testGetDigestValueAttributesOfElementNotFound() throws IOException, XMLValidationException {
        var content = new String(this.getClass().getResourceAsStream("document-content-UsedXSDReference-no-attributes.xml").readAllBytes());
        var document =  new InMemoryDocument(content.getBytes(), null);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";

        Assertions.assertThrows(XMLValidationException.class, () -> XDCValidator.validateXsdDigest(schema, EFormUtils.getXmlFromDocument(document).getDocumentElement(), CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256));
    }

    @Test
    void testGetDigestValueNotFound() throws IOException, XMLValidationException {
        var content = new String(this.getClass().getResourceAsStream("document-content-UsedXSDReference-no-DigestValue.xml").readAllBytes());
        var document =  new InMemoryDocument(content.getBytes(), null);

        var schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\" xmlns=\"http://schemas.gov.sk/form/App.GeneralAgenda/1.9\">\n<xs:simpleType name=\"textArea\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n<xs:simpleType name=\"meno\">\n<xs:restriction base=\"xs:string\">\n</xs:restriction>\n</xs:simpleType>\n\n\n<xs:element name=\"GeneralAgenda\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"subject\" type=\"meno\" minOccurs=\"0\" nillable=\"true\" />\n<xs:element name=\"text\" type=\"textArea\" minOccurs=\"0\" nillable=\"true\" />\n</xs:sequence>\n</xs:complexType>\n</xs:element>\n</xs:schema>";

        Assertions.assertThrows(XMLValidationException.class, () -> XDCValidator.validateXsdDigest(schema, EFormUtils.getXmlFromDocument(document).getDocumentElement(), CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256));
    }

    @Test
    void testGetContentXDCXmlDataNotFound() throws XMLValidationException {
        var content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:UsedSchemasReferenced><xdc:UsedXSDReference DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"/Ctn0B9D7HKn6URFR8iPUKfyGe4mBYpK+25dc1iYWuE=\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xsd</xdc:UsedXSDReference><xdc:UsedPresentationSchemaReference ContentType=\"application/xslt+xml\" DigestMethod=\"urn:oid:2.16.840.1.101.3.4.2.1\" DigestValue=\"Qo1jYX1JWydvM/OL/rnirphk1rM1z41fPRXBEgp/qbg=\" Language=\"sk\" MediaDestinationTypeDescription=\"TXT\" TransformAlgorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">http://schemas.gov.sk/form/App.GeneralAgenda/1.9/form.xslt</xdc:UsedPresentationSchemaReference></xdc:UsedSchemasReferenced></xdc:XMLDataContainer>";
        var document =  new InMemoryDocument(content.getBytes(), null);

        Assertions.assertThrows(XMLValidationException.class, () -> EFormUtils.getEformXmlFromXdcDocument(document));
    }

    @Test
    void testGetContentFromXDCEmptyContent() throws XMLValidationException {
        var content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdc:XMLDataContainer xmlns:xdc=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><xdc:XMLData></xdc:XMLData></xdc:XMLDataContainer>";
        var document =  new InMemoryDocument(content.getBytes(), null);


        Assertions.assertThrows(XMLValidationException.class, () -> EFormUtils.getEformXmlFromXdcDocument(document));
    }
}
