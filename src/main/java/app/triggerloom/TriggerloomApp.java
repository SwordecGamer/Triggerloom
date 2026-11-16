package app.triggerloom;

import app.triggerloom.core.ApplicationController;
import app.triggerloom.core.ApplicationLifecycleExecutor;
import app.triggerloom.core.ApplicationState;
import app.triggerloom.core.DemoService;
import app.triggerloom.core.ServiceStatus;
import app.triggerloom.core.SlowDemoService;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TriggerloomApp extends Application {

    private final ApplicationController controller =
            new ApplicationController();

    private final ApplicationLifecycleExecutor lifecycle =
            new ApplicationLifecycleExecutor(controller);

    private final Label applicationStatus = new Label();
    private final VBox serviceList = new VBox(10);
    private final Map<String, Label> serviceLabels = new HashMap<>();

    @Override
    public void start(Stage stage) {
        Label title = new Label("Triggerloom Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label servicesTitle = new Label("Services");
        servicesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox root = new VBox(
                20,
                title,
                applicationStatus,
                servicesTitle,
                serviceList
        );

        root.setPadding(new Insets(25));

        Scene scene = new Scene(root, 1000, 650);

        stage.setTitle("Triggerloom");
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.setScene(scene);

        controller.addStateListener((previous, current) ->
                Platform.runLater(() ->
                        updateApplicationStatus(current)
                )
        );

        controller.addServiceStatusListener(status ->
                Platform.runLater(() -> updateServiceStatus(status))
        );

        controller.registerService(new DemoService());
        controller.registerService(new SlowDemoService());

        updateApplicationStatus(controller.getState());
        refreshServiceList();

        stage.show();

        lifecycle.startAsync().whenComplete((result, error) -> {
            if (error != null) {
                error.printStackTrace();
            }
        });
    }

    private void updateApplicationStatus(ApplicationState state) {
        applicationStatus.setText("Application status: " + state);
    }

    private void refreshServiceList() {
        serviceList.getChildren().clear();
        serviceLabels.clear();

        for (ServiceStatus status : controller.getServiceStatuses()) {
            updateServiceStatus(status);
        }
    }

//    private void refreshServiceList() {
//        serviceList.getChildren().clear();
//
//        for (ServiceStatus status : controller.getServiceStatuses()) {
//            Label label = new Label(
//                    status.name() + " — " + status.state()
//            );
//
//            switch (status.state()) {
//                case RUNNING -> label.setStyle("-fx-text-fill: #2ecc71;");
//                case STARTING, STOPPING ->
//                        label.setStyle("-fx-text-fill: #f39c12;");
//                case FAILED -> label.setStyle("-fx-text-fill: #e74c3c;");
//                default -> label.setStyle("-fx-text-fill: #888888;");
//            }
//
//            if (status.errorMessage() != null) {
//                label.setText(
//                        label.getText() + ": " + status.errorMessage()
//                );
//            }
//
//            serviceList.getChildren().add(label);
//        }
//    }

    private void updateServiceStatus(ServiceStatus status) {
        Label label = serviceLabels.computeIfAbsent(
                status.name(),
                name -> {
                    Label newLabel = new Label();
                    serviceList.getChildren().add(newLabel);
                    return newLabel;
                }
        );

        label.setText(status.name() + " — " + status.state());

        if (status.errorMessage() != null) {
            label.setText(label.getText() + ": " + status.errorMessage());
        }

        switch (status.state()) {
            case RUNNING -> label.setStyle("-fx-text-fill: #2ecc71;");
            case STARTING, STOPPING ->
                    label.setStyle("-fx-text-fill: #f39c12;");
            case FAILED -> label.setStyle("-fx-text-fill: #e74c3c;");
            default -> label.setStyle("-fx-text-fill: #888888;");
        }
    }

    @Override
    public void stop() {
        lifecycle.stopAsync().whenComplete((result, error) -> {
            if (error != null) {
                error.printStackTrace();
            }

            lifecycle.close();
        });
    }
}
