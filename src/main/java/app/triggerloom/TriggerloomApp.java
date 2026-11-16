
package app.triggerloom;

import app.triggerloom.core.ApplicationController;
import app.triggerloom.core.ApplicationState;
import app.triggerloom.core.DemoService;
import app.triggerloom.core.ServiceStatus;

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

    private final Label applicationStatus = new Label();
    private final VBox serviceList = new VBox(10);

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
                Platform.runLater(this::refreshServiceList)
        );

        controller.registerService(new DemoService());

        updateApplicationStatus(controller.getState());
        refreshServiceList();

        stage.show();

        try {
            controller.start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void updateApplicationStatus(ApplicationState state) {
        applicationStatus.setText(
                "Application status: " + state
        );
    }

    private void refreshServiceList() {
        serviceList.getChildren().clear();

        for (ServiceStatus status : controller.getServiceStatuses()) {
            Label label = new Label(
                    status.name() + " — " + status.state()
            );

            if (status.errorMessage() != null) {
                label.setText(
                        label.getText() + ": " + status.errorMessage()
                );
            }

            serviceList.getChildren().add(label);
        }
    }

    @Override
    public void stop() throws Exception {
        if (controller.getState() == ApplicationState.RUNNING) {
            controller.stop();
        }
    }
}
