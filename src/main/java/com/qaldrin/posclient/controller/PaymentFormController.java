package com.qaldrin.posclient.controller;




import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PaymentFormController implements Initializable {

    @FXML private AnchorPane primaryScene;
    @FXML private VBox itemsVBox;
    @FXML private AnchorPane invoiceMessage;



    // Payment Method Buttons


    // Price Labels
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
        // Load initial data
        // loadSaleData();
    }

    private void initializeUI() {
        // TODO: Initialize UI components, styles, event handlers
        setupPaymentButtons();
        setupInvoiceButtons();
        updatePriceLabels(0.0, 0.0, 0.0);
    }

    // Action Button Handlers
    @FXML
    private void onCancel() {
        // TODO: Implement cancel payment logic
    }

    @FXML
    private void onDiscount() {
        // TODO: Implement discount application logic
    }

    @FXML
    private void onTaxes() {
        // TODO: Implement tax management logic
    }

    // Payment Method Handlers
    @FXML
    private void onCashPayment() {
        // TODO: Implement cash payment processing
        selectPaymentMethod("Cash");
    }

    @FXML
    private void onCheckPayment() {
        // TODO: Implement check payment processing
        selectPaymentMethod("Check");
    }

    @FXML
    private void onCardPayment() {
        // TODO: Implement card payment processing
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
        // TODO: Complete payment transaction
    }

    // Utility Methods
    public void updatePriceLabels(double subtotal, double tax, double total) {
        // TODO: Update price display
        subTotalLabel.setText(String.format("Sub Total: $%.2f", subtotal));
        taxLabel.setText(String.format("Tax: $%.2f", tax));
        totalLabel.setText(String.format("Total: $%.2f", total));
        paymentTotalLabel.setText(String.format("Total: $%.2f", total));
        changeLabel.setText("Change: $0.00");
    }

    public void selectPaymentMethod(String method) {

    }

    public void updateCustomerInfo(String saleId, String customerContact) {
        // TODO: Update customer information display
        saleIdLabel.setText("Sale ID: " + saleId);
        customerContactLabel.setText("Customer Contact: " + customerContact);
    }

    public void calculateChange() {
        // TODO: Calculate and display change
        try {
            String paidText = paidTextField.getText().replace("$", "").trim();
            double paid = Double.parseDouble(paidText);
            double total = Double.parseDouble(totalLabel.getText().replaceAll("[^\\d.]", ""));
            double change = paid - total;
            changeLabel.setText(String.format("Change: $%.2f", Math.max(0, change)));
        } catch (NumberFormatException e) {
            changeLabel.setText("Change: $0.00");
        }
    }

    public void loadSaleItems() {
        // TODO: Load and display sale items in itemsVBox
        itemsVBox.getChildren().clear();
    }

    public void setupPaymentButtons() {
        // TODO: Setup payment button event handlers and styling
    }

    public void setupInvoiceButtons() {
        // TODO: Setup invoice button event handlers
    }

    public void enablePaidInput(boolean enable) {
        paidTextField.setDisable(!enable);
    }

    public double getTotalAmount() {
        // TODO: Extract total amount from label or data model
        return 0.0;
    }

    public String getSelectedPaymentMethod() {
        // TODO: Return currently selected payment method
        return "";
    }

    public void resetPaymentForm() {
        // TODO: Reset form to initial state
        updatePriceLabels(0.0, 0.0, 0.0);
        paidTextField.clear();
        paidTextField.setDisable(true);

        loadSaleItems();
    }

    public void validatePayment() {
        // TODO: Validate payment before completion
    }
}