package com.qaldrin.posclient.controller;


import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class AddCustomerFormController {

    @FXML
    private TextField saleIdField;

    @FXML
    private Button regenerateButton;

    @FXML
    private TextField customerContactField;

    @FXML
    private TextField customerEmailField;

    @FXML
    private JFXButton saveButton;

    @FXML
    private JFXButton cancelButton;

    private Stage stage;

    @FXML
    public void initialize() {
        // Store reference to the stage
        stage = (Stage) saveButton.getScene().getWindow();

        // Auto-generate initial sale ID
        generateSaleId();
    }

    private void generateSaleId() {
        // TODO: Implement actual sale ID generation logic
        String saleId = "SALE-" + System.currentTimeMillis();
        saleIdField.setText(saleId);
    }

    @FXML
    private void onRegenerateSaleId() {
        generateSaleId();
        System.out.println("Sale ID regenerated");
    }

    @FXML
    private void onSaveCustomer() {
        // TODO: Implement save logic
        String saleId = saleIdField.getText();
        String contact = customerContactField.getText();
        String email = customerEmailField.getText();

        System.out.println("Saving customer - SaleID: " + saleId + ", Contact: " + contact + ", Email: " + email);

        // Show success message
        showAlert(AlertType.INFORMATION, "Success", "Customer information saved successfully!");

        // Close the popup
        stage.close();
    }

    @FXML
    private void onCancelCustomer() {
        // Confirm cancellation
        boolean confirmed = showConfirmation("Cancel", "Are you sure you want to cancel? All entered data will be lost.");
        if (confirmed) {
            stage.close();
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(stage);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}