
package app.triggerloom.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ApplicationController {

    private final List<LifecycleService> services = new ArrayList<>();

    private final Map<LifecycleService, ServiceStatus> statuses =
            new LinkedHashMap<>();

    private final List<ApplicationStateListener> stateListeners =
            new CopyOnWriteArrayList<>();

    private final List<ServiceStatusListener> serviceListeners =
            new CopyOnWriteArrayList<>();

    private volatile ApplicationState state = ApplicationState.CREATED;

    public ApplicationState getState() {
        return state;
    }

    public void addStateListener(ApplicationStateListener listener) {
        stateListeners.add(Objects.requireNonNull(listener));
    }

    public void removeStateListener(ApplicationStateListener listener) {
        stateListeners.remove(listener);
    }

    public void addServiceStatusListener(ServiceStatusListener listener) {
        serviceListeners.add(Objects.requireNonNull(listener));
    }

    public void removeServiceStatusListener(ServiceStatusListener listener) {
        serviceListeners.remove(listener);
    }

    public synchronized List<ServiceStatus> getServiceStatuses() {
        return List.copyOf(statuses.values());
    }

    public synchronized void registerService(LifecycleService service) {
        Objects.requireNonNull(service);

        if (state != ApplicationState.CREATED) {
            throw new IllegalStateException(
                    "Cannot register services after startup"
            );
        }

        if (statuses.containsKey(service)) {
            throw new IllegalArgumentException(
                    "Service already registered"
            );
        }

        services.add(service);
        updateServiceStatus(service, ServiceState.REGISTERED, null);
    }

    public synchronized void start() throws Exception {
        if (state != ApplicationState.CREATED) {
            throw new IllegalStateException(
                    "Application already started"
            );
        }

        changeState(ApplicationState.STARTING);

        List<LifecycleService> started = new ArrayList<>();

        try {
            for (LifecycleService service : services) {
                updateServiceStatus(service, ServiceState.STARTING, null);

                try {
                    service.start();
                } catch (Exception exception) {
                    updateServiceStatus(
                            service,
                            ServiceState.FAILED,
                            exception.getMessage()
                    );
                    throw exception;
                }

                started.add(service);
                updateServiceStatus(service, ServiceState.RUNNING, null);
            }

            changeState(ApplicationState.RUNNING);
        } catch (Exception exception) {
            rollback(started, exception);
            changeState(ApplicationState.FAILED);
            throw exception;
        }
    }

    public synchronized void stop() throws Exception {
        if (state == ApplicationState.STOPPED) {
            return;
        }

        if (state != ApplicationState.RUNNING) {
            throw new IllegalStateException(
                    "Cannot stop application in state " + state
            );
        }

        changeState(ApplicationState.STOPPING);

        Exception failure = null;

        for (int i = services.size() - 1; i >= 0; i--) {
            LifecycleService service = services.get(i);

            updateServiceStatus(service, ServiceState.STOPPING, null);

            try {
                service.stop();
                updateServiceStatus(service, ServiceState.STOPPED, null);
            } catch (Exception exception) {
                updateServiceStatus(
                        service,
                        ServiceState.FAILED,
                        exception.getMessage()
                );

                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        changeState(failure == null
                ? ApplicationState.STOPPED
                : ApplicationState.FAILED);

        if (failure != null) {
            throw failure;
        }
    }

    private void rollback(
            List<LifecycleService> started,
            Exception originalFailure
    ) {
        for (int i = started.size() - 1; i >= 0; i--) {
            LifecycleService service = started.get(i);

            updateServiceStatus(service, ServiceState.STOPPING, null);

            try {
                service.stop();
                updateServiceStatus(service, ServiceState.STOPPED, null);
            } catch (Exception exception) {
                updateServiceStatus(
                        service,
                        ServiceState.FAILED,
                        exception.getMessage()
                );
                originalFailure.addSuppressed(exception);
            }
        }
    }

    private void updateServiceStatus(
            LifecycleService service,
            ServiceState newState,
            String errorMessage
    ) {
        ServiceStatus status = new ServiceStatus(
                service.getName(),
                newState,
                errorMessage
        );

        statuses.put(service, status);

        for (ServiceStatusListener listener : serviceListeners) {
            try {
                listener.onServiceStatusChanged(status);
            } catch (RuntimeException exception) {
                System.err.println(
                        "Service status listener failed: "
                                + exception.getMessage()
                );
            }
        }
    }

    private void changeState(ApplicationState newState) {
        ApplicationState previous = state;
        state = newState;

        for (ApplicationStateListener listener : stateListeners) {
            try {
                listener.onStateChanged(previous, newState);
            } catch (RuntimeException exception) {
                System.err.println(
                        "State listener failed: "
                                + exception.getMessage()
                );
            }
        }
    }
}
