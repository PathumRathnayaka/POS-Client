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
        primaryScene.toFront();
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
        String saleId = AddCustomerFormController.getTempSaleId();
        ObservableList<SaleItem> saleItems = dataService.getCurrentSaleItems();

        System.out.println("Customer from temp: " + (customer != null ? customer.getContact() : "null"));
        System.out.println("Sale ID from temp: " + saleId);
        System.out.println("Sale items count: " + (saleItems != null ? saleItems.size() : 0));

        if (customer == null || saleItems == null || saleItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No sale data available!");
            return;
        }

        // Handle Walk-in customers (Quick Sale)
        if ("WALK-IN".equalsIgnoreCase(customer.getContact())) {
            if (saleIdLabel != null) {
                saleIdLabel.setText(saleId + " (Quick Sale)");
                saleIdLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            }
            if (customerContactLabel != null) {
                customerContactLabel.setText("Customer: Walk-in");
                customerContactLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            }

            // Hide wallet UI for walk-in customers
            Platform.runLater(() -> {
                if (addwalletBtn != null) {
                    addwalletBtn.setVisible(false);
                    addwalletBtn.setManaged(false);
                }
                if (oldBalanceLabel != null) {
                    oldBalanceLabel.setVisible(false);
                    oldBalanceLabel.setManaged(false);
                }
            });
        } else {
            // Regular customer with potential wallet
            if (saleIdLabel != null) {
                saleIdLabel.setText("Sale ID: " + saleId);
            }
            if (customerContactLabel != null) {
                customerContactLabel.setText("Customer: " + customer.getContact());
            }

            // ✅ Load and apply wallet balance automatically
            loadWalletBalanceAndApply(customer.getContact());
        }

        // Display sale items
        displaySaleItems(saleItems);

        // Set totals
        BigDecimal subtotal = dataService.getSubtotal();
        BigDecimal tax = dataService.getTax();
        BigDecimal total = dataService.getTotal();

        System.out.println("Subtotal: " + subtotal + ", Tax: " + tax + ", Total: " + total);

        if (subTotalLabel != null) {
            subTotalLabel.setText(String.format("Rs. %.2f", subtotal));
        }
        if (taxLabel != null) {
            taxLabel.setText(String.format("Rs. %.2f", tax));
        }
        if (totalLabel != null) {
            totalLabel.setText(String.format("Rs. %.2f", total));
        }
        if (paymentTotalLabel != null) {
            // This will be updated by loadWalletBalanceAndApply if wallet exists
            paymentTotalLabel.setText(String.format("Rs. %.2f", total));
        }
        if (changeLabel != null) {
            changeLabel.setText("Rs. 0.00");
        }
    }

    private void loadWalletBalanceAndApply(String customerContact) {
        System.out.println("Loading and applying wallet balance for: " + customerContact);

        new Thread(() -> {
            try {
                // Fetch wallet balance from server
                BigDecimal balance = apiService.getWalletBalance(customerContact);
                walletBalance = balance;

                System.out.println("Wallet balance received: Rs. " + balance);

                // Get current total (before wallet deduction)
                SaleDataService dataService = SaleDataService.getInstance();
                BigDecimal originalTotal = dataService.getTotal();

                // Calculate how much wallet balance to use
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    if (balance.compareTo(originalTotal) >= 0) {
                        // Wallet covers entire total
                        walletBalanceUsed = originalTotal;
                    } else {
                        // Wallet covers partial total
                        walletBalanceUsed = balance;
                    }

                    // Calculate new total after wallet deduction
                    BigDecimal reducedTotal = originalTotal.subtract(walletBalanceUsed);
                    if (reducedTotal.compareTo(BigDecimal.ZERO) < 0) {
                        reducedTotal = BigDecimal.ZERO;
                    }

                    BigDecimal finalReducedTotal = reducedTotal;

                    // Update UI
                    Platform.runLater(() -> {
                        if (oldBalanceLabel != null) {
                            oldBalanceLabel.setText(String.format("Wallet Balance: Rs. %.2f (Applied)",
                                    balance.doubleValue()));
                            oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            oldBalanceLabel.setVisible(true);
                            oldBalanceLabel.setManaged(true);
                        }

                        // ✅ CRITICAL: Show reduced total (after wallet deduction)
                        if (paymentTotalLabel != null) {
                            paymentTotalLabel.setText(String.format("Total: Rs. %.2f", finalReducedTotal.doubleValue()));
                        }

                        if (addwalletBtn != null) {
                            addwalletBtn.setVisible(true);
                            addwalletBtn.setManaged(true);
                        }

                        // Update change if amount already entered
                        if (paidTextField != null && !paidTextField.getText().trim().isEmpty()) {
                            calculateChange();
                        }
                    });

                    System.out.println("Wallet applied - Used: Rs. " + walletBalanceUsed +
                            ", Original Total: Rs. " + originalTotal +
                            ", New Total: Rs. " + finalReducedTotal);
                } else {
                    // No wallet balance
                    Platform.runLater(() -> {
                        if (oldBalanceLabel != null) {
                            oldBalanceLabel.setText("Wallet Balance: Rs. 0.00");
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

            } catch (Exception e) {
                System.err.println("Error loading and applying wallet: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    if (oldBalanceLabel != null) {
                        oldBalanceLabel.setText("Wallet Balance: Rs. 0.00");
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


    private void displaySaleItems(ObservableList<SaleItem> saleItems) {


        if (itemsVBox == null) {
            System.err.println("itemsVBox is null!");
            return;
        }

        itemsVBox.getChildren().clear();

        for (SaleItem item : saleItems) {
            HBox itemRow = new HBox(10);
            itemRow.setPadding(new Insets(5));
            itemRow.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8;");

            Label nameLabel = new Label(item.getName());
            nameLabel.setPrefWidth(200);
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            Label qtyLabel = new Label("Qty: " + item.getQuantity());
            qtyLabel.setPrefWidth(80);
            qtyLabel.setStyle("-fx-text-fill: #ecf0f1;");

            Label priceLabel = new Label(String.format("$%.2f", item.getSalePrice()));
            priceLabel.setPrefWidth(80);
            priceLabel.setStyle("-fx-text-fill: #ecf0f1;");

            Label amountLabel = new Label(String.format("$%.2f", item.getAmount()));
            amountLabel.setPrefWidth(80);
            amountLabel.setStyle("-fx-text-fill: #25D366; -fx-font-weight: bold;");

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

            // ✅ Calculate actual amount to pay after wallet deduction
            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);
            if (amountToPay.compareTo(BigDecimal.ZERO) < 0) {
                amountToPay = BigDecimal.ZERO;
            }

            // ✅ FIX: Make it final so it can be used in lambda
            final BigDecimal finalAmountToPay = amountToPay;

            // ✅ Validate against wallet-adjusted amount
            if (paidAmount.compareTo(finalAmountToPay) < 0) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Amount",
                        String.format("Paid amount (Rs. %.2f) is less than amount to pay (Rs. %.2f)",
                                paidAmount, finalAmountToPay));
                return;
            }

            CustomerDTO tempCustomer = AddCustomerFormController.getTempCustomerDTO();
            String tempSaleId = AddCustomerFormController.getTempSaleId();

            if (tempCustomer == null || tempSaleId == null) {
                showAlert(Alert.AlertType.ERROR, "No Data",
                        "Customer or sale information is missing. Please add customer first.");
                return;
            }

            System.out.println("Processing payment for customer: " + tempCustomer.getContact() +
                    ", Sale ID: " + tempSaleId);

            // Show processing message
            if (invoiceMessage != null) {
                invoiceMessage.setVisible(true);
                invoiceMessage.setManaged(true);
            }

            new Thread(() -> {
                try {
                    // STEP 1: Save/Get customer
                    CustomerDTO savedCustomer = apiService.saveCustomer(tempCustomer);
                    System.out.println("Customer processed - ID: " + savedCustomer.getId());

                    // STEP 2: Process payment with CLIENT-GENERATED sale ID
                    PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
                    paymentRequest.setSaleId(tempSaleId);
                    paymentRequest.setCustomerContact(savedCustomer.getContact());
                    paymentRequest.setCustomerEmail(savedCustomer.getEmail());
                    paymentRequest.setSaleItems(toSaleItemDTOList());
                    paymentRequest.setSubTotal(dataService.getSubtotal());
                    paymentRequest.setTaxAmount(dataService.getTax());
                    paymentRequest.setDiscountAmount(BigDecimal.ZERO);
                    paymentRequest.setTotalAmount(originalTotal);
                    paymentRequest.setPaidAmount(paidAmount);
                    // ✅ Use finalAmountToPay here
                    paymentRequest.setChangeAmount(paidAmount.subtract(finalAmountToPay));
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
                            message.append(String.format("Sale ID: %s\n", tempSaleId));
                            message.append(String.format("Customer: %s\n", savedCustomer.getContact()));

                            if (walletBalanceUsed.compareTo(BigDecimal.ZERO) > 0) {
                                message.append(String.format("Wallet Used: Rs. %.2f\n", walletBalanceUsed));
                            }

                            message.append(String.format("Paid: Rs. %.2f\n", paidAmount));

                            // ✅ Use finalAmountToPay here too
                            BigDecimal change = paidAmount.subtract(finalAmountToPay);
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
        }    }

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

            // ✅ Calculate change from wallet-adjusted total
            BigDecimal amountToPay = totalAmount.subtract(walletBalanceUsed);
            if (amountToPay.compareTo(BigDecimal.ZERO) < 0) {
                amountToPay = BigDecimal.ZERO;
            }

            BigDecimal changeAmount = paidAmount.subtract(amountToPay);

            if (changeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.INFORMATION, "No Change",
                        "There is no change to add to the wallet.\nChange amount must be greater than zero.");
                return;
            }

            // --- TOGGLE BEHAVIOR ---
            if (walletAdditionPending) {
                // UNDO - Customer will receive cash
                System.out.println("Undoing wallet addition - customer will receive cash change");
                pendingWalletAddition = BigDecimal.ZERO;
                walletAdditionPending = false;

                Platform.runLater(() -> {
                    if (oldBalanceLabel != null) {
                        if (walletBalance.compareTo(BigDecimal.ZERO) > 0) {
                            oldBalanceLabel.setText(String.format("Wallet Balance: Rs. %.2f (Applied)",
                                    walletBalance.doubleValue()));
                            oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else {
                            oldBalanceLabel.setText("Wallet Balance: Rs. 0.00");
                            oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                        }
                    }

                    // Change button back to GREEN
                    if (addwalletBtn != null) {
                        addwalletBtn.setText("Add to Wallet");
                        addwalletBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                    }

                    // Show actual change amount (customer gets cash)
                    if (changeLabel != null) {
                        changeLabel.setText(String.format("Change: Rs. %.2f", changeAmount.doubleValue()));
                        changeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                    }
                });

                System.out.println("Wallet addition cancelled - Rs. " + changeAmount + " will be given as cash");

            } else {
                // ADD TO WALLET - Stage the addition (SET new balance, not add)
                System.out.println("Staging wallet addition - customer will NOT receive cash");
                pendingWalletAddition = changeAmount; // NEW balance (replaces old)
                walletAdditionPending = true;

                Platform.runLater(() -> {
                    if (oldBalanceLabel != null) {
                        oldBalanceLabel.setText(String.format("New Balance: Rs. %.2f (Pending)",
                                pendingWalletAddition.doubleValue()));
                        oldBalanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
                    }

                    // Change button to BLUE
                    if (addwalletBtn != null) {
                        addwalletBtn.setText("Undo Wallet");
                        addwalletBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                    }

                    // Show Rs. 0.00 (no cash change for customer)
                    if (changeLabel != null) {
                        changeLabel.setText("Change: Rs. 0.00");
                        changeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
                    }
                });

                System.out.println("Wallet addition staged - Rs. " + changeAmount +
                        " | Current balance: Rs. " + walletBalance +
                        " | New balance will be: Rs. " + pendingWalletAddition);
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

        String selectedStyle = "-fx-background-color: #25D366; -fx-text-fill: white; -fx-font-weight: bold;";
        String normalStyle = "-fx-background-color: #34495e; -fx-text-fill: white;";
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
                // Handle pending wallet display
                if (walletAdditionPending) {
                    changeLabel.setText("Change: Rs. 0.00");
                    changeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
                } else {
                    changeLabel.setText("Change: Rs. 0.00");
                    changeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                }
                return;
            }

            BigDecimal paid = new BigDecimal(paidText);
            BigDecimal originalTotal = SaleDataService.getInstance().getTotal();

            // ✅ CRITICAL FIX: Calculate actual amount to pay after wallet deduction
            BigDecimal amountToPay = originalTotal.subtract(walletBalanceUsed);
            if (amountToPay.compareTo(BigDecimal.ZERO) < 0) {
                amountToPay = BigDecimal.ZERO;
            }

            BigDecimal change = paid.subtract(amountToPay);

            if (change.compareTo(BigDecimal.ZERO) < 0) {
                changeLabel.setText("Change: Rs. 0.00");
                changeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            } else {
                // Show change based on wallet addition status
                if (walletAdditionPending) {
                    changeLabel.setText("Change: Rs. 0.00"); // Customer not getting cash
                    changeLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 16px; -fx-font-weight: bold;");
                } else {
                    changeLabel.setText(String.format("Change: Rs. %.2f", change));
                    changeLabel.setStyle("-fx-text-fill: green; -fx-font-size: 16px; -fx-font-weight: bold;");
                }
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("Change: Rs. 0.00");
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