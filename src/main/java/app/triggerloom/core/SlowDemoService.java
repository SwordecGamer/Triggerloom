
package app.triggerloom.core;

public class SlowDemoService implements LifecycleService {

    @Override
    public String getName() {
        return "Slow Demo Service";
    }

    @Override
    public void start() throws Exception {
        System.out.println("Slow Demo Service initializing...");
        Thread.sleep(5000);
        System.out.println("Slow Demo Service initialized");
    }

    @Override
    public void stop() {
        System.out.println("Slow Demo Service stopped");
    }
}
