package com.qaldrin.posclient.controller;

import com.qaldrin.posclient.service.ApiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainformController {

    @FXML
    private TextField pinTextField;
    @FXML
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnDelete;
    @FXML
    private Button loginBtn;
    @FXML
    private Button getStartedbtn;

    private final ApiService apiService = new ApiService();

    @FXML
    public void initialize() {
        // Check if server has a PIN set
        new Thread(() -> {
            try {
                boolean hasPin = apiService.hasPin();
                Platform.runLater(() -> {
                    if (hasPin) {
                        getStartedbtn.setDisable(true);
                        System.out.println("PIN is required, 'Get Started' disabled");
                    } else {
                        getStartedbtn.setDisable(false);
                        System.out.println("No PIN required, 'Get Started' enabled");
                    }
                });
            } catch (IOException e) {
                System.err.println("Error checking PIN status: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    protected void onNumberClicked(ActionEvent event) {
        Button btn = (Button) event.getSource();
        pinTextField.appendText(btn.getText());
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
        String pin = pinTextField.getText().trim();
        if (pin.isEmpty()) {
            showAlert("Error", "Please enter your special PIN");
            return;
        }

        loginBtn.setDisable(true);
        new Thread(() -> {
            try {
                boolean isValid = apiService.validatePin(pin);
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    if (isValid) {
                        System.out.println("Login successful");
                        onGetStarted();
                    } else {
                        showAlert("Invalid PIN", "The PIN you entered is incorrect. Please try again.");
                        pinTextField.clear();
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    showAlert("Connection Error", "Could not connect to server: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    protected void onGetStarted() {
        // Load Dashboard-form.fxml and show it
        try {
            // Load the dashboard FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/qaldrin/posclient/Dashboard-form.fxml"));

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