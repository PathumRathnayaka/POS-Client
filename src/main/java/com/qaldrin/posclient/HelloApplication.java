package com.qaldrin.posclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Get the resource URL explicitly
            URL fxmlLocation = HelloApplication.class.getResource("main-form.fxml");
            if (fxmlLocation == null) {
                throw new IOException("Cannot find main-form.fxml in resources. Check file location.");
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            stage.setTitle("POS System Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load FXML", e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}