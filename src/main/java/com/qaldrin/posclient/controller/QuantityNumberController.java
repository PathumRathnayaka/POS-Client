package com.qaldrin.posclient.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class QuantityNumberController implements Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;

    @FXML
    private Button btnDot, btnMinus, btnDelete, btnEsc, btnEnter, btnEnterKey, btnClose;

    private BigDecimal currentQuantity = BigDecimal.ONE;
    private BigDecimal maxQuantity = new BigDecimal("9999");
    private String productName = "";
    private String unitType = "";
    private Runnable onCancel;
    private Consumer<BigDecimal> onQuantityChanged;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("QuantityNumberController initialized");
        setupNumberButtons();
    }

    private void setupNumberButtons() {
        if (btn0 != null)
            btn0.setOnAction(e -> appendNumber("0"));
        if (btn1 != null)
            btn1.setOnAction(e -> appendNumber("1"));
        if (btn2 != null)
            btn2.setOnAction(e -> appendNumber("2"));
        if (btn3 != null)
            btn3.setOnAction(e -> appendNumber("3"));
        if (btn4 != null)
            btn4.setOnAction(e -> appendNumber("4"));
        if (btn5 != null)
            btn5.setOnAction(e -> appendNumber("5"));
        if (btn6 != null)
            btn6.setOnAction(e -> appendNumber("6"));
        if (btn7 != null)
            btn7.setOnAction(e -> appendNumber("7"));
        if (btn8 != null)
            btn8.setOnAction(e -> appendNumber("8"));
        if (btn9 != null)
            btn9.setOnAction(e -> appendNumber("9"));
    }

    private void appendNumber(String number) {
        if (quantityField == null)
            return;

        String currentText = quantityField.getText();

        if (("0".equals(currentText) || "1".equals(currentText))) {
            quantityField.setText(number);
        } else {
            String newText = currentText + number;
            try {
                BigDecimal newValue = new BigDecimal(newText);
                if (newValue.compareTo(maxQuantity) <= 0) {
                    quantityField.setText(newText);
                } else {
                    quantityField.setText(maxQuantity.stripTrailingZeros().toPlainString());
                }
            } catch (NumberFormatException e) {
                // Ignore parse errors
            }
        }
    }

    @FXML
    private void onDeleteClick(ActionEvent event) {
        if (quantityField == null)
            return;

        String currentText = quantityField.getText();
        if (currentText.length() > 1) {
            quantityField.setText(currentText.substring(0, currentText.length() - 1));
        } else {
            quantityField.setText("1");
        }
    }

    @FXML
    private void onMinusClick(ActionEvent event) {
        if (quantityField == null)
            return;

        try {
            BigDecimal current = new BigDecimal(quantityField.getText());
            if (current.compareTo(BigDecimal.ONE) > 0) {
                quantityField.setText(current.subtract(BigDecimal.ONE).stripTrailingZeros().toPlainString());
            }
        } catch (NumberFormatException e) {
            quantityField.setText("1");
        }
    }

    @FXML
    private void onDotClick(ActionEvent event) {
        if (quantityField == null || isDecimalDisabled())
            return;
        String currentText = quantityField.getText();
        if (!currentText.contains(".")) {
            quantityField.setText(currentText + ".");
        }
    }

    private boolean isDecimalDisabled() {
        return "PIECE".equalsIgnoreCase(unitType) ||
                "PACK".equalsIgnoreCase(unitType) ||
                "BOX".equalsIgnoreCase(unitType);
    }

    @FXML
    private void onEscClick(ActionEvent event) {
        if (onCancel != null)
            onCancel.run();
    }

    @FXML
    private void onCloseClick(ActionEvent event) {
        if (onCancel != null)
            onCancel.run();
    }

    @FXML
    private void onEnterClick(ActionEvent event) {
        if (quantityField == null)
            return;

        try {
            BigDecimal newQuantity = new BigDecimal(quantityField.getText());

            if (newQuantity.compareTo(maxQuantity) > 0) {
                newQuantity = maxQuantity;
            }

            if (newQuantity.compareTo(BigDecimal.ZERO) > 0 && onQuantityChanged != null) {
                onQuantityChanged.accept(newQuantity);
            }
        } catch (NumberFormatException e) {
            quantityField.setText("1");
        }
    }

    // Setters for configuration
    public void setCurrentQuantity(BigDecimal quantity) {
        this.currentQuantity = quantity;
        Platform.runLater(() -> {
            if (quantityField != null) {
                quantityField.setText(quantity != null ? quantity.stripTrailingZeros().toPlainString() : "1");
            }
        });
    }

    public void setProductName(String name) {
        this.productName = name;
        Platform.runLater(() -> {
            if (subtitleLabel != null) {
                subtitleLabel.setText("Enter quantity for " + name);
            }
        });
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setOnQuantityChanged(Consumer<BigDecimal> onQuantityChanged) {
        this.onQuantityChanged = onQuantityChanged;
    }

    public void setMaxQuantity(BigDecimal max) {
        this.maxQuantity = max;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
        Platform.runLater(() -> {
            if (btnDot != null) {
                boolean disable = isDecimalDisabled();
                btnDot.setDisable(disable);
                if (disable) {
                    btnDot.setStyle(
                            "-fx-background-color: #3b3b3b; -fx-text-fill: #5a5a5a; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 8px;");
                }
            }
        });
    }
}
