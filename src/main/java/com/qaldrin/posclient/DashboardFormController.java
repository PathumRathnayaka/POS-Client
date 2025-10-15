package com.qaldrin.posclient;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;


public class DashboardFormController {

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


    @FXML
    private void onDashboardButtonClick() {
        // Load dashboard content into primaryScene
        System.out.println("Dashboard clicked");
    }

    @FXML
    private void onProductClick() {
        // Load product content into primaryScene
        System.out.println("Product clicked");
    }

    @FXML
    private void onSettingButtonClick() {
        // Load settings content into primaryScene
        System.out.println("Settings clicked");
    }

    @FXML
    private void onDeleteButtonClick() {
        // Handle delete action
        System.out.println("Delete clicked");
    }

    @FXML
    private void onQuantityButtonClick() {
        // Handle quantity action
        System.out.println("Quantity clicked");
    }

    @FXML
    private void onNewCustomerButtonClick() {
        // Handle new customer action
        System.out.println("New Customer clicked");
    }

    @FXML
    private void onSyncDatabaseClick() {
        // Handle database sync
        System.out.println("Sync Database clicked");
    }

    @FXML
    private void onPaymentButtonClick() {
        // Handle payment
        System.out.println("Payment clicked");
    }
}