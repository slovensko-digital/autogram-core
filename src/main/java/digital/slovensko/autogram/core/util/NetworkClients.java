package digital.slovensko.autogram.core.util;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.policy.SignaturePolicyProvider;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Data loaders and certificate verifiers for validating documents that come from a request. Every outbound lookup
 * done while building or checking a submitted signature - AIA caIssuers, CRL, OCSP, signature policy - is confined
 * to plain HTTP(S) and to public destinations, with the same check re-applied after each redirect.
 *
 * <p>The URL of these lookups is taken from the submitted certificate or signature, so without this a request could
 * steer the lookup at an arbitrary host, port or scheme (e.g. {@code file:}, {@code ldap:}, a loopback or internal
 * address). DSS's own defaults do not restrict any of that: {@link CommonCertificateVerifier} installs a
 * {@link DefaultAIASource} backed by a native loader that accepts every scheme and destination.
 */
public final class NetworkClients {
    private NetworkClients() {
    }

    /** For AIA caIssuers and CRL: follows redirects but re-checks every hop. */
    public static DataLoader dataLoader() {
        return new RestrictedDataLoader(new CheckedRedirectsDataLoader(NetworkClients::isAllowed),
                NetworkClients::isAllowed);
    }

    /** For OCSP, which is a POST and must not follow redirects. */
    public static DataLoader ocspDataLoader() {
        var loader = new OCSPDataLoader();
        loader.setRedirectsEnabled(false);
        return new RestrictedDataLoader(loader, NetworkClients::isAllowed);
    }

    /** A verifier whose AIA lookups are restricted; DSS's default one is not. */
    public static CommonCertificateVerifier certificateVerifier() {
        var verifier = new CommonCertificateVerifier();
        verifier.setAIASource(new DefaultAIASource(dataLoader()));
        return verifier;
    }

    /** DSS lazily creates a native, unrestricted loader for signature policies unless one is provided. */
    public static SignaturePolicyProvider signaturePolicyProvider() {
        var provider = new SignaturePolicyProvider();
        provider.setDataLoader(dataLoader());
        return provider;
    }

    static boolean isAllowed(String url) {
        return isHttp(url) && !leadsToInternalAddress(url);
    }

    // Prefix check only: URLs from certificates (e.g. LDAP DNs with spaces) need not be valid URIs.
    private static boolean isHttp(String url) {
        var lowerCaseUrl = url.toLowerCase(Locale.ROOT);
        return lowerCaseUrl.startsWith("http://") || lowerCaseUrl.startsWith("https://");
    }

    private static boolean leadsToInternalAddress(String url) {
        try {
            var host = new URI(url).getHost();
            return host == null || Arrays.stream(InetAddress.getAllByName(host)).anyMatch(NetworkClients::isInternal);
        } catch (URISyntaxException e) {
            return true;
        } catch (UnknownHostException e) {
            return false; // the host cannot be reached anyway, so connecting fails without us blocking it
        }
    }

    private static boolean isInternal(InetAddress address) {
        var bytes = address.getAddress();
        var sharedIpv4 = bytes.length == 4 && (bytes[0] & 0xff) == 100 && (bytes[1] & 0xc0) == 64;
        var uniqueLocalIpv6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
        return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || sharedIpv4 || uniqueLocalIpv6;
    }

    /** Re-applies the check to each redirect target, which DSS would otherwise follow unchecked. */
    private static final class CheckedRedirectsDataLoader extends CommonsDataLoader {
        private final Predicate<String> isAllowed;

        CheckedRedirectsDataLoader(Predicate<String> isAllowed) {
            this.isAllowed = isAllowed;
        }

        @Override
        protected synchronized HttpClientBuilder getHttpClientBuilder(String url) {
            return super.getHttpClientBuilder(url).setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context)
                        throws HttpException {
                    var location = super.getLocationURI(request, response, context);
                    if (!isAllowed.test(location.toString()))
                        throw new ProtocolException("Redirect blocked: " + location);
                    return location;
                }
            });
        }
    }

    private record RestrictedDataLoader(DataLoader delegate, Predicate<String> isAllowed) implements DataLoader {
        private void check(String url) {
            if (!isAllowed.test(url))
                throw new DSSExternalResourceException("Outbound request blocked: " + url);
        }

        @Override
        public byte[] get(String url) {
            check(url);
            return delegate.get(url);
        }

        @Override
        public DataAndUrl get(List<String> urls) {
            // Filter before delegating: the delegate's list method calls its own get().
            var allowed = urls.stream().filter(isAllowed).toList();
            if (allowed.isEmpty())
                throw new DSSExternalResourceException("Outbound requests blocked.");
            return delegate.get(allowed);
        }

        @Override
        public byte[] post(String url, byte[] content) {
            check(url);
            return delegate.post(url, content);
        }

        @Override
        public void setContentType(String contentType) {
            delegate.setContentType(contentType);
        }
    }
}
