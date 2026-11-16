package app.triggerloom.core;

public record ServiceStatus(
        String name,
        ServiceState state,
        String errorMessage
) {
}
