package digital.slovensko.autogram.core.server.dto;

public record InfoResponse (String version, String status, boolean trustedListsLoaded) {
    public static String getStatus() {
        return "READY"; // TODO: check if server is ready
    }
}
