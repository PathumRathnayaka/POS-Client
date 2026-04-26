package com.qaldrin.posclient.util;

import com.qaldrin.posclient.dto.CustomerDTO;
import com.qaldrin.posclient.model.SaleItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptsUtil {

    private static final double RECEIPT_WIDTH = 280; // Optimized for 80mm/58mm thermal printers

    /**
     * Generates and prints a receipt to the default printer.
     */
    public static void printReceipt(String saleId, CustomerDTO customer, List<SaleItem> items,
            BigDecimal total, BigDecimal paid, BigDecimal change) {
        VBox receipt = createReceiptLayout(saleId, customer, items, total, paid, change);

        // Use PrinterJob to print
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            // Receipt printers are sensitive to logical scale,
            // but usually JavaFX handles this well if the Node width is restricted.
            boolean success = job.printPage(receipt);
            if (success) {
                job.endJob();
            } else {
                System.err.println("Printing failed or was cancelled.");
            }
        } else {
            System.err.println("No default printer found.");
        }
    }

    /**
     * Creates a digital VBox layout of the receipt.
     */
    private static VBox createReceiptLayout(String saleId, CustomerDTO customer, List<SaleItem> items,
            BigDecimal total, BigDecimal paid, BigDecimal change) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(10));
        container.setPrefWidth(RECEIPT_WIDTH);
        container.setStyle("-fx-background-color: white; -fx-text-fill: black;");

        // --- Header Section ---
        Label title = new Label("POS SYSTEM");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        Label subTitle = new Label("QUALITY PRODUCTS, BEST PRICE");
        subTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
        subTitle.setMaxWidth(Double.MAX_VALUE);
        subTitle.setAlignment(Pos.CENTER);

        container.getChildren().addAll(title, subTitle, createSeparator());

        // --- Transaction Info ---
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Label dateLabel = new Label("Date: " + dtf.format(LocalDateTime.now()));
        Label saleIdLabel = new Label("Inv #: " + saleId);
        Label customerLabel = new Label("Customer: " + (customer != null ? customer.getContact() : "Walk-in"));

        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
        saleIdLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
        customerLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");

        container.getChildren().addAll(dateLabel, saleIdLabel, customerLabel, createSeparator());

        // --- Items Table Header ---
        HBox header = new HBox(5);
        Label itemH = new Label("Item");
        Label qtyH = new Label("Qty");
        Label priceH = new Label("Price");
        Label amountH = new Label("Total");

        itemH.setPrefWidth(100);
        qtyH.setPrefWidth(40);
        priceH.setPrefWidth(60);
        amountH.setPrefWidth(60);
        amountH.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(itemH, qtyH, priceH, amountH);
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: black;");
        container.getChildren().add(header);

        // --- Items List ---
        for (SaleItem item : items) {
            HBox row = new HBox(5);
            Label name = new Label(item.getName());
            Label qty = new Label(String.valueOf(item.getQuantity()));
            Label price = new Label(String.format("%.2f", item.getSalePrice()));
            Label amount = new Label(String.format("%.2f", item.getAmount()));

            name.setPrefWidth(100);
            name.setWrapText(true);
            qty.setPrefWidth(40);
            price.setPrefWidth(60);
            amount.setPrefWidth(60);
            amount.setAlignment(Pos.CENTER_RIGHT);

            row.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
            row.getChildren().addAll(name, qty, price, amount);
            container.getChildren().add(row);
        }

        container.getChildren().add(createSeparator());

        // --- Totals Section ---
        container.getChildren().add(createTotalRow("SUBTOTAL", total));
        container.getChildren().add(createTotalRow("TAX (LKR)", BigDecimal.ZERO)); // Replace with actual tax logic if
                                                                                   // needed
        container.getChildren().add(createTotalRow("TOTAL", total, true));
        container.getChildren().add(createSeparator());
        container.getChildren().add(createTotalRow("PAID", paid));
        container.getChildren().add(createTotalRow("CHANGE", change));

        container.getChildren().add(createSeparator());

        // --- Footer ---
        Label footer = new Label("THANK YOU FOR YOUR BUSINESS!");
        footer.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: black;");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);

        Label footer2 = new Label("PLEASE COME AGAIN!");
        footer2.setStyle("-fx-font-size: 9px; -fx-text-fill: black;");
        footer2.setMaxWidth(Double.MAX_VALUE);
        footer2.setAlignment(Pos.CENTER);

        container.getChildren().addAll(footer, footer2);

        return container;
    }

    /**
     * Creates a dashed line separator for the receipt.
     */
    private static Line createSeparator() {
        Line line = new Line(0, 0, RECEIPT_WIDTH - 20, 0);
        line.getStrokeDashArray().addAll(2.0, 2.0);
        line.setStyle("-fx-stroke: black;");
        return line;
    }

    private static HBox createTotalRow(String labelText, BigDecimal value) {
        return createTotalRow(labelText, value, false);
    }

    private static HBox createTotalRow(String labelText, BigDecimal value, boolean bold) {
        HBox row = new HBox();
        Label label = new Label(labelText);
        Label val = new Label(String.format("LKR %.2f", value));

        HBox.setHgrow(label, Priority.ALWAYS);
        val.setPrefWidth(100);
        val.setAlignment(Pos.CENTER_RIGHT);

        if (bold) {
            row.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: black;");
        } else {
            row.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
        }

        row.getChildren().addAll(label, val);
        return row;
    }
}
