package digital.slovensko.autogram.core.server.filters;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Add CORS HTTP headers and check HTTP method.
 * Access-Control-Allow-*...
 */
public class AutogramCorsFilter extends Filter {
    private final List<String> allowedMethods;

    public AutogramCorsFilter(String allowedMethod) {
        this(List.of(allowedMethod));
    }

    public AutogramCorsFilter(List<String> allowedMethod) {
        this.allowedMethods = allowedMethod;
    }

    @Override
    public String description() {
        return "Add CORS headers and check HTTP method";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        doFilter(exchange, chain, "*");
    }

    public void doFilter(HttpExchange exchange, Chain chain, String allowedOrigin) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
        var methods = new ArrayList<>(allowedMethods);
        methods.add("OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods",
                String.join(",", methods));
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers",
                "Content-Type, Authorization");

        // Allow preflight requests
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().close();
            return;
        }

        // Check HTTP request method
        if (allowedMethods.stream().noneMatch((method) -> method.equalsIgnoreCase(exchange.getRequestMethod()))) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        chain.doFilter(exchange);
    }

}
