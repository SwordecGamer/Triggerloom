package app.triggerloom;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TriggerloomApp extends Application {

    @Override
    public void start(Stage stage) {
        Label welcome = new Label("Welcome to Triggerloom!");

        StackPane root = new StackPane(welcome);
        Scene scene = new Scene(root, 1000, 650);

        stage.setTitle("Triggerloom");
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.setScene(scene);

        stage.show();
    }
}
