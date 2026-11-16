
package app.triggerloom;

import app.triggerloom.core.ApplicationController;
import app.triggerloom.core.ApplicationState;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TriggerloomApp extends Application {

    private final ApplicationController controller =
            new ApplicationController();

    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        statusLabel = new Label();

        controller.addStateListener((previous, current) ->
                Platform.runLater(() -> updateStatus(current))
        );

        BorderPane root = new BorderPane();
        root.setCenter(statusLabel);

        Scene scene = new Scene(root, 1000, 650);

        stage.setTitle("Triggerloom");
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.setScene(scene);

        updateStatus(controller.getState());
        stage.show();

        try {
            controller.start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void updateStatus(ApplicationState state) {
        statusLabel.setText("Application status: " + state);
    }

    @Override
    public void stop() throws Exception {
        if (controller.getState() == ApplicationState.RUNNING) {
            controller.stop();
        }
    }
}
