
package app.triggerloom;

import app.triggerloom.core.ApplicationController;

import javafx.application.Application;
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

        BorderPane root = new BorderPane();
        root.setCenter(statusLabel);

        Scene scene = new Scene(root, 1000, 650);

        stage.setTitle("Triggerloom");
        stage.setScene(scene);
        stage.show();

        try {
            controller.start();
            updateStatus();
        } catch (Exception exception) {
            statusLabel.setText(
                    "Startup failed: " + exception.getMessage()
            );
        }
    }

    private void updateStatus() {
        statusLabel.setText(
                "Application status: " + controller.getState()
        );
    }

    @Override
    public void stop() throws Exception {
        controller.stop();
    }
}
