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

    @FXML private Button addwalletBtn;

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

    private BigDecimal pendingWalletAddition = BigDecimal.ZERO;
    private boolean walletAdditionPending = false;


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

        if ("WALK-IN".equalsIgnoreCase(customer.getContact())) {
            if (saleIdLabel != null) {
                saleIdLabel.setText(customer.getSaleId() + " (Quick Sale)");
                saleIdLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            }
            if (customerContactLabel != null) {
                customerContactLabel.setText("Customer: Walk-in");
                customerContactLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            }
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

    public void disableWalletForWalkIn() {
        if (addwalletBtn != null) {
            addwalletBtn.setVisible(false);
            addwalletBtn.setManaged(false);
        }
        if (oldBalanceLabel != null) {
            oldBalanceLabel.setVisible(false);
            oldBalanceLabel.setManaged(false);
        }
        System.out.println("Wallet UI disabled for Walk-in (Quick Sale).");
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
                if (addwalletBtn != null) {
                    addwalletBtn.setVisible(true);
                    addwalletBtn.setManaged(true);
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
                            oldBalanceLabel.setText(String.format("Wallet Balance: $%.2f (Applied)", balance));
                            oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            System.out.println("Set oldBalanceLabel text to: Wallet Balance: $" + balance + " (Applied)");
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

                    if (addwalletBtn != null) {
                        addwalletBtn.setVisible(true);
                        addwalletBtn.setManaged(true);
                        System.out.println("addwalletBtn button made visible");
                    } else {
                        System.err.println("ERROR: addwalletBtn is NULL!");
                    }

                    // Apply wallet balance after loading
                    applyWalletBalance();
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
                    if (addwalletBtn != null) {
                        addwalletBtn.setVisible(true);
                        addwalletBtn.setManaged(true);
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
        BigDecimal originalTotal = dataService.getTotal();

        System.out.println("Original total: $" + originalTotal);

        // Use wallet balance to reduce total
        if (walletBalance.compareTo(originalTotal) >= 0) {
            // Wallet covers entire total
            walletBalanceUsed = originalTotal;
        } else {
            // Wallet covers partial total
            walletBalanceUsed = walletBalance;
        }

        // New total after wallet deduction
        BigDecimal reducedTotal = originalTotal.subtract(walletBalanceUsed);
        if (reducedTotal.compareTo(BigDecimal.ZERO) < 0) {
            reducedTotal = BigDecimal.ZERO;
        }

        // Update UI
        BigDecimal finalReducedTotal = reducedTotal;
        Platform.runLater(() -> {
            if (oldBalanceLabel != null) {
                oldBalanceLabel.setText(String.format("Wallet Balance: Rs. %.2f (Applied)",
                        walletBalance.doubleValue()));
                oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }

            if (paymentTotalLabel != null) {
                // ✅ Show only reduced total (like your POS payment form)
                paymentTotalLabel.setText(String.format("Total: Rs. %.2f", finalReducedTotal.doubleValue()));
            }

            // Update change if already entered
            if (paidTextField != null && !paidTextField.getText().trim().isEmpty()) {
                calculateChange();
            }
        });

        System.out.println("Wallet applied: used Rs. " + walletBalanceUsed +
                ", reduced total Rs. " + finalReducedTotal);
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
            showAlert(Alert.AlertType.WARNING, "Already Processed", "This payment has already been processed!");
            return;
        }

        if (selectedPaymentMethod == null) {
            showAlert(Alert.AlertType.WARNING, "Payment Method Required",
                    "Please select a payment method (Cash, Card, or Check)");
            return;
        }

        String paidText = paidTextField.getText().trim();
        if (paidText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Amount Required", "Please enter the paid amount");
            return;
        }

        try {
            BigDecimal paidAmount = new BigDecimal(paidText);
            SaleDataService dataService = SaleDataService.getInstance();
            BigDecimal originalTotal = dataService.getTotal();

            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);
            if (paidAmount.compareTo(amountToPay) < 0) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Amount",
                        String.format("Paid amount (Rs. %.2f) is less than amount to pay (Rs. %.2f)",
                                paidAmount, amountToPay));
                return;
            }

            CustomerDTO tempCustomer = AddCustomerFormController.getTempCustomerDTO();
            if (tempCustomer == null) {
                showAlert(Alert.AlertType.ERROR, "No Customer",
                        "Customer information is missing. Please add customer first.");
                return;
            }

            System.out.println("Processing payment for customer: " + tempCustomer.getContact());

            // Show processing message
            if (invoiceMessage != null) {
                invoiceMessage.setVisible(true);
                invoiceMessage.setManaged(true);
            }

            new Thread(() -> {
                try {
                    // STEP 1: Save customer - backend generates sale ID
                    CustomerDTO savedCustomer = apiService.saveCustomer(tempCustomer);
                    System.out.println("Customer saved with sale ID: " + savedCustomer.getSaleId());

                    // CRITICAL: Update temp customer with backend-generated sale ID
                    tempCustomer.setSaleId(savedCustomer.getSaleId());
                    tempCustomer.setId(savedCustomer.getId());
                    AddCustomerFormController.setTempCustomerDTO(tempCustomer);

                    // STEP 2: Process payment with the correct sale ID
                    PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
                    paymentRequest.setSaleId(savedCustomer.getSaleId()); // Use backend sale ID
                    paymentRequest.setCustomerContact(savedCustomer.getContact());
                    paymentRequest.setCustomerEmail(savedCustomer.getEmail());
                    paymentRequest.setSaleItems(toSaleItemDTOList());
                    paymentRequest.setSubTotal(dataService.getSubtotal());
                    paymentRequest.setTaxAmount(dataService.getTax());
                    paymentRequest.setDiscountAmount(BigDecimal.ZERO);
                    paymentRequest.setTotalAmount(originalTotal);
                    paymentRequest.setPaidAmount(paidAmount);
                    paymentRequest.setChangeAmount(paidAmount.subtract(amountToPay));
                    paymentRequest.setPaymentMethod(selectedPaymentMethod);

                    boolean success = apiService.processPayment(paymentRequest);

                    if (success) {
                        System.out.println("Payment processed successfully!");

                        // STEP 3: Deduct used wallet balance (if applicable)
                        if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0 &&
                                !"WALK-IN".equalsIgnoreCase(savedCustomer.getContact())) {
                            try {
                                apiService.deductFromWallet(savedCustomer.getContact(), walletBalanceUsed);
                                System.out.println("Deducted Rs. " + walletBalanceUsed + " from wallet");
                            } catch (Exception e) {
                                System.err.println("Failed to deduct from wallet: " + e.getMessage());
                            }
                        }

                        // STEP 4: Apply pending wallet addition (if staged)
                        if (walletAdditionPending && pendingWalletAddition.compareTo(BigDecimal.ZERO) > 0) {
                            try {
                                apiService.addToWallet(savedCustomer.getContact(), pendingWalletAddition);
                                System.out.println("Wallet updated to Rs. " + pendingWalletAddition);
                            } catch (Exception e) {
                                System.err.println("Failed to update wallet: " + e.getMessage());
                            }
                        }

                        Platform.runLater(() -> {
                            paymentProcessed = true;

                            // STEP 5: Update stock
                            updateStockAfterPayment(SaleDataService.getInstance().getCurrentSaleItems());

                            // STEP 6: Show success message
                            StringBuilder message = new StringBuilder();
                            message.append("Payment processed successfully!\n\n");
                            message.append(String.format("Sale ID: %s\n", savedCustomer.getSaleId()));
                            message.append(String.format("Customer: %s\n", savedCustomer.getContact()));

                            if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0) {
                                message.append(String.format("Wallet Used: Rs. %.2f\n", walletBalanceUsed));
                            }

                            message.append(String.format("Paid: Rs. %.2f\n", paidAmount));

                            BigDecimal change = paidAmount.subtract(amountToPay);
                            if (change.compareTo(BigDecimal.ZERO) < 0) change = BigDecimal.ZERO;

                            if (walletAdditionPending && pendingWalletAddition.compareTo(BigDecimal.ZERO) > 0) {
                                message.append(String.format("Change Added to Wallet: Rs. %.2f\n", pendingWalletAddition));
                            } else {
                                message.append(String.format("Change: Rs. %.2f\n", change));
                            }

                            message.append(String.format("Payment Method: %s\n", selectedPaymentMethod));
                            showAlert(Alert.AlertType.INFORMATION, "Payment Successful", message.toString());

                            // STEP 7: Clear data & reset UI
                            AddCustomerFormController.clearTempCustomerDTO();
                            SaleDataService.getInstance().clearSaleData();

                            walletBalance = BigDecimal.ZERO;
                            walletBalanceUsed = BigDecimal.ZERO;
                            pendingWalletAddition = BigDecimal.ZERO;
                            walletAdditionPending = false;

                            if (addwalletBtn != null) {
                                addwalletBtn.setText("Add to Wallet");
                                addwalletBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                                addwalletBtn.setDisable(false);
                            }

                            if (invoiceMessage != null) {
                                invoiceMessage.setVisible(false);
                                invoiceMessage.setManaged(false);
                            }

                            // STEP 8: Reload Dashboard
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
                            showAlert(Alert.AlertType.ERROR, "Payment Failed", "Failed to process payment. Please try again.");
                        });
                    }

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        if (invoiceMessage != null) {
                            invoiceMessage.setVisible(false);
                            invoiceMessage.setManaged(false);
                        }
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Payment Error",
                                "Error processing payment: " + e.getMessage() + "\n\nPlease check server connection.");
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid number");
        }
    }

    private List<SaleItemDTO> toSaleItemDTOList() {
        List<SaleItemDTO> saleItemDTOs = new ArrayList<>();
        ObservableList<SaleItem> saleItems = SaleDataService.getInstance().getCurrentSaleItems();

        if (saleItems != null) {
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
        }

        return saleItemDTOs;
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

        // Show wallet savings if used
        if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            message.append(String.format("Original Total: $%.2f\n", originalTotal));
            message.append(String.format("Wallet Used: $%.2f\n", walletBalanceUsed));
            message.append(String.format("Amount to Pay: $%.2f\n", amountToPay));
        } else {
            message.append(String.format("Total: $%.2f\n", originalTotal));
        }

        message.append(String.format("Paid: $%.2f\n", paidAmount));

        BigDecimal change = paidAmount.subtract(amountToPay);
        if (change.compareTo(BigDecimal.ZERO) < 0) {
            change = BigDecimal.ZERO;
        }
        message.append(String.format("Change: $%.2f\n", change));
        message.append(String.format("Payment Method: %s\n\n", selectedPaymentMethod));
        message.append("Customer and sale data saved to server successfully!");

        showAlert(Alert.AlertType.INFORMATION, "Payment Successful", message.toString());
    }

    @FXML
    public void addwalletOnClick(ActionEvent actionEvent) {
        try {
            CustomerDTO tempCustomer = AddCustomerFormController.getTempCustomerDTO();
            if (tempCustomer == null) {
                showAlert(Alert.AlertType.WARNING, "No Customer", "No customer information available.");
                return;
            }

            if ("WALK-IN".equalsIgnoreCase(tempCustomer.getContact())) {
                showAlert(Alert.AlertType.INFORMATION, "Not Available", "Wallet feature is not available for walk-in customers.");
                return;
            }

            String paidText = paidTextField != null ? paidTextField.getText().trim() : "";
            if (paidText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Payment Amount", "Please enter the payment amount first.");
                return;
            }

            BigDecimal paidAmount;
            try {
                paidAmount = new BigDecimal(paidText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid payment amount.");
                return;
            }

            BigDecimal totalAmount = SaleDataService.getInstance().getTotal();
            BigDecimal changeAmount = paidAmount.subtract(totalAmount.subtract(walletBalanceUsed));

            if (changeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.INFORMATION, "No Change", "There is no change to add to the wallet.");
                return;
            }

            // --- TOGGLE BEHAVIOR ---
            if (walletAdditionPending) {
                // Undo staged wallet
                walletAdditionPending = false;
                pendingWalletAddition = BigDecimal.ZERO;

                if (oldBalanceLabel != null) {
                    oldBalanceLabel.setText(String.format("Wallet Balance: Rs. %.2f", walletBalance.doubleValue()));
                    oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
                if (addwalletBtn != null) {
                    addwalletBtn.setText("Add to Wallet");
                    addwalletBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                }
                if (changeLabel != null) {
                    changeLabel.setText(String.format("Change: Rs. %.2f", changeAmount.doubleValue()));
                    changeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                }

                System.out.println("Wallet addition cancelled. Customer receives cash Rs." + changeAmount);
            } else {
                // Stage new wallet addition
                walletAdditionPending = true;
                pendingWalletAddition = changeAmount;

                if (oldBalanceLabel != null) {
                    oldBalanceLabel.setText(String.format("New Balance: Rs. %.2f (Pending)", pendingWalletAddition.doubleValue()));
                    oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
                }
                if (addwalletBtn != null) {
                    addwalletBtn.setText("Undo Wallet");
                    addwalletBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                }
                if (changeLabel != null) {
                    changeLabel.setText("Change: Rs. 0.00");
                    changeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
                }

                System.out.println("Wallet addition staged: Rs." + pendingWalletAddition);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to handle Add Wallet: " + e.getMessage());
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

            // Calculate actual amount to pay after wallet deduction
            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);
            if (amountToPay.compareTo(BigDecimal.ZERO) < 0) {
                amountToPay = BigDecimal.ZERO;
            }

            BigDecimal change = paid.subtract(amountToPay);

            if (change.compareTo(BigDecimal.ZERO) < 0) {
                changeLabel.setText("Change: $0.00");
                changeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            } else {
                changeLabel.setText(String.format("Change: $%.2f", change));
                changeLabel.setStyle("-fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold;");
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("Change: $0.00");
            changeLabel.setStyle("-fx-font-size: 16px;");
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

    public void onCashSelected(ActionEvent actionEvent) {

    }

    public void onCardSelected(ActionEvent actionEvent) {

    }

    public void onWalletSelected(ActionEvent actionEvent) {

    }
}