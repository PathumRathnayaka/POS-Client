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

    // ✅ Store BOTH customer AND sale ID separately
    private static CustomerDTO tempCustomerDTO;
    private static String tempSaleId;

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

        // ✅ Create customer DTO WITHOUT sale ID
        tempCustomerDTO = new CustomerDTO(contact, email.isEmpty() ? null : email);

        // ✅ Store sale ID separately
        tempSaleId = saleId;

        System.out.println("Customer saved TEMPORARILY: " + tempCustomerDTO);
        System.out.println("Sale ID saved TEMPORARILY: " + tempSaleId);

        showAlert(AlertType.INFORMATION, "Success",
                "Customer information saved temporarily!\n\n" +
                        "Sale ID: " + saleId +
                        "\nContact: " + contact +
                        "\n\nCustomer will be saved to server when payment is completed.");

        // Close the dialog
        getStage().close();
    }

    @FXML
    private void onCancelCustomer() {
        boolean confirmed = showConfirmation("Cancel",
                "Are you sure you want to cancel?\nAll entered data will be lost.");

        if (confirmed) {
            getStage().close();
        }
    }

    /**
     * Get the temporarily saved customer (null if not saved yet)
     */
    public static CustomerDTO getTempCustomerDTO() {
        return tempCustomerDTO;
    }

    /**
     * Get the temporarily saved sale ID
     */
    public static String getTempSaleId() {
        return tempSaleId;
    }

    /**
     * Clear the temporary data after payment is completed
     */
    public static void clearTempCustomerDTO() {
        tempCustomerDTO = null;
        tempSaleId = null;
        System.out.println("Temporary customer and sale data cleared");
    }

    /**
     * Set temporary customer and sale ID (used when resuming paused sales)
     */
    public static void setTempCustomerDTO(CustomerDTO customerDTO, String saleId) {
        tempCustomerDTO = customerDTO;
        tempSaleId = saleId;
        System.out.println("Customer data set in temp storage - Contact: " +
                (customerDTO != null ? customerDTO.getContact() : "null") +
                ", Sale ID: " + saleId);
    }

    private Stage getStage() {
        if (stage == null) {
            stage = (Stage) saveButton.getScene().getWindow();
        }
        return stage;
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(getStage());
        alert.showAndWait();
    }

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