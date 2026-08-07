package digital.slovensko.autogram.core.eforms;

import static digital.slovensko.autogram.core.eforms.EFormUtils.computeDigest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import digital.slovensko.autogram.core.eforms.dto.ManifestXsltEntry;
import digital.slovensko.autogram.core.errors.ServiceUnavailableException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EFormResourceLoader {
    private final FileCacheDataLoader dataLoader;

    public EFormResourceLoader() {
        dataLoader = new FileCacheDataLoader();
        dataLoader.setCacheExpirationTime(21600000);  // 6 hours
        dataLoader.setDataLoader(new CommonsDataLoader());
    }

    public EFormResourceLoader(FileCacheDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public byte[] getResource(String url) throws ServiceUnavailableException {
        DSSDocument document;
        try {
            document = dataLoader.getDocument(url);
        } catch (DSSExternalResourceException e) {
            var cause = e.getCause();
            var message = cause == null ? null : cause.getMessage();
            if (message == null)
                return null;

            var matcher = Pattern.compile("HTTP status code : (\\d{3})").matcher(message);
            if (!matcher.find())
                return null;

            if (matcher.group(1).startsWith("5"))
                throw new ServiceUnavailableException(url, e);

            return null;
        } catch (DSSException e) {
            return null;
        }

        if (document == null)
            return null;

        try (var inputStream = document.openStream()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    public ArrayList<ManifestXsltEntry> getManifestXsltEntries(NodeList nodes, String sourceUrl, String formUrl) {
        var entries = new ArrayList<ManifestXsltEntry>();

        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);

            var fullPath = nullOrNodeValue(node.getAttributes().getNamedItem("full-path"));
            if (fullPath == null)
                continue;

            fullPath = fullPath.replace("\\", "/");

            var mediaType = nullOrNodeValue(node.getAttributes().getNamedItem("media-type"));
            var mediaDestinationTypeDescription = nullOrNodeValue(
                    node.getAttributes().getNamedItem("media-destination-type-description"),
                    new String[] { "HTML", "XHTML", "TXT" });
            var mediaDestination = nullOrNodeValue(node.getAttributes().getNamedItem("media-destination"));
            var mediaDestinationType = nullOrNodeValue(node.getAttributes().getNamedItem("media-destination-type"));

            if (mediaDestination == null) {
                if (Stream.of(".sb.xslt", ".html.xslt").noneMatch(fullPath::endsWith))
                    continue;

                mediaDestination = fullPath.contains(".sb.xslt") ? "sign" : "view";
            } else {
                if (mediaType == null)
                    continue;

                if (Stream.of("sign", "x-xslt-ro", "view").noneMatch(mediaDestination::contains))
                    continue;

                if (Stream.of("application/xslt+xml", "text/xsl").noneMatch(mediaType::equals)
                        && (Stream.of("text/xml", "application/xml").noneMatch(mediaType::equals)
                        || Stream.of(".xsl", ".xslt").noneMatch(fullPath::endsWith)))
                    continue;
            }

            if (mediaDestinationTypeDescription == null && mediaDestinationType != null)
                mediaDestinationTypeDescription = switch (mediaDestinationType) {
                    case "text/plain" -> "TXT";
                    case "text/html" -> "HTML";
                    case "application/xhtml+xml" -> "XHTML";
                    default -> null;
                };

            if (mediaDestinationTypeDescription == null) {
                var xslt = getResource(sourceUrl + formUrl + "/" + fullPath);
                if (xslt == null)
                    continue;

                try {
                    mediaDestinationTypeDescription = EFormUtils.extractTransformationOutputMimeTypeString(
                            new String(xslt, EFormUtils.ENCODING), true);
                } catch (TransformationParsingErrorException e) {
                    continue;
                }
            }

            entries.add(new ManifestXsltEntry(
                    mediaType,
                    nullOrNodeValue(node.getAttributes().getNamedItem("media-language")),
                    mediaDestinationTypeDescription,
                    nullOrNodeValue(node.getAttributes().getNamedItem("target-environment")),
                    fullPath,
                    mediaDestination));
        }

        return entries;
    }

    public ManifestXsltEntry selectXslt(ArrayList<ManifestXsltEntry> entries, String xsltDestinationType,
                                        String xsltLanguage, String xsltTarget, String xsltDigest,
                                        String canonicalizationMethod, String sourcePrefix) {
        if (xsltDestinationType != null)
            entries.removeIf(entry -> !xsltDestinationType.equals(entry.destinationType()));

        if (xsltLanguage != null)
            entries.removeIf(entry -> !xsltLanguage.equals(entry.language()));

        if (xsltTarget != null)
            entries.removeIf(entry -> !xsltTarget.equals(entry.target()));

        if (xsltDigest != null)
            entries.removeIf(entry -> {
                try {
                    var xslt = getResource(sourcePrefix + entry.fullPath());
                    if (xslt == null)
                        return false;

                    var candidateDigest = computeDigest(xslt, canonicalizationMethod, DigestAlgorithm.SHA256,
                            EFormUtils.ENCODING, true);
                    return !candidateDigest.equals(xsltDigest);
                } catch (XMLValidationException e) {
                    return true;
                }
            });

        entries = filterIfExist(entries, entry -> entry.mediaDesination().equals("sign"));
        entries = filterIfExist(entries, entry -> List.of("HTML", "XHTML").contains(entry.destinationType()));
        entries = filterIfExist(entries, entry -> entry.mediaDesination().equals("view"));
        entries = filterIfExist(entries, entry -> entry.destinationType().equals("XHTML"));
        entries = filterIfExist(entries, entry -> entry.language().equals("sk"));
        entries = filterIfExist(entries, entry -> entry.language().equals("en"));

        return entries.stream().findFirst().orElse(null);
    }

    private static String nullOrNodeValue(Node node) {
        return nullOrNodeValue(node, null);
    }

    private static String nullOrNodeValue(Node node, String[] allowedValues) {
        var value = node != null && !node.getNodeValue().isEmpty() ? node.getNodeValue() : null;

        if (allowedValues != null && value != null && !List.of(allowedValues).contains(value))
            value = null;

        return value;
    }

    private static ArrayList<ManifestXsltEntry> filterIfExist(ArrayList<ManifestXsltEntry> entries,
                                                               Predicate<ManifestXsltEntry> predicate) {
        if (entries.stream().anyMatch(predicate))
            return new ArrayList<>(entries.stream().filter(predicate).toList());

        return entries;
    }
}
