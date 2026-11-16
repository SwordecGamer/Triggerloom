
package app.triggerloom.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ApplicationController {

    private final List<LifecycleService> services = new ArrayList<>();
    private final List<ApplicationStateListener> listeners =
            new CopyOnWriteArrayList<>();

    private volatile ApplicationState state = ApplicationState.CREATED;

    public ApplicationState getState() {
        return state;
    }

    public void addStateListener(ApplicationStateListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeStateListener(ApplicationStateListener listener) {
        listeners.remove(listener);
    }

    public synchronized void registerService(LifecycleService service) {
        if (state != ApplicationState.CREATED) {
            throw new IllegalStateException(
                    "Cannot register services after startup"
            );
        }

        services.add(Objects.requireNonNull(service));
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
                service.start();
                started.add(service);
            }

            changeState(ApplicationState.RUNNING);
        } catch (Exception exception) {
            // Also attempt cleanup of the service that failed.
            // Its start() method may have partially initialized resources.
            for (int i = started.size() - 1; i >= 0; i--) {
                try {
                    started.get(i).stop();
                } catch (Exception cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }

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
            try {
                services.get(i).stop();
            } catch (Exception exception) {
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

    private void changeState(ApplicationState newState) {
        ApplicationState previous = state;
        state = newState;

        for (ApplicationStateListener listener : listeners) {
            try {
                listener.onStateChanged(previous, newState);
            } catch (RuntimeException exception) {
                System.err.println(
                        "State listener failed: " + exception.getMessage()
                );
            }
        }
    }
}
