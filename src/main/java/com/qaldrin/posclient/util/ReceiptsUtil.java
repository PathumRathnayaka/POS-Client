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
        String lang = settings.getLanguage();
        VBox container = new VBox(5);
        container.setPadding(new Insets(10));
        container.setPrefWidth(RECEIPT_WIDTH);
        container.setStyle("-fx-background-color: white;");

        // --- TRACE DEBUG ---
        System.out.println("[RECEIPT-DEBUG] Rendering Invoice (" + lang + ")...");

        renderHeader(container, settings);

        Label subTitle = new Label(settings.getCompanySlogan());
        String subtitleStyle = "-fx-font-size: 10px; -fx-text-fill: black;";
        subTitle.setStyle(subtitleStyle);
        subTitle.setMaxWidth(Double.MAX_VALUE);
        subTitle.setAlignment(Pos.CENTER);

        container.getChildren().add(subTitle);

        if (settings.getCompanyAddress() != null && !settings.getCompanyAddress().trim().isEmpty()) {
            Label address = new Label(settings.getCompanyAddress());
            address.setStyle("-fx-font-size: 9px; -fx-text-fill: black;");
            address.setMaxWidth(Double.MAX_VALUE);
            address.setAlignment(Pos.CENTER);
            container.getChildren().add(address);
        }

        if (settings.getCompanyContact() != null && !settings.getCompanyContact().trim().isEmpty()) {
            String contactPrefix = (lang != null && lang.equalsIgnoreCase("SINHALA")) ? "දු.ක: " : "Tel: ";
            Label contact = new Label(contactPrefix + settings.getCompanyContact());
            contact.setStyle("-fx-font-size: 9px; -fx-text-fill: black;");
            contact.setMaxWidth(Double.MAX_VALUE);
            contact.setAlignment(Pos.CENTER);
            container.getChildren().add(contact);
        }

        container.getChildren().add(createSeparator());

        // --- Transaction Info ---
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Label dateLabel = new Label(getLabel("Date:", lang) + " " + dtf.format(LocalDateTime.now()));
        Label saleIdLabel = new Label(getLabel("Inv #:", lang) + " " + saleId);

        String custVal = (customer != null ? customer.getContact() : getLabel("Walk-in", lang));
        Label customerLabel = new Label(getLabel("Customer:", lang) + " " + custVal);

        String transactionStyle = "-fx-font-size: 10px; -fx-text-fill: black;";
        dateLabel.setStyle(transactionStyle);
        saleIdLabel.setStyle(transactionStyle);
        customerLabel.setStyle(transactionStyle);

        container.getChildren().addAll(dateLabel, saleIdLabel, customerLabel, createSeparator());

        // --- Items Table Header ---
        HBox header = new HBox(5);
        header.setStyle("-fx-background-color: white;");
        Label itemH = new Label(getLabel("Item", lang));
        Label qtyH = new Label(getLabel("Qty", lang));
        Label mrpH = new Label(getLabel("MRP", lang)); // NEW
        Label ourPriceH = new Label(getLabel("Our", lang)); // NEW
        Label amountH = new Label(getLabel("Total", lang));

        String headerStyle = "-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 10px;";
        itemH.setStyle(headerStyle);
        qtyH.setStyle(headerStyle);
        mrpH.setStyle(headerStyle);
        ourPriceH.setStyle(headerStyle);
        amountH.setStyle(headerStyle);

        itemH.setPrefWidth(60);
        qtyH.setPrefWidth(30);
        mrpH.setPrefWidth(45);
        ourPriceH.setPrefWidth(60);
        amountH.setPrefWidth(65);
        amountH.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(itemH, qtyH, mrpH, ourPriceH, amountH);
        container.getChildren().add(header);

        // --- Items List ---
        for (SaleItem item : items) {
            HBox row = new HBox(5);
            row.setStyle("-fx-background-color: white;");
            Label name = new Label(item.getName());
            Label qty = new Label(String.valueOf(item.getQuantity()));
            Label mrp = new Label(String.format("%.2f", item.getSalePrice())); // MRP
            Label our = new Label(String.format("%.2f", item.getOurPrice())); // Our Price
            Label amount = new Label(String.format("%.2f", item.getAmount()));

            String rowStyle = "-fx-font-size: 9px; -fx-text-fill: black;";
            name.setStyle(rowStyle);
            qty.setStyle(rowStyle);
            mrp.setStyle(rowStyle);
            our.setStyle(rowStyle);
            amount.setStyle(rowStyle);

            name.setPrefWidth(60);
            name.setWrapText(true);
            qty.setPrefWidth(30);
            mrp.setPrefWidth(45);
            our.setPrefWidth(60);
            amount.setPrefWidth(65);
            amount.setAlignment(Pos.CENTER_RIGHT);

            row.getChildren().addAll(name, qty, mrp, our, amount);
            container.getChildren().add(row);
        }

        container.getChildren().add(createSeparator());

        // --- Totals Section ---
        container.getChildren().add(createTotalRow(getLabel("SUBTOTAL", lang), total, false, lang));
        container.getChildren().add(createTotalRow(getLabel("TAX (LKR)", lang), BigDecimal.ZERO, false, lang));
        container.getChildren().add(createTotalRow(getLabel("TOTAL", lang), total, true, lang));
        container.getChildren().add(createSeparator());
        container.getChildren().add(createTotalRow(getLabel("PAID", lang), paid, false, lang));
        container.getChildren().add(createTotalRow(getLabel("CHANGE", lang), change, false, lang));

        container.getChildren().add(createSeparator());

        // --- Profit / Savings Section ---
        BigDecimal totalProfit = items.stream()
                .map(item -> {
                    BigDecimal unitProfit = item.getSalePrice().subtract(item.getOurPrice());
                    return unitProfit.multiply(item.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalProfit.compareTo(BigDecimal.ZERO) > 0) {
            String profitLabel = getLabel("PROFIT_MESSAGE", lang);
            Label profitMsg = new Label(String.format("%s = %.2f", profitLabel, totalProfit.doubleValue()));
            profitMsg.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: black;");
            profitMsg.setMaxWidth(Double.MAX_VALUE);
            profitMsg.setAlignment(Pos.CENTER);
            container.getChildren().add(profitMsg);
        }

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

        // --- Powered by QALDRIN ---
        Label poweredBy = new Label("powered by QALDRIN");
        poweredBy.setStyle("-fx-font-size: 8px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        Label website = new Label("www.qaldrin.com");
        website.setStyle("-fx-font-size: 8px; -fx-text-fill: #7f8c8d;");

        VBox qaldrinBox = new VBox(2, poweredBy, website);
        qaldrinBox.setAlignment(Pos.CENTER);
        qaldrinBox.setPadding(new Insets(10, 0, 10, 0));
        container.getChildren().add(qaldrinBox);

        return container;
    }

    private static void renderHeader(VBox container, InvoiceSettingsDTO settings) {
        if (settings.getLogoPath() != null && !settings.getLogoPath().isEmpty()) {
            try {
                File file = new File(settings.getLogoPath());
                if (file.exists()) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString());
                    if (!img.isError() && img.getWidth() > 0) {
                        double MAX_LOGO_WIDTH = RECEIPT_WIDTH - 20;
                        double MAX_LOGO_HEIGHT = 80;
                        double imgWidth = img.getWidth();
                        double imgHeight = img.getHeight();
                        double scale = Math.min(MAX_LOGO_WIDTH / imgWidth, MAX_LOGO_HEIGHT / imgHeight);
                        if (scale > 1.0)
                            scale = 1.0;
                        double targetWidth = imgWidth * scale;
                        double targetHeight = imgHeight * scale;

                        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(targetWidth, targetHeight);
                        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                        gc.setFill(javafx.scene.paint.Color.WHITE);
                        gc.fillRect(0, 0, targetWidth, targetHeight);
                        gc.drawImage(img, 0, 0, targetWidth, targetHeight);

                        HBox logoWrapper = new HBox(canvas);
                        logoWrapper.setAlignment(Pos.CENTER);
                        logoWrapper.setStyle("-fx-background-color: white;");
                        container.getChildren().add(logoWrapper);
                        return;
                    }
                }
            } catch (Exception e) {
            }
        }
        addStringHeader(container, settings);
    }

    private static void addStringHeader(VBox container, InvoiceSettingsDTO settings) {
        Label title = new Label(settings.getCompanyName());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        container.getChildren().add(title);
    }

    private static Line createSeparator() {
        Line line = new Line(0, 0, RECEIPT_WIDTH - 20, 0);
        line.getStrokeDashArray().addAll(2.0, 2.0);
        line.setStyle("-fx-stroke: black;");
        return line;
    }

    private static HBox createTotalRow(String labelText, BigDecimal value, boolean bold, String lang) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: white;");

        Label label = new Label(labelText);
        String currency = (lang != null && lang.equalsIgnoreCase("SINHALA")) ? "රු. " : "LKR ";
        Label val = new Label(String.format(currency + "%.2f", value));

        HBox.setHgrow(label, Priority.ALWAYS);
        val.setPrefWidth(120);
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

    private static String getLabel(String key, String lang) {
        if (lang == null || !lang.equalsIgnoreCase("SINHALA")) {
            if (key.equals("PROFIT_MESSAGE")) {
                return "The profit you earned from this bill";
            }
            return key;
        }

        switch (key) {
            case "Date:":
                return "දිනය:";
            case "Inv #:":
                return "අංකය:";
            case "Customer:":
                return "පාරිභෝගිකයා:";
            case "Walk-in":
                return "සාමාන්‍ය";
            case "MRP":
                return "මිල";
            case "Our":
                return "අපේ මිල";
            case "Total":
                return "මුළු මුදල";
            case "SUBTOTAL":
                return "එකතුව";
            case "TAX (LKR)":
                return "බදු (රු.)";
            case "TOTAL":
                return "මුළු එකතුව";
            case "PAID":
                return "ගෙවූ මුදල";
            case "CHANGE":
                return "ඉතිරි මුදල";
            case "PROFIT_MESSAGE":
                return "මෙම බිලෙන් ඔබට ලැබුන ලාබය";
            default:
                return key;
        }
    }
}
