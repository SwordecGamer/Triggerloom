package app.triggerloom.core;

public interface LifecycleService {
    String getName();

    void start() throws Exception;

    void stop() throws Exception;
}
