package com.qaldrin.posclient.controller;

import com.jfoenix.controls.JFXButton;
import com.qaldrin.posclient.dto.*;
import com.qaldrin.posclient.model.SaleItem;
import com.qaldrin.posclient.service.ApiService;
import com.qaldrin.posclient.service.SaleDataService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
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

    // Reference to parent controller - ADD THIS
    private DashboardFormController dashboardFormController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PaymentFormController initializing...");
        initializeUI();
        setupPaidTextFieldListener();
    }

    // ADD THIS METHOD
    public void setDashboardFormController(DashboardFormController controller) {
        this.dashboardFormController = controller;
    }

    private void initializeUI() {
        if (invoiceMessage != null) {
            invoiceMessage.setVisible(false);
            invoiceMessage.setManaged(false);
        }
        if (paidTextField != null) {
            paidTextField.setDisable(true);
        }
        System.out.println("UI initialized");
    }

    public void loadSaleData() {
        System.out.println("Loading sale data...");

        SaleDataService dataService = SaleDataService.getInstance();
        CustomerDTO customer = AddCustomerFormController.getTempCustomerDTO();
        ObservableList<SaleItem> saleItems = dataService.getCurrentSaleItems();

        System.out.println("Customer from temp: " + (customer != null ? customer.getContact() : "null"));
        System.out.println("Sale items count: " + (saleItems != null ? saleItems.size() : 0));

        if (customer == null || saleItems == null || saleItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No sale data available!");
            return;
        }

        if (saleIdLabel != null) {
            saleIdLabel.setText(customer.getSaleId());
            System.out.println("Set sale ID: " + customer.getSaleId());
        }
        if (customerContactLabel != null) {
            customerContactLabel.setText(customer.getContact());
            System.out.println("Set customer contact: " + customer.getContact());
        }

        displaySaleItems(saleItems);

        BigDecimal subtotal = dataService.getSubtotal();
        BigDecimal tax = dataService.getTax();
        BigDecimal total = dataService.getTotal();

        System.out.println("Subtotal: " + subtotal + ", Tax: " + tax + ", Total: " + total);

        if (subTotalLabel != null) {
            subTotalLabel.setText(String.format("$%.2f", subtotal));
        }
        if (taxLabel != null) {
            taxLabel.setText(String.format("$%.2f", tax));
        }
        if (totalLabel != null) {
            totalLabel.setText(String.format("$%.2f", total));
        }
        if (paymentTotalLabel != null) {
            paymentTotalLabel.setText(String.format("$%.2f", total));
        }
        if (changeLabel != null) {
            changeLabel.setText("$0.00");
        }

        System.out.println("About to load wallet for: " + customer.getContact());
        loadWalletBalance(customer.getContact());
    }

    private void loadWalletBalance(String customerContact) {
        System.out.println("loadWalletBalance called for: " + customerContact);

        if ("WALK-IN".equalsIgnoreCase(customerContact)) {
            System.out.println("Walk-in customer detected - hiding wallet UI");
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

        System.out.println("Loading wallet balance from server for: " + customerContact);

        new Thread(() -> {
            try {
                System.out.println("Calling API getWalletBalance for: " + customerContact);
                BigDecimal balance = apiService.getWalletBalance(customerContact);
                walletBalance = balance;

                System.out.println("Wallet balance received: $" + balance);

                Platform.runLater(() -> {
                    System.out.println("Updating UI with wallet balance: $" + balance);

                    if (oldBalanceLabel != null) {
                        if (balance.compareTo(BigDecimal.ZERO) > 0) {
                            oldBalanceLabel.setText(String.format("Wallet Balance: $%.2f", balance));
                            oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            System.out.println("Set oldBalanceLabel text to: Wallet Balance: $" + balance);
                            applyWalletBalance();
                        } else {
                            oldBalanceLabel.setText("Wallet Balance: $0.00");
                            oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                            System.out.println("Set oldBalanceLabel to zero balance");
                        }
                        oldBalanceLabel.setVisible(true);
                        oldBalanceLabel.setManaged(true);
                        System.out.println("oldBalanceLabel made visible");
                    } else {
                        System.err.println("ERROR: oldBalanceLabel is NULL!");
                    }

                    if (addwallet != null) {
                        addwallet.setVisible(true);
                        addwallet.setManaged(true);
                        System.out.println("addwallet button made visible");
                    } else {
                        System.err.println("ERROR: addwallet button is NULL!");
                    }
                });

                System.out.println("Wallet balance loaded successfully: $" + balance);

            } catch (Exception e) {
                System.err.println("Error loading wallet balance: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    if (oldBalanceLabel != null) {
                        oldBalanceLabel.setText("Wallet Balance: $0.00");
                        oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                        oldBalanceLabel.setVisible(true);
                        oldBalanceLabel.setManaged(true);
                    }
                    if (addwallet != null) {
                        addwallet.setVisible(true);
                        addwallet.setManaged(true);
                    }
                });
            }
        }).start();
    }

    private void applyWalletBalance() {
        System.out.println("applyWalletBalance called - Wallet balance: $" + walletBalance);

        if (walletBalance.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("No wallet balance to apply");
            return;
        }

        SaleDataService dataService = SaleDataService.getInstance();
        BigDecimal total = dataService.getTotal();

        System.out.println("Original total: $" + total);

        if (walletBalance.compareTo(total) >= 0) {
            walletBalanceUsed = total;
            total = BigDecimal.ZERO;
            System.out.println("Wallet covers entire amount - Used: $" + walletBalanceUsed);
        } else {
            walletBalanceUsed = walletBalance;
            total = total.subtract(walletBalance);
            System.out.println("Wallet covers partial amount - Used: $" + walletBalanceUsed + ", Remaining: $" + total);
        }

        BigDecimal finalTotal = total;
        Platform.runLater(() -> {
            if (paymentTotalLabel != null) {
                if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0) {
                    paymentTotalLabel.setText(String.format("Total: $%.2f (Wallet: -$%.2f)",
                            finalTotal, walletBalanceUsed));
                    System.out.println("Updated paymentTotalLabel with wallet deduction");
                } else {
                    paymentTotalLabel.setText(String.format("Total: $%.2f", finalTotal));
                }
            }
        });

        System.out.println("Wallet balance applied - Used: $" + walletBalanceUsed +
                ", Remaining to pay: $" + total);
    }

    private void displaySaleItems(ObservableList<SaleItem> saleItems) {
        if (itemsVBox == null) {
            System.err.println("itemsVBox is null!");
            return;
        }

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

        System.out.println("Displayed " + saleItems.size() + " sale items");
    }

    private void setupPaidTextFieldListener() {
        if (paidTextField == null) return;

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
            // RELOAD DASHBOARD
            if (dashboardFormController != null) {
                dashboardFormController.loadDashboardContentAfterPayment();
            }
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
                    "Please select a payment method (Cash, Card, or Check)");
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

            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);

            if (paidAmount.compareTo(amountToPay) < 0) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Amount",
                        String.format("Paid amount ($%.2f) is less than amount to pay ($%.2f)",
                                paidAmount, amountToPay));
                return;
            }

            CustomerDTO tempCustomer = AddCustomerFormController.getTempCustomerDTO();

            if (tempCustomer == null) {
                showAlert(Alert.AlertType.ERROR, "No Customer",
                        "Customer information is missing. Please add customer first.");
                return;
            }

            System.out.println("Processing payment for customer: " + tempCustomer.getSaleId());

            processPayment(tempCustomer, paidAmount, originalTotal, amountToPay);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                    "Please enter a valid number");
        }
    }

    private void processPayment(CustomerDTO tempCustomer, BigDecimal paidAmount,
                                BigDecimal originalTotal, BigDecimal amountToPay) {
        SaleDataService dataService = SaleDataService.getInstance();
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
        paymentRequest.setSaleId(tempCustomer.getSaleId());
        paymentRequest.setCustomerContact(tempCustomer.getContact());
        paymentRequest.setCustomerEmail(tempCustomer.getEmail());
        paymentRequest.setSaleItems(saleItemDTOs);
        paymentRequest.setSubTotal(dataService.getSubtotal());
        paymentRequest.setTaxAmount(dataService.getTax());
        paymentRequest.setDiscountAmount(BigDecimal.ZERO);
        paymentRequest.setTotalAmount(originalTotal);
        paymentRequest.setPaidAmount(paidAmount);
        paymentRequest.setChangeAmount(paidAmount.subtract(amountToPay));
        paymentRequest.setPaymentMethod(selectedPaymentMethod);

        // Show processing message
        if (invoiceMessage != null) {
            invoiceMessage.setVisible(true);
            invoiceMessage.setManaged(true);
        }

        new Thread(() -> {
            try {
                // STEP 1: Save customer to server
                CustomerDTO savedCustomer = apiService.saveCustomer(tempCustomer);
                System.out.println("Customer saved: " + savedCustomer.getSaleId());

                // STEP 2: Process payment
                boolean success = apiService.processPayment(paymentRequest);

                if (success) {
                    System.out.println("Payment processed successfully!");

                    // STEP 3: Deduct wallet balance if used
                    if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0 &&
                            !"WALK-IN".equalsIgnoreCase(tempCustomer.getContact())) {
                        try {
                            apiService.deductFromWallet(tempCustomer.getContact(), walletBalanceUsed);
                            System.out.println("Deducted $" + walletBalanceUsed + " from wallet");
                        } catch (Exception we) {
                            System.err.println("Failed to deduct from wallet: " + we.getMessage());
                        }
                    }

                    Platform.runLater(() -> {
                        paymentProcessed = true;

                        // STEP 4: Update stock
                        System.out.println("Stock update initiated");
                        updateStockAfterPayment(saleItems);

                        // STEP 5: Show success
                        System.out.println("Success dialog shown");
                        showPaymentSuccess(tempCustomer, originalTotal, amountToPay, paidAmount);

                        // STEP 6: Clear temporary data
                        AddCustomerFormController.clearTempCustomerDTO();
                        SaleDataService.getInstance().clearSaleData();
                        walletBalance = BigDecimal.ZERO;
                        walletBalanceUsed = BigDecimal.ZERO;

                        // STEP 7: Hide processing message
                        if (invoiceMessage != null) {
                            invoiceMessage.setVisible(false);
                            invoiceMessage.setManaged(false);
                        }

                        System.out.println("Temporary customer data cleared");

                        // STEP 8: Reload Dashboard - THIS IS THE KEY FIX
                        System.out.println("Payment completed - UI updated");
                        if (dashboardFormController != null) {
                            dashboardFormController.loadDashboardContentAfterPayment();
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (invoiceMessage != null) {
                            invoiceMessage.setVisible(false);
                            invoiceMessage.setManaged(false);
                        }
                        showAlert(Alert.AlertType.ERROR, "Payment Failed",
                                "Failed to process payment. Please try again.");
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (invoiceMessage != null) {
                        invoiceMessage.setVisible(false);
                        invoiceMessage.setManaged(false);
                    }
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

    private void showPaymentSuccess(CustomerDTO customer, BigDecimal originalTotal,
                                    BigDecimal amountToPay, BigDecimal paidAmount) {
        StringBuilder message = new StringBuilder();
        message.append("Payment processed successfully!\n\n");
        message.append(String.format("Sale ID: %s\n", customer.getSaleId()));
        message.append(String.format("Customer: %s\n", customer.getContact()));
        message.append(String.format("Original Total: $%.2f\n", originalTotal));

        if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            message.append(String.format("Wallet Used: $%.2f\n", walletBalanceUsed));
            message.append(String.format("Amount Paid: $%.2f\n", paidAmount));
        } else {
            message.append(String.format("Paid: $%.2f\n", paidAmount));
        }

        message.append(String.format("Change: $%.2f\n", paidAmount.subtract(amountToPay)));
        message.append(String.format("Payment Method: %s\n\n", selectedPaymentMethod));
        message.append("Customer and sale data saved to server successfully!");

        showAlert(Alert.AlertType.INFORMATION, "Payment Successful", message.toString());
    }

    @FXML
    public void addwalletOnClick(ActionEvent actionEvent) {
        try {
            System.out.println("Add Wallet button clicked");

            CustomerDTO tempCustomer = AddCustomerFormController.getTempCustomerDTO();
            if (tempCustomer == null) {
                showAlert(Alert.AlertType.WARNING, "No Customer",
                        "No customer information available.");
                return;
            }

            if ("WALK-IN".equalsIgnoreCase(tempCustomer.getContact())) {
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

            BigDecimal originalTotal = SaleDataService.getInstance().getTotal();
            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);
            BigDecimal changeAmount = paidAmount.subtract(amountToPay);

            if (changeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.INFORMATION, "No Change",
                        "There is no change to add to the wallet.\nChange amount must be greater than zero.");
                return;
            }

            new Thread(() -> {
                try {
                    WalletDTO updatedWallet = apiService.addToWallet(
                            tempCustomer.getContact(), changeAmount);

                    System.out.println("Add to wallet response: " + updatedWallet);

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
                                    tempCustomer.getContact(),
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

        if (paidTextField != null) {
            paidTextField.setDisable(false);
            paidTextField.requestFocus();
        }
    }

    public void calculateChange() {
        try {
            String paidText = paidTextField.getText().trim();
            if (paidText.isEmpty()) {
                changeLabel.setText("$0.00");
                return;
            }

            BigDecimal paid = new BigDecimal(paidText);
            BigDecimal originalTotal = SaleDataService.getInstance().getTotal();
            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);
            BigDecimal change = paid.subtract(amountToPay);

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