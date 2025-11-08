package com.qaldrin.posclient.controller;

import com.qaldrin.posclient.dto.*;
import com.qaldrin.posclient.model.SaleItem;
import com.qaldrin.posclient.service.ApiService;
import com.qaldrin.posclient.service.SaleDataService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

    public Label oldBalanceLabel;
    public Button addwallet;
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

    private BigDecimal walletBalance = BigDecimal.ZERO;
    private BigDecimal walletBalanceUsed = BigDecimal.ZERO;



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

        // Load wallet balance for the customer
        loadWalletBalance(customer.getContact());
    }

    private void loadWalletBalance(String customerContact) {
        // Check if it's a walk-in customer
        if ("WALK-IN".equalsIgnoreCase(customerContact)) {
            Platform.runLater(() -> {
                if (oldBalanceLabel != null) {
                    oldBalanceLabel.setVisible(false);
                    oldBalanceLabel.setManaged(false);
                }
                if (addwallet != null) {
                    addwallet.setVisible(false);
                    addwallet.setManaged(false);
                }
            });
            return;
        }

        // Load wallet in background thread
        new Thread(() -> {
            try {
                BigDecimal balance = apiService.getWalletBalance(customerContact);
                walletBalance = balance;

                Platform.runLater(() -> {
                    if (oldBalanceLabel != null) {
                        if (balance.compareTo(BigDecimal.ZERO) > 0) {
                            oldBalanceLabel.setText(String.format("Wallet Balance: $%.2f", balance));
                            oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

                            // Calculate wallet usage
                            applyWalletBalance();
                        } else {
                            oldBalanceLabel.setText("Wallet Balance: $0.00");
                            oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                        }
                        oldBalanceLabel.setVisible(true);
                        oldBalanceLabel.setManaged(true);
                    }

                    if (addwallet != null) {
                        addwallet.setVisible(true);
                        addwallet.setManaged(true);
                    }
                });

                System.out.println("Wallet balance loaded: $" + balance);

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (oldBalanceLabel != null) {
                        oldBalanceLabel.setText("Wallet Balance: $0.00");
                        oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                    }
                });
                System.err.println("Error loading wallet balance: " + e.getMessage());
            }
        }).start();
    }
    /**
     * Apply wallet balance to reduce payment amount
     */
    private void applyWalletBalance() {
        if (walletBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        SaleDataService dataService = SaleDataService.getInstance();
        BigDecimal total = dataService.getTotal();

        // Calculate how much wallet balance to use
        if (walletBalance.compareTo(total) >= 0) {
            // Wallet covers entire amount
            walletBalanceUsed = total;
            total = BigDecimal.ZERO;
        } else {
            // Wallet covers partial amount
            walletBalanceUsed = walletBalance;
            total = total.subtract(walletBalance);
        }

        // Update payment total label
        if (paymentTotalLabel != null) {
            if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0) {
                paymentTotalLabel.setText(String.format("Total: $%.2f (Wallet: -$%.2f)",
                        total, walletBalanceUsed));
            } else {
                paymentTotalLabel.setText(String.format("Total: $%.2f", total));
            }
        }

        System.out.println("Wallet balance applied - Used: $" + walletBalanceUsed + ", Remaining to pay: $" + total);
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
            SaleDataService dataService = SaleDataService.getInstance();
            BigDecimal originalTotal = dataService.getTotal();

            // Calculate actual amount to pay after wallet deduction
            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);

            if (paidAmount.compareTo(amountToPay) < 0) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Amount",
                        String.format("Paid amount ($%.2f) is less than amount to pay ($%.2f)",
                                paidAmount, amountToPay));
                return;
            }

            processPayment(paidAmount, originalTotal, amountToPay);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                    "Please enter a valid number");
        }
    }

    private void processPayment(BigDecimal paidAmount, BigDecimal originalTotal, BigDecimal amountToPay) {
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
        paymentRequest.setTotalAmount(originalTotal); // Use original total for records
        paymentRequest.setPaidAmount(paidAmount);
        paymentRequest.setChangeAmount(paidAmount.subtract(amountToPay));
        paymentRequest.setPaymentMethod(selectedPaymentMethod);

        new Thread(() -> {
            try {
                // Process payment first
                boolean success = apiService.processPayment(paymentRequest);

                if (success) {
                    // Deduct wallet balance if used
                    if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0 &&
                            !"WALK-IN".equalsIgnoreCase(customer.getContact())) {
                        try {
                            apiService.deductFromWallet(customer.getContact(), walletBalanceUsed);
                            System.out.println("Deducted $" + walletBalanceUsed + " from wallet");
                        } catch (Exception we) {
                            System.err.println("Failed to deduct from wallet: " + we.getMessage());
                            // Continue even if wallet deduction fails
                        }
                    }

                    Platform.runLater(() -> {
                        paymentProcessed = true;
                        updateStockAfterPayment(saleItems);
                        showPaymentSuccess(originalTotal, amountToPay, paidAmount);
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Payment Failed",
                                "Failed to process payment. Please try again.");
                    });
                }

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

    private void showPaymentSuccess(BigDecimal originalTotal, BigDecimal amountToPay, BigDecimal paidAmount) {
        invoiceMessage.setVisible(true);
        invoiceMessage.setManaged(true);

        StringBuilder message = new StringBuilder();
        message.append("Payment processed successfully!\n\n");
        message.append(String.format("Sale ID: %s\n",
                SaleDataService.getInstance().getCurrentCustomer().getSaleId()));
        message.append(String.format("Original Total: $%.2f\n", originalTotal));

        if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            message.append(String.format("Wallet Used: $%.2f\n", walletBalanceUsed));
            message.append(String.format("Amount Paid: $%.2f\n", paidAmount));
        } else {
            message.append(String.format("Paid: $%.2f\n", paidAmount));
        }

        message.append(String.format("Change: $%.2f\n", paidAmount.subtract(amountToPay)));
        message.append(String.format("Payment Method: %s", selectedPaymentMethod));

        showAlert(Alert.AlertType.INFORMATION, "Payment Successful", message.toString());

        SaleDataService.getInstance().clearSaleData();
        walletBalance = BigDecimal.ZERO;
        walletBalanceUsed = BigDecimal.ZERO;
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

    public void addwalletOnClick(ActionEvent actionEvent) {
        try {
            System.out.println("Add Wallet button clicked");

            SaleDataService dataService = SaleDataService.getInstance();
            CustomerDTO customer = dataService.getCurrentCustomer();

            if (customer == null) {
                showAlert(Alert.AlertType.WARNING, "No Customer",
                        "No customer information available.");
                return;
            }

            if ("WALK-IN".equalsIgnoreCase(customer.getContact())) {
                showAlert(Alert.AlertType.INFORMATION, "Not Available",
                        "Wallet feature is not available for walk-in customers.");
                return;
            }

            String paidText = paidTextField != null ? paidTextField.getText().trim() : "";
            if (paidText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Payment Amount",
                        "Please enter the payment amount first.");
                return;
            }

            BigDecimal paidAmount;
            try {
                paidAmount = new BigDecimal(paidText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                        "Please enter a valid payment amount.");
                return;
            }

            BigDecimal originalTotal = dataService.getTotal();
            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);
            BigDecimal changeAmount = paidAmount.subtract(amountToPay);

            if (changeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.INFORMATION, "No Change",
                        "There is no change to add to the wallet.\nChange amount must be greater than zero.");
                return;
            }

            // Add to wallet in background thread
            new Thread(() -> {
                try {
                    WalletDTO updatedWallet = apiService.addToWallet(
                            customer.getContact(), changeAmount);

                    Platform.runLater(() -> {
                        if (updatedWallet != null && updatedWallet.isSuccess()) {
                            if (oldBalanceLabel != null) {
                                oldBalanceLabel.setText(String.format("New Balance: $%.2f",
                                        updatedWallet.getBalance()));
                                oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            }

                            if (addwallet != null) {
                                addwallet.setText("Added ✓");
                                addwallet.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
                                addwallet.setDisable(true);
                            }

                            String successMessage = String.format(
                                    "Change Added to Wallet!\n\n" +
                                            "Customer: %s\n" +
                                            "Old Balance: $%.2f\n" +
                                            "Change Added: $%.2f\n" +
                                            "New Balance: $%.2f\n\n" +
                                            "You can now give the customer $0.00 in cash.",
                                    customer.getContact(),
                                    walletBalance,
                                    changeAmount,
                                    updatedWallet.getBalance()
                            );

                            showAlert(Alert.AlertType.INFORMATION, "Wallet Updated", successMessage);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Failed to add to wallet: " +
                                            (updatedWallet != null ? updatedWallet.getMessage() : "Unknown error"));
                        }
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.err.println("Error adding to wallet: " + e.getMessage());
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to add to wallet: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Error in addwalletOnClick: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to add to wallet: " + e.getMessage());
        }
    }

}