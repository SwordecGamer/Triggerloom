
package app.triggerloom.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationLifecycleExecutorTest {

    @Test
    void executesLifecycleOperationsAsynchronously() throws Exception {
        ApplicationController controller = new ApplicationController();

        controller.registerService(new LifecycleService() {
            @Override
            public String getName() {
                return "Test Service";
            }

            @Override
            public void start() throws Exception {
                Thread.sleep(100);
            }

            @Override
            public void stop() {
            }
        });

        try (ApplicationLifecycleExecutor lifecycle =
                     new ApplicationLifecycleExecutor(controller)) {

            lifecycle.startAsync().get(5, TimeUnit.SECONDS);

            assertEquals(
                    ApplicationState.RUNNING,
                    controller.getState()
            );

            lifecycle.stopAsync().get(5, TimeUnit.SECONDS);

            assertEquals(
                    ApplicationState.STOPPED,
                    controller.getState()
            );
        }
    }
}
