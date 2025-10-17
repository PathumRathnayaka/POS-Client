package com.qaldrin.posclient.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainformController {

    @FXML private TextField pinTextField;
    @FXML private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    @FXML private Button btnClear;
    @FXML private Button btnDelete;
    @FXML private Button loginBtn;
    @FXML private Button getStartedbtn;

    @FXML
    protected void onNumberClicked() {
        // Method for number button clicks
        // You can implement PIN entry logic here
    }

    @FXML
    protected void onClearClicked() {
        // Method for clear button click
        pinTextField.clear();
    }

    @FXML
    protected void onDeleteClicked() {
        // Method for delete button click
        String currentText = pinTextField.getText();
        if (!currentText.isEmpty()) {
            pinTextField.setText(currentText.substring(0, currentText.length() - 1));
        }
    }

    @FXML
    protected void onLoginClicked() {
        // Method for login button click
        // Implement your login validation logic here
    }

    @FXML
    protected void onGetStarted() {
        // Load Dashboard-form.fxml and show it
        try {
            // Load the dashboard FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/qaldrin/posclient/Dashboard-form.fxml")
            );

            AnchorPane dashboardRoot = loader.load();

            // Get the current stage
            Stage currentStage = (Stage) getStartedbtn.getScene().getWindow();

            // Create new scene with dashboard
            Scene dashboardScene = new Scene(dashboardRoot, 1366, 766);

            // Set the new scene
            currentStage.setScene(dashboardScene);
            currentStage.setTitle("POS Dashboard");
            currentStage.show();

            // Optional: Create DashboardFormController instance and initialize
            DashboardFormController dashboardController = loader.getController();
            // You can pass data or initialize dashboard here if needed

        } catch (IOException e) {
            e.printStackTrace();
            // Handle error - show error dialog or log
            System.err.println("Error loading Dashboard: " + e.getMessage());
        }
    }
}