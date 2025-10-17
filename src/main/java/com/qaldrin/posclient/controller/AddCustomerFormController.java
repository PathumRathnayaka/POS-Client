package com.qaldrin.posclient.controller;

import com.jfoenix.controls.JFXButton;
import com.qaldrin.posclient.dto.CustomerDTO;
import com.qaldrin.posclient.service.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private final ApiService apiService = new ApiService();
    private CustomerDTO savedCustomer; // Store the saved customer

    @FXML
    public void initialize() {
        // Auto-generate initial sale ID
        generateSaleId();
    }

    /**
     * Generate a unique sale ID using timestamp
     */
    private void generateSaleId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String saleId = "SALE-" + timestamp;
        saleIdField.setText(saleId);
    }

    @FXML
    private void onRegenerateSaleId() {
        generateSaleId();
        System.out.println("Sale ID regenerated: " + saleIdField.getText());
    }

    @FXML
    private void onSaveCustomer() {
        // Validate input
        String saleId = saleIdField.getText().trim();
        String contact = customerContactField.getText().trim();
        String email = customerEmailField.getText().trim();

        if (saleId.isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Sale ID cannot be empty!");
            return;
        }

        if (contact.isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Customer contact is required!");
            return;
        }

        // Validate contact (basic phone number validation)
        if (!contact.matches("\\d{10,15}")) {
            showAlert(AlertType.WARNING, "Validation Error", "Please enter a valid phone number (10-15 digits)!");
            return;
        }

        // Validate email if provided
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(AlertType.WARNING, "Validation Error", "Please enter a valid email address!");
            return;
        }

        // Disable buttons during save
        saveButton.setDisable(true);
        cancelButton.setDisable(true);

        // Create customer DTO
        CustomerDTO customer = new CustomerDTO(saleId, contact, email.isEmpty() ? null : email);

        // Save customer in background thread
        new Thread(() -> {
            try {
                CustomerDTO savedCustomerDTO = apiService.saveCustomer(customer);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    this.savedCustomer = savedCustomerDTO;
                    System.out.println("Customer saved successfully: " + savedCustomerDTO);

                    showAlert(AlertType.INFORMATION, "Success",
                            "Customer saved successfully!\n\nSale ID: " + savedCustomerDTO.getSaleId() +
                                    "\nContact: " + savedCustomerDTO.getContact());

                    // Close the dialog
                    getStage().close();
                });

            } catch (Exception e) {
                // Handle error on JavaFX thread
                Platform.runLater(() -> {
                    System.err.println("Failed to save customer: " + e.getMessage());
                    e.printStackTrace();

                    showAlert(AlertType.ERROR, "Error",
                            "Failed to save customer!\n\n" +
                                    "Error: " + e.getMessage() +
                                    "\n\nPlease check:\n" +
                                    "1. Server is running\n" +
                                    "2. Network connection is active\n" +
                                    "3. Server IP address is correct in ApiConfig");

                    // Re-enable buttons
                    saveButton.setDisable(false);
                    cancelButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void onCancelCustomer() {
        // Confirm cancellation
        boolean confirmed = showConfirmation("Cancel",
                "Are you sure you want to cancel?\nAll entered data will be lost.");

        if (confirmed) {
            getStage().close();
        }
    }

    /**
     * Get the saved customer (null if not saved yet)
     */
    public CustomerDTO getSavedCustomer() {
        return savedCustomer;
    }

    /**
     * Get the current stage
     */
    private Stage getStage() {
        if (stage == null) {
            stage = (Stage) saveButton.getScene().getWindow();
        }
        return stage;
    }

    /**
     * Show alert dialog
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(getStage());
        alert.showAndWait();
    }

    /**
     * Show confirmation dialog
     */
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(getStage());

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}