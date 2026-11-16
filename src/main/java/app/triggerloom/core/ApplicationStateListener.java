package app.triggerloom.core;

@FunctionalInterface
public interface ApplicationStateListener {
    void onStateChanged(ApplicationState previous, ApplicationState current);
}
