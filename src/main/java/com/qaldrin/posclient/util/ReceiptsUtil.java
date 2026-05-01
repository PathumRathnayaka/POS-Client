package com.qaldrin.posclient.util;

import com.qaldrin.posclient.dto.CustomerDTO;
import com.qaldrin.posclient.model.SaleItem;
import com.qaldrin.posclient.dto.InvoiceSettingsDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

import java.io.File;
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
            BigDecimal total, BigDecimal paid, BigDecimal change, InvoiceSettingsDTO settings) {
        javafx.application.Platform.runLater(() -> {
            VBox receipt = createReceiptLayout(saleId, customer, items, total, paid, change, settings);

            // Apply CSS and layout explicitly to off-screen node to ensure constraints
            // exist before printing
            receipt.applyCss();
            receipt.layout();

            // Use PrinterJob to print
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                job.getJobSettings().setJobName(saleId);
                boolean success = job.printPage(receipt);
                if (success) {
                    job.endJob();
                } else {
                    System.err.println("Printing failed or was cancelled.");
                }
            } else {
                System.err.println("No default printer found.");
            }
        });
    }

    /**
     * Creates a digital VBox layout of the receipt.
     */
    private static VBox createReceiptLayout(String saleId, CustomerDTO customer, List<SaleItem> items,
            BigDecimal total, BigDecimal paid, BigDecimal change, InvoiceSettingsDTO settings) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(10));
        container.setPrefWidth(RECEIPT_WIDTH);
        container.setStyle("-fx-background-color: white;");

        // --- TRACE DEBUG ---
        System.out.println("[RECEIPT-DEBUG] Rendering Invoice...");
        System.out.println("[RECEIPT-DEBUG] Address: " + settings.getCompanyAddress());
        System.out.println("[RECEIPT-DEBUG] Contact: " + settings.getCompanyContact());

        // --- Header Section / Logo ---
        if (settings.getLogoPath() != null && !settings.getLogoPath().isEmpty()) {
            try {
                File file = new File(settings.getLogoPath());
                System.out.println("[PRINT-CLIENT-DEBUG] Checking logo file. Exists? " + file.exists() + " | Path: "
                        + file.getAbsolutePath());
                if (file.exists()) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString());
                    System.out.println("[PRINT-CLIENT-DEBUG] Image Object Loaded. isError? " + img.isError()
                            + " | WxH: " + img.getWidth() + "x" + img.getHeight());

                    if (!img.isError() && img.getWidth() > 0) {
                        double MAX_LOGO_WIDTH = RECEIPT_WIDTH - 20;
                        double MAX_LOGO_HEIGHT = 80; // Reasonable vertical threshold for thermal paper

                        double imgWidth = img.getWidth();
                        double imgHeight = img.getHeight();

                        double scaleX = MAX_LOGO_WIDTH / imgWidth;
                        double scaleY = MAX_LOGO_HEIGHT / imgHeight;
                        double scale = Math.min(scaleX, scaleY);
                        if (scale > 1.0)
                            scale = 1.0;

                        double targetWidth = imgWidth * scale;
                        double targetHeight = imgHeight * scale;

                        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(targetWidth, targetHeight);
                        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

                        // Explicitly fill the background with white to neutralize PNG transparency
                        // issues on thermal printers
                        gc.setFill(javafx.scene.paint.Color.WHITE);
                        gc.fillRect(0, 0, targetWidth, targetHeight);

                        // Immediatly draw pixels out before attaching to scene graph
                        gc.drawImage(img, 0, 0, targetWidth, targetHeight);

                        HBox logoWrapper = new HBox(canvas);
                        logoWrapper.setAlignment(Pos.CENTER);
                        logoWrapper.setStyle("-fx-background-color: white;");
                        container.getChildren().add(logoWrapper);
                    } else {
                        System.err.println("[PRINT-CLIENT-DEBUG] Logo image returned invalid dimensions.");
                        addStringHeader(container, settings);
                    }
                } else {
                    System.err.println("[PRINT-CLIENT-DEBUG] Fallback to string header: Logo file not found on disk");
                    addStringHeader(container, settings);
                }
            } catch (Exception e) {
                System.err.println("[PRINT-CLIENT-DEBUG] Exception during logo loading: " + e.getMessage());
                e.printStackTrace();
                addStringHeader(container, settings);
            }
        } else {
            System.out.println("[PRINT-CLIENT-DEBUG] No valid logo path configured in DB. Using fallback text.");
            addStringHeader(container, settings);
        }

        Label subTitle = new Label(settings.getCompanySlogan());
        String subtitleStyle = "-fx-font-size: 10px; -fx-text-fill: black;";
        subTitle.setStyle(subtitleStyle);
        subTitle.setMaxWidth(Double.MAX_VALUE);
        subTitle.setAlignment(Pos.CENTER);

        container.getChildren().add(subTitle);

        if (settings.getCompanyAddress() != null && !settings.getCompanyAddress().trim().isEmpty()) {
            System.out.println("[RECEIPT-DEBUG] Adding address label to Layout");
            Label address = new Label(settings.getCompanyAddress());
            address.setStyle("-fx-font-size: 9px; -fx-text-fill: black;");
            address.setMaxWidth(Double.MAX_VALUE);
            address.setAlignment(Pos.CENTER);
            container.getChildren().add(address);
        }

        if (settings.getCompanyContact() != null && !settings.getCompanyContact().trim().isEmpty()) {
            System.out.println("[RECEIPT-DEBUG] Adding contact label to Layout");
            Label contact = new Label("Tel: " + settings.getCompanyContact());
            contact.setStyle("-fx-font-size: 9px; -fx-text-fill: black;");
            contact.setMaxWidth(Double.MAX_VALUE);
            contact.setAlignment(Pos.CENTER);
            container.getChildren().add(contact);
        }

        container.getChildren().add(createSeparator());

        // --- Transaction Info ---
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Label dateLabel = new Label("Date: " + dtf.format(LocalDateTime.now()));
        Label saleIdLabel = new Label("Inv #: " + saleId);
        Label customerLabel = new Label("Customer: " + (customer != null ? customer.getContact() : "Walk-in"));

        String transactionStyle = "-fx-font-size: 10px; -fx-text-fill: black;";
        dateLabel.setStyle(transactionStyle);
        saleIdLabel.setStyle(transactionStyle);
        customerLabel.setStyle(transactionStyle);

        container.getChildren().addAll(dateLabel, saleIdLabel, customerLabel, createSeparator());

        // --- Items Table Header ---
        HBox header = new HBox(5);
        header.setStyle("-fx-background-color: white;");
        Label itemH = new Label("Item");
        Label qtyH = new Label("Qty");
        Label priceH = new Label("Price");
        Label amountH = new Label("Total");

        String headerStyle = "-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 11px;";
        itemH.setStyle(headerStyle);
        qtyH.setStyle(headerStyle);
        priceH.setStyle(headerStyle);
        amountH.setStyle(headerStyle);

        itemH.setPrefWidth(100);
        qtyH.setPrefWidth(40);
        priceH.setPrefWidth(60);
        amountH.setPrefWidth(60);
        amountH.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(itemH, qtyH, priceH, amountH);
        container.getChildren().add(header);

        // --- Items List ---
        for (SaleItem item : items) {
            HBox row = new HBox(5);
            row.setStyle("-fx-background-color: white;");
            Label name = new Label(item.getName());
            Label qty = new Label(String.valueOf(item.getQuantity()));
            Label price = new Label(String.format("%.2f", item.getSalePrice()));
            Label amount = new Label(String.format("%.2f", item.getAmount()));

            String rowStyle = "-fx-font-size: 10px; -fx-text-fill: black;";
            name.setStyle(rowStyle);
            qty.setStyle(rowStyle);
            price.setStyle(rowStyle);
            amount.setStyle(rowStyle);

            name.setPrefWidth(100);
            name.setWrapText(true);
            qty.setPrefWidth(40);
            price.setPrefWidth(60);
            amount.setPrefWidth(60);
            amount.setAlignment(Pos.CENTER_RIGHT);

            row.getChildren().addAll(name, qty, price, amount);
            container.getChildren().add(row);
        }

        container.getChildren().add(createSeparator());

        // --- Totals Section ---
        container.getChildren().add(createTotalRow("SUBTOTAL", total));
        container.getChildren().add(createTotalRow("TAX (LKR)", BigDecimal.ZERO));
        container.getChildren().add(createTotalRow("TOTAL", total, true));
        container.getChildren().add(createSeparator());
        container.getChildren().add(createTotalRow("PAID", paid));
        container.getChildren().add(createTotalRow("CHANGE", change));

        container.getChildren().add(createSeparator());

        // --- Footer ---
        Label footer = new Label(settings.getFooterMessage1());
        footer.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: black;");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);

        Label footer2 = new Label(settings.getFooterMessage2());
        footer2.setStyle("-fx-font-size: 9px; -fx-text-fill: black;");
        footer2.setMaxWidth(Double.MAX_VALUE);
        footer2.setAlignment(Pos.CENTER);

        container.getChildren().addAll(footer, footer2);

        return container;
    }

    private static void addStringHeader(VBox container, InvoiceSettingsDTO settings) {
        Label title = new Label(settings.getCompanyName());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        container.getChildren().add(title);
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
        row.setStyle("-fx-background-color: white;");

        Label label = new Label(labelText);
        Label val = new Label(String.format("LKR %.2f", value));

        HBox.setHgrow(label, Priority.ALWAYS);
        val.setPrefWidth(100);
        val.setAlignment(Pos.CENTER_RIGHT);

        if (bold) {
            String boldStyle = "-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: black;";
            label.setStyle(boldStyle);
            val.setStyle(boldStyle);
        } else {
            String standardStyle = "-fx-font-size: 10px; -fx-text-fill: black;";
            label.setStyle(standardStyle);
            val.setStyle(standardStyle);
        }

        row.getChildren().addAll(label, val);
        return row;
    }
}
