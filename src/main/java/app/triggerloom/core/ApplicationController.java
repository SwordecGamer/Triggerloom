package app.triggerloom.core;

import java.util.ArrayList;
import java.util.List;

public class ApplicationController {
    private final List<LifecycleService> services = new ArrayList<>();

    private ApplicationState state = ApplicationState.CREATED;

    public ApplicationState getState() {
        return state;
    }

    public void registerService(LifecycleService service) {
        if (state != ApplicationState.CREATED) {
            throw new IllegalStateException("Cannot register services after startup");
        }

        services.add(service);
    }

    public void start() throws Exception {
        if (state != ApplicationState.CREATED) {
            throw new IllegalStateException("Application already started");
        }

        state = ApplicationState.STARTING;

        try {
            for (LifecycleService service : services) {
                service.start();
            }

            state = ApplicationState.RUNNING;
        } catch (Exception exception) {
            state = ApplicationState.FAILED;
            throw exception;
        }
    }

    public void stop() throws Exception {
        if (state == ApplicationState.STOPPED) {
            return;
        }

        state = ApplicationState.STOPPING;

        for (int i = services.size() - 1; i >= 0; i--) {
            services.get(i).stop();
        }

        state = ApplicationState.STOPPED;
    }
}
