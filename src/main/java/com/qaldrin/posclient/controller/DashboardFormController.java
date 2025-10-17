package com.qaldrin.posclient.controller;

import com.qaldrin.posclient.dto.CustomerDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardFormController implements Initializable {

    @FXML private AnchorPane primaryScene;
    @FXML private AnchorPane navigationPane;
    @FXML private Label connectionStatusLabel;
    @FXML private Label notificationlabel;

    // Navigation buttons
    @FXML private Button dashboardButton;
    @FXML private Button productButton;
    @FXML private Button settingButton;
    @FXML private Button deleteButton;
    @FXML private Button quantityButton;
    @FXML private Button syncDatabaseButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Automatically load Dashboard content when the form loads
        loadDashboardContent();
    }

    /**
     * Load Dashboard-content.fxml into primaryScene
     */
    private void loadDashboardContent() {
        loadContent("/com/qaldrin/posclient/Dashboard-content.fxml");
    }

    /**
     * Generic method to load FXML content into primaryScene
     */
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane content = loader.load();

            // Clear existing content
            primaryScene.getChildren().clear();

            // Add new content and anchor it to fill the parent
            primaryScene.getChildren().add(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);

            System.out.println("Successfully loaded: " + fxmlPath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading content from: " + fxmlPath);
        }
    }

    @FXML
    private void onDashboardButtonClick() {
        System.out.println("Dashboard clicked");
        loadDashboardContent();
    }

    @FXML
    private void onProductClick() {
        System.out.println("Product clicked");
        // TODO: Load product content when available
        // loadContent("/com/qaldrin/posclient/Product-content.fxml");
    }

    @FXML
    private void onSettingButtonClick() {
        System.out.println("Settings clicked");
        // TODO: Load settings content when available
        // loadContent("/com/qaldrin/posclient/Settings-content.fxml");
    }

    @FXML
    private void onDeleteButtonClick() {
        System.out.println("Delete clicked");
        // Handle delete action for selected item in current view
    }

    @FXML
    private void onQuantityButtonClick() {
        System.out.println("Quantity clicked");
        // Handle quantity modification for selected item
    }

    @FXML
    private void onNewCustomerButtonClick() {
        System.out.println("New Customer clicked");
        showAddCustomerPopup();
    }

    @FXML
    private void onSyncDatabaseClick() {
        System.out.println("Sync Database clicked");
        // Handle database synchronization
    }

    @FXML
    private void onPaymentButtonClick() {
        System.out.println("Payment clicked");
        loadContent("/com/qaldrin/posclient/Payment-form.fxml");
    }

    private void showAddCustomerPopup() {
        try {
            // FIXED: Correct file name with hyphen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/qaldrin/posclient/AddCustomer-form.fxml"));
            AnchorPane root = loader.load();

            // Get the controller instance
            AddCustomerFormController controller = loader.getController();

            // Create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.setTitle("New Customer - Sale Information");
            popupStage.setScene(new Scene(root, 671, 447));

            // Set modality to block interaction with parent window
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED); // Optional: Remove default window decorations

            // Center the popup on screen
            popupStage.centerOnScreen();

            // Optional: Add custom window styling (check if CSS exists first)
            try {
                URL cssUrl = getClass().getResource("/styles/main.css");
                if (cssUrl != null) {
                    popupStage.getScene().getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("CSS file not found, skipping styling");
            }

            // Show the popup
            popupStage.showAndWait(); // Wait until popup is closed

            // Check if customer was saved
            CustomerDTO savedCustomer = controller.getSavedCustomer();
            if (savedCustomer != null) {
                System.out.println("Customer saved with Sale ID: " + savedCustomer.getSaleId());
                // You can pass this information back to the dashboard if needed
            } else {
                System.out.println("Add Customer popup closed without saving");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading AddCustomer-form.fxml: " + e.getMessage());

            // Show error alert to user
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open Add Customer form");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
}