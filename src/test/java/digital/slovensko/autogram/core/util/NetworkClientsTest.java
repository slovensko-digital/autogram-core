package digital.slovensko.autogram.core.util;

import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkClientsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://93.184.216.34/ca.crt",
            "https://93.184.216.34/ca.crt",
            "http://8.8.8.8/aia",
    })
    void allowsPublicHttpAndHttps(String url) {
        assertTrue(NetworkClients.isAllowed(url), url);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // non-HTTP schemes an attacker certificate may carry
            "file:///etc/passwd",
            "file://10.37.1.10/share/x.cer",
            "ldap://ldap.example.com/CN=CA?cACertificate",
            "ftp://ftp.example.com/ca.crt",
            "jar:http://8.8.8.8/x!/",
            // internal and reserved destinations, even over HTTP
            "http://127.0.0.1/x",
            "http://10.0.0.1/x",
            "http://192.168.1.1/x",
            "http://172.16.0.1/x",
            "http://169.254.169.254/latest/meta-data",
            "http://100.64.0.1/x",
            "http://[::1]/x",
            "http://[fc00::1]/x",
            "https://localhost/x",
            // not a valid URI (e.g. an LDAP DN with spaces)
            "not a url",
    })
    void blocksNonHttpAndInternalDestinations(String url) {
        assertFalse(NetworkClients.isAllowed(url), url);
    }

    @Test
    void dataLoaderRejectsBlockedUrlBeforeConnecting() {
        assertThrows(DSSExternalResourceException.class,
                () -> NetworkClients.dataLoader().get("file:///etc/passwd"));
        assertThrows(DSSExternalResourceException.class,
                () -> NetworkClients.dataLoader().get("http://127.0.0.1/x"));
    }

    @Test
    void dataLoaderRejectsListOfOnlyBlockedUrls() {
        assertThrows(DSSExternalResourceException.class,
                () -> NetworkClients.dataLoader().get(List.of("file:///x", "ldap://y/z")));
    }

    @Test
    void ocspDataLoaderRejectsBlockedUrlBeforeConnecting() {
        assertThrows(DSSExternalResourceException.class,
                () -> NetworkClients.ocspDataLoader().get("http://127.0.0.1/ocsp"));
        assertThrows(DSSExternalResourceException.class,
                () -> NetworkClients.ocspDataLoader().post("ftp://ocsp.example.com", new byte[0]));
    }

    @Test
    void certificateVerifierUsesAnAiaSource() {
        assertInstanceOf(DefaultAIASource.class, NetworkClients.certificateVerifier().getAIASource());
    }

    @Test
    void signaturePolicyProviderDoesNotFetchBlockedUrls() {
        var provider = NetworkClients.signaturePolicyProvider();
        // An unrestricted provider would return the contents of this readable local file; the restricted one must not.
        assertNull(provider.getSignaturePolicyByUrl("file:///etc/passwd"));
        assertNull(provider.getSignaturePolicyByUrl("http://169.254.169.254/latest/meta-data"));
    }
}
