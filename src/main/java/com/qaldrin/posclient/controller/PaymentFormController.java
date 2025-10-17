package com.qaldrin.posclient.controller;

import com.qaldrin.posclient.dto.CustomerDTO;
import com.qaldrin.posclient.dto.PaymentRequestDTO;
import com.qaldrin.posclient.dto.PaymentResponseDTO;
import com.qaldrin.posclient.dto.SaleItemDTO;
import com.qaldrin.posclient.model.SaleItem;
import com.qaldrin.posclient.service.ApiService;
import com.qaldrin.posclient.service.SaleDataService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentFormController implements Initializable {

    @FXML private AnchorPane primaryScene;
    @FXML private VBox itemsVBox;
    @FXML private AnchorPane invoiceMessage;

    @FXML private Button cashButton;
    @FXML private Button cardButton;
    @FXML private Button checkButton;

    private final ApiService apiService = new ApiService();
    private String selectedPaymentMethod = null;
    private boolean paymentProcessed = false;
    @FXML private Label subTotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label paymentTotalLabel;
    @FXML private Label changeLabel;

    // Customer Info
    @FXML private Label saleIdLabel;
    @FXML private Label customerContactLabel;

    // Input Fields
    @FXML private TextField paidTextField;

    // Invoice Buttons
    @FXML private Button printInvoiceButton;
    @FXML private Button emailInvoiceButton;
    @FXML private Button pdfInvoiceButton;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeUI();
        loadSaleData();
        setupPaidTextFieldListener();
    }

    private void initializeUI() {
        invoiceMessage.setVisible(false);
        invoiceMessage.setManaged(false);
        paidTextField.setDisable(true);
    }

    private void loadSaleData() {
        SaleDataService dataService = SaleDataService.getInstance();
        CustomerDTO customer = dataService.getCurrentCustomer();
        ObservableList<SaleItem> saleItems = dataService.getCurrentSaleItems();

        if (customer == null || saleItems == null || saleItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No sale data available!");
            return;
        }

        saleIdLabel.setText(customer.getSaleId());
        customerContactLabel.setText(customer.getContact());

        displaySaleItems(saleItems);

        BigDecimal subtotal = dataService.getSubtotal();
        BigDecimal tax = dataService.getTax();
        BigDecimal total = dataService.getTotal();

        subTotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        totalLabel.setText(String.format("$%.2f", total));
        paymentTotalLabel.setText(String.format("$%.2f", total));
        changeLabel.setText("$0.00");
    }

    private void displaySaleItems(ObservableList<SaleItem> saleItems) {
        itemsVBox.getChildren().clear();

        for (SaleItem item : saleItems) {
            HBox itemRow = new HBox(10);
            itemRow.setPadding(new Insets(5));
            itemRow.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

            Label nameLabel = new Label(item.getName());
            nameLabel.setPrefWidth(200);
            nameLabel.setStyle("-fx-font-weight: bold;");

            Label qtyLabel = new Label("Qty: " + item.getQuantity());
            qtyLabel.setPrefWidth(80);

            Label priceLabel = new Label(String.format("$%.2f", item.getSalePrice()));
            priceLabel.setPrefWidth(80);

            Label amountLabel = new Label(String.format("$%.2f", item.getAmount()));
            amountLabel.setPrefWidth(80);
            amountLabel.setStyle("-fx-font-weight: bold;");

            itemRow.getChildren().addAll(nameLabel, qtyLabel, priceLabel, amountLabel);
            itemsVBox.getChildren().add(itemRow);
        }
    }

    private void setupPaidTextFieldListener() {
        paidTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                paidTextField.setText(oldValue);
            } else {
                calculateChange();
            }
        });
    }

    @FXML
    private void onCancel() {
        boolean confirm = showConfirmation("Cancel Payment",
                "Are you sure you want to cancel this payment?");
        if (confirm) {
            System.out.println("Payment cancelled by user");
        }
    }

    @FXML
    private void onDiscount() {
        showAlert(Alert.AlertType.INFORMATION, "Discount", "Discount feature coming soon!");
    }

    @FXML
    private void onTaxes() {
        showAlert(Alert.AlertType.INFORMATION, "Taxes", "Tax is already calculated (10%)");
    }

    @FXML
    private void onCashPayment() {
        selectPaymentMethod("Cash");
    }

    @FXML
    private void onCheckPayment() {
        selectPaymentMethod("Check");
    }

    @FXML
    private void onCardPayment() {
        selectPaymentMethod("Card");
    }

    // Invoice Button Handlers
    @FXML
    private void onPrintInvoice() {
        // TODO: Implement invoice printing
    }

    @FXML
    private void onEmailInvoice() {
        // TODO: Implement invoice emailing
    }

    @FXML
    private void onPdfInvoice() {
        // TODO: Implement PDF invoice generation
    }

    @FXML
    private void onDone() {
        if (paymentProcessed) {
            showAlert(Alert.AlertType.WARNING, "Already Processed",
                    "This payment has already been processed!");
            return;
        }

        if (selectedPaymentMethod == null) {
            showAlert(Alert.AlertType.WARNING, "Payment Method Required",
                    "Please select a payment method (Cash or Card)");
            return;
        }

        String paidText = paidTextField.getText().trim();
        if (paidText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Amount Required",
                    "Please enter the paid amount");
            return;
        }

        try {
            BigDecimal paidAmount = new BigDecimal(paidText);
            BigDecimal totalAmount = SaleDataService.getInstance().getTotal();

            if (paidAmount.compareTo(totalAmount) < 0) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Amount",
                        String.format("Paid amount ($%.2f) is less than total ($%.2f)",
                                paidAmount, totalAmount));
                return;
            }

            processPayment(paidAmount);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                    "Please enter a valid number");
        }
    }

    private void processPayment(BigDecimal paidAmount) {
        SaleDataService dataService = SaleDataService.getInstance();
        CustomerDTO customer = dataService.getCurrentCustomer();
        ObservableList<SaleItem> saleItems = dataService.getCurrentSaleItems();

        List<SaleItemDTO> saleItemDTOs = new ArrayList<>();
        for (SaleItem item : saleItems) {
            SaleItemDTO dto = new SaleItemDTO();
            dto.setProductId(item.getId());
            dto.setProductName(item.getName());
            dto.setBarcode(item.getBarcode());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getSalePrice());
            dto.setTotalPrice(item.getAmount());
            saleItemDTOs.add(dto);
        }

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
        paymentRequest.setSaleId(customer.getSaleId());
        paymentRequest.setCustomerContact(customer.getContact());
        paymentRequest.setCustomerEmail(customer.getEmail());
        paymentRequest.setSaleItems(saleItemDTOs);
        paymentRequest.setSubTotal(dataService.getSubtotal());
        paymentRequest.setTaxAmount(dataService.getTax());
        paymentRequest.setDiscountAmount(BigDecimal.ZERO);
        paymentRequest.setTotalAmount(dataService.getTotal());
        paymentRequest.setPaidAmount(paidAmount);
        paymentRequest.setChangeAmount(paidAmount.subtract(dataService.getTotal()));
        paymentRequest.setPaymentMethod(selectedPaymentMethod);

        new Thread(() -> {
            try {
                boolean success = apiService.processPayment(paymentRequest);

                Platform.runLater(() -> {
                    if (success) {
                        paymentProcessed = true;
                        updateStockAfterPayment(saleItems);
                        showPaymentSuccess();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Payment Failed",
                                "Failed to process payment. Please try again.");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Payment error: " + e.getMessage());
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Payment Error",
                            "Error processing payment: " + e.getMessage() +
                                    "\n\nPlease check server connection.");
                });
            }
        }).start();
    }

    private void updateStockAfterPayment(ObservableList<SaleItem> saleItems) {
        new Thread(() -> {
            try {
                List<ApiService.StockUpdateItem> stockUpdates = new ArrayList<>();
                for (SaleItem item : saleItems) {
                    stockUpdates.add(new ApiService.StockUpdateItem(item.getId(), item.getQuantity()));
                }

                apiService.updateStock(stockUpdates);
                System.out.println("Stock updated successfully");

            } catch (Exception e) {
                System.err.println("Stock update error: " + e.getMessage());
            }
        }).start();
    }

    private void showPaymentSuccess() {
        invoiceMessage.setVisible(true);
        invoiceMessage.setManaged(true);

        showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                String.format("Payment processed successfully!\n\n" +
                                "Sale ID: %s\n" +
                                "Total: $%.2f\n" +
                                "Paid: $%.2f\n" +
                                "Change: $%.2f\n" +
                                "Payment Method: %s",
                        SaleDataService.getInstance().getCurrentCustomer().getSaleId(),
                        SaleDataService.getInstance().getTotal(),
                        new BigDecimal(paidTextField.getText()),
                        new BigDecimal(paidTextField.getText()).subtract(SaleDataService.getInstance().getTotal()),
                        selectedPaymentMethod));

        SaleDataService.getInstance().clearSaleData();
    }

    // Utility Methods
    public void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        System.out.println("Payment method selected: " + method);

        if (cashButton != null) cashButton.setStyle("");
        if (cardButton != null) cardButton.setStyle("");
        if (checkButton != null) checkButton.setStyle("");

        String selectedStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white;";
        if ("Cash".equals(method) && cashButton != null) {
            cashButton.setStyle(selectedStyle);
        } else if ("Card".equals(method) && cardButton != null) {
            cardButton.setStyle(selectedStyle);
        } else if ("Check".equals(method) && checkButton != null) {
            checkButton.setStyle(selectedStyle);
        }

        paidTextField.setDisable(false);
        paidTextField.requestFocus();
    }


    public void calculateChange() {
        try {
            String paidText = paidTextField.getText().trim();
            if (paidText.isEmpty()) {
                changeLabel.setText("$0.00");
                return;
            }

            BigDecimal paid = new BigDecimal(paidText);
            BigDecimal total = SaleDataService.getInstance().getTotal();
            BigDecimal change = paid.subtract(total);

            if (change.compareTo(BigDecimal.ZERO) < 0) {
                changeLabel.setText("$0.00");
                changeLabel.setStyle("-fx-text-fill: red;");
            } else {
                changeLabel.setText(String.format("$%.2f", change));
                changeLabel.setStyle("-fx-text-fill: green;");
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("$0.00");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
}