package com.qaldrin.posclient.controller;

import com.jfoenix.controls.JFXButton;
import com.qaldrin.posclient.dto.CustomerDTO;
import com.qaldrin.posclient.service.SaleDataService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardFormController implements Initializable {

    public Button lockBtn;
    public Button pauseCustomer;
    public Button quickSale;
    public JFXButton paymentButton;
    @FXML private AnchorPane primaryScene;
    @FXML private AnchorPane navigationPane;
    @FXML private Label connectionStatusLabel;
    @FXML private Label notificationlabel;

    private DashboardContentController dashboardContentController;

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/qaldrin/posclient/Dashboard-content.fxml"));
            AnchorPane content = loader.load();

            dashboardContentController = loader.getController();

            primaryScene.getChildren().clear();
            primaryScene.getChildren().add(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);

            System.out.println("Successfully loaded Dashboard-content.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Dashboard content");
        }
    }

    /**
     * ADD THIS NEW METHOD - Reload dashboard after payment completion
     */
    public void loadDashboardContentAfterPayment() {
        System.out.println("Reloading dashboard after payment completion...");

        // Clear any existing sale data
        if (dashboardContentController != null) {
            dashboardContentController.clearSale();
        }

        // Reload the dashboard content to start fresh
        loadDashboardContent();

        System.out.println("Dashboard reloaded - ready for new sale");
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load content: " + e.getMessage());
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
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Product management feature is coming soon!");
    }

    @FXML
    private void onSettingButtonClick() {
        System.out.println("Settings clicked");
        loadContent("/com/qaldrin/posclient/setting-form.fxml");
    }

    @FXML
    private void onDeleteButtonClick() {
        System.out.println("Delete clicked");
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Delete feature is coming soon!");
    }

    @FXML
    private void onQuantityButtonClick() {
        System.out.println("Quantity clicked");
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Quantity modification feature is coming soon!");
    }

    @FXML
    private void onNewCustomerButtonClick() {
        System.out.println("New Customer clicked");
        showAddCustomerPopup();

        // After popup closes, check if customer was saved temporarily
        CustomerDTO tempCustomer = AddCustomerFormController.getTempCustomerDTO();
        if (tempCustomer != null) {
            System.out.println("Customer saved temporarily - Sale ID: ");
            SaleDataService.getInstance().setCustomer(tempCustomer);
            showAlert(Alert.AlertType.INFORMATION, "Customer Info Saved",
                    "Customer information saved temporarily!\n\n" +
                            "Sale ID: " +
                            "\nContact: " + tempCustomer.getContact() +
                            "\n\nCustomer will be saved to server when you complete payment.");
        }
    }

    @FXML
    private void onSyncDatabaseClick() {
        System.out.println("Sync Database clicked");
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Database sync feature is coming soon!");
    }

    @FXML
    private void onPaymentButtonClick() {
        System.out.println("Payment clicked");

        if (dashboardContentController == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Dashboard not loaded properly!");
            return;
        }

        if (dashboardContentController.getSaleItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Items", "Please add items to the sale before proceeding to payment!");
            return;
        }

        // Check if customer info exists in temp storage
        CustomerDTO tempCustomer = AddCustomerFormController.getTempCustomerDTO();
        if (tempCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "No Customer",
                    "Please add customer information before proceeding to payment!");
            return;
        }

        // Store sale data in SaleDataService
        SaleDataService.getInstance().setSaleData(
                dashboardContentController.getSaleItems(),
                dashboardContentController.getSubtotal(),
                dashboardContentController.getTax(),
                dashboardContentController.getTotal()
        );

        // Also ensure customer is stored
        SaleDataService.getInstance().setCustomer(tempCustomer);

        System.out.println("Proceeding to payment with customer: ");

        // Load payment form and call loadSaleData()
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/qaldrin/posclient/Payment-form.fxml"));
            AnchorPane content = loader.load();

            // Get the controller and call loadSaleData()
            PaymentFormController paymentController = loader.getController();
            if (paymentController != null) {
                // IMPORTANT: Pass this controller reference to payment controller
                paymentController.setDashboardFormController(this);

                paymentController.loadSaleData();
                System.out.println("Called loadSaleData() on PaymentFormController");
            }

            // Clear existing content
            primaryScene.getChildren().clear();

            // Add new content and anchor it to fill the parent
            primaryScene.getChildren().add(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);

            System.out.println("Payment form loaded successfully");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading payment form: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load payment form: " + e.getMessage());
        }
    }

    private void showAddCustomerPopup() {
        try {
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
            popupStage.initStyle(StageStyle.UNDECORATED);

            // Center the popup on screen
            popupStage.centerOnScreen();

            // Optional: Add custom window styling
            try {
                URL cssUrl = getClass().getResource("/styles/main.css");
                if (cssUrl != null) {
                    popupStage.getScene().getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("CSS file not found, skipping styling");
            }

            // Show the popup and wait for it to close
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading AddCustomer-form.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to open Add Customer form\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void lockOnClick(ActionEvent actionEvent) {

    }

    public void pauseCustomerOnClick(ActionEvent actionEvent) {

    }

    @FXML
    public void quickSaleOnClick(ActionEvent actionEvent) {
        System.out.println("Quick Sale clicked");

        if (dashboardContentController == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Dashboard not loaded properly!");
            return;
        }

        // 1️⃣ Ensure items exist
        if (dashboardContentController.getSaleItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Items", "Please add items before processing Quick Sale!");
            return;
        }

        // 2️⃣ Generate a unique sale ID for walk-in customer
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String quickSaleId = "SALE-" + timestamp;

        System.out.println("Generated Quick Sale ID: " + quickSaleId);

        // 3️⃣ Create a temporary Walk-in customer
        CustomerDTO walkInCustomer = new CustomerDTO();
        walkInCustomer.setContact("WALK-IN");
        walkInCustomer.setEmail("");

        // 4️⃣ ✅ CRITICAL: Store BOTH customer AND sale ID in temp storage
        AddCustomerFormController.setTempCustomerDTO(walkInCustomer, quickSaleId);

        System.out.println("Walk-in customer stored with Sale ID: " + quickSaleId);

        // 5️⃣ Store walk-in sale data in SaleDataService
        SaleDataService.getInstance().setSaleData(
                dashboardContentController.getSaleItems(),
                dashboardContentController.getSubtotal(),
                dashboardContentController.getTax(),
                dashboardContentController.getTotal()
        );
        SaleDataService.getInstance().setCustomer(walkInCustomer);

        // 6️⃣ Load payment form
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/qaldrin/posclient/Payment-form.fxml"));
            AnchorPane content = loader.load();

            PaymentFormController paymentController = loader.getController();
            if (paymentController != null) {
                paymentController.setDashboardFormController(this);

                // Load the sale data
                paymentController.loadSaleData();

                // Disable wallet features for quick sale
                paymentController.disableWalletForWalkIn();

                System.out.println("Quick Sale PaymentForm initialized for Walk-in.");
            }

            // Replace dashboard with payment form
            primaryScene.getChildren().clear();
            primaryScene.getChildren().add(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);

            System.out.println("Quick Sale Payment Form loaded successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Quick Sale payment form: " + e.getMessage());
        }
    }

}