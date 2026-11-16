package app.triggerloom.core;

@FunctionalInterface
public interface ServiceStatusListener {
    void onServiceStatusChanged(ServiceStatus status);
}
