
package app.triggerloom.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationControllerTest {

    @Test
    void startsServicesInRegistrationOrder() throws Exception {
        ApplicationController controller = new ApplicationController();
        List<String> events = new ArrayList<>();

        controller.registerService(service("A", events));
        controller.registerService(service("B", events));

        controller.start();

        assertEquals(List.of("start A", "start B"), events);
        assertEquals(ApplicationState.RUNNING, controller.getState());
    }

    @Test
    void stopsServicesInReverseOrder() throws Exception {
        ApplicationController controller = new ApplicationController();
        List<String> events = new ArrayList<>();

        controller.registerService(service("A", events));
        controller.registerService(service("B", events));

        controller.start();
        controller.stop();

        assertEquals(
                List.of("start A", "start B", "stop B", "stop A"),
                events
        );
        assertEquals(ApplicationState.STOPPED, controller.getState());
    }

    @Test
    void notifiesListenersOfStateChanges() throws Exception {
        ApplicationController controller = new ApplicationController();
        List<ApplicationState> states = new ArrayList<>();

        controller.addStateListener((previous, current) ->
                states.add(current)
        );

        controller.start();
        controller.stop();

        assertEquals(
                List.of(
                        ApplicationState.STARTING,
                        ApplicationState.RUNNING,
                        ApplicationState.STOPPING,
                        ApplicationState.STOPPED
                ),
                states
        );
    }

    @Test
    void rollsBackStartedServicesWhenStartupFails() {
        ApplicationController controller = new ApplicationController();
        List<String> events = new ArrayList<>();

        controller.registerService(service("A", events));

        controller.registerService(new LifecycleService() {
            @Override
            public String getName() {
                return "Failing";
            }

            @Override
            public void start() throws Exception {
                events.add("start Failing");
                throw new Exception("Startup failure");
            }

            @Override
            public void stop() {
                events.add("stop Failing");
            }
        });

        Exception failure = assertThrows(
                Exception.class,
                controller::start
        );

        assertEquals("Startup failure", failure.getMessage());
        assertEquals(ApplicationState.FAILED, controller.getState());

        assertEquals(
                List.of("start A", "start Failing", "stop A"),
                events
        );
    }

    @Test
    void notifiesListenersOfServiceStateChanges() throws Exception {
        ApplicationController controller = new ApplicationController();

        List<ServiceState> observedStates = new ArrayList<>();

        controller.addServiceStatusListener(status ->
                observedStates.add(status.state())
        );

        controller.registerService(service("Demo", new ArrayList<>()));

        controller.start();
        controller.stop();

        assertEquals(
                List.of(
                        ServiceState.REGISTERED,
                        ServiceState.STARTING,
                        ServiceState.RUNNING,
                        ServiceState.STOPPING,
                        ServiceState.STOPPED
                ),
                observedStates
        );
    }

    private LifecycleService service(
            String name,
            List<String> events
    ) {
        return new LifecycleService() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public void start() {
                events.add("start " + name);
            }

            @Override
            public void stop() {
                events.add("stop " + name);
            }
        };
    }
}
