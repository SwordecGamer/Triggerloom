package app.triggerloom.core;

public class DemoService implements LifecycleService {

    @Override
    public String getName() {
        return "Demo Service";
    }

    @Override
    public void start() {
        System.out.println("Demo Service started");
    }

    @Override
    public void stop() {
        System.out.println("Demo Service stopped");
    }
}
