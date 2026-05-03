package com.qaldrin.posclient.controller;

import com.qaldrin.posclient.dto.ProductWithQuantityDTO;
import com.qaldrin.posclient.model.SaleItem;
import com.qaldrin.posclient.service.ApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

import com.qaldrin.posclient.dto.CurrentStockBatchDTO;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class DashboardContentController implements Initializable {

    @FXML
    private TableView<SaleItem> saleTable;
    @FXML
    private TableColumn<SaleItem, String> colId;
    @FXML
    private TableColumn<SaleItem, String> colName;
    @FXML
    private TableColumn<SaleItem, String> colCategory;
    @FXML
    private TableColumn<SaleItem, BigDecimal> colSalePrice;
    @FXML
    private TableColumn<SaleItem, BigDecimal> colOurPrice;
    @FXML
    private TableColumn<SaleItem, BigDecimal> colQuantity;
    @FXML
    private TableColumn<SaleItem, BigDecimal> colAmount;

    @FXML
    private TextField searchField;
    @FXML
    private ListView<ProductWithQuantityDTO> searchDropdown;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label taxLabel;
    @FXML
    private Label totalLabel;

    private final ApiService apiService = new ApiService();
    private final ObservableList<SaleItem> saleItems = FXCollections.observableArrayList();
    private static final double TAX_RATE = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableColumns();
        saleTable.setItems(saleItems);
        setupSearchListener();
        setupDropdownClickHandler();
        updateSummaryLabels();
    }

    private void initializeTableColumns() {
        // ID Column - Show row index (count)
        colId.setCellFactory(column -> new TableCell<SaleItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        colSalePrice.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty());
        colOurPrice.setCellValueFactory(cellData -> cellData.getValue().ourPriceProperty());
        colQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty());

        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");
        colName.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");
        colCategory.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");
        colSalePrice.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");
        colOurPrice.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");
        colQuantity.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");
        colAmount.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");

        colQuantity.setCellFactory(column -> new TableCell<SaleItem, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    SaleItem saleItem = getTableView().getItems().get(getIndex());
                    TextField quantityField = new TextField(item.stripTrailingZeros().toPlainString());
                    quantityField.setPrefWidth(60);
                    quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal.matches("\\d*(\\.\\d*)?")) {
                            quantityField.setText(oldVal);
                        }
                    });
                    quantityField.setOnAction(e -> {
                        try {
                            BigDecimal newQuantity = new BigDecimal(quantityField.getText());
                            if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
                                saleItem.setQuantity(newQuantity);
                                updateSummaryLabels();
                            } else {
                                showAlert("Invalid Quantity", "Quantity must be greater than 0");
                                quantityField.setText(item.stripTrailingZeros().toPlainString());
                            }
                        } catch (NumberFormatException ex) {
                            showAlert("Invalid Input", "Please enter a valid number");
                            quantityField.setText(item.stripTrailingZeros().toPlainString());
                        }
                    });
                    setGraphic(quantityField);
                }
            }
        });
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showSearchDropdown(false);
                return;
            }

            if (newValue.trim().length() >= 1) {
                performSearch(newValue.trim());
            }
        });

        searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !searchDropdown.isFocused()) {
                Platform.runLater(() -> showSearchDropdown(false));
            }
        });
    }

    private void setupDropdownClickHandler() {
        searchDropdown.setCellFactory(param -> new ListCell<ProductWithQuantityDTO>() {
            @Override
            protected void updateItem(ProductWithQuantityDTO product, boolean empty) {
                super.updateItem(product, empty);

                if (empty || product == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // Text - Show only Product and Brand as requested
                String brand = (product.getBrand() != null && !product.getBrand().equalsIgnoreCase("NO BRAND"))
                        ? product.getBrand()
                        : "Default";
                setText(String.format("%s  |  %s", product.getName(), brand));

                // Fix invisible items issue (important)
                setPrefHeight(40);

                // Dark theme
                setStyle(
                        "-fx-text-fill: #ecf0f1;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 8;" +
                                "-fx-background-color: #1f2a35;");
            }
        });

        searchDropdown.setOnMouseClicked(event -> {
            ProductWithQuantityDTO selected = searchDropdown.getSelectionModel().getSelectedItem();
            if (selected != null) {
                addProductToSale(selected);
                searchField.clear();
                showSearchDropdown(false);
            }
        });
    }

    private void performSearch(String query) {
        new Thread(() -> {
            try {
                List<ProductWithQuantityDTO> products = apiService.searchProducts(query, 10);
                Platform.runLater(() -> {
                    if (!products.isEmpty()) {
                        searchDropdown.setItems(FXCollections.observableArrayList(products));
                        showSearchDropdown(true);
                    } else {
                        showSearchDropdown(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showSearchDropdown(false);
                    System.err.println("Search error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void addProductToSale(ProductWithQuantityDTO product) {
        // Fetch variations in a separate thread to keep UI responsive
        new Thread(() -> {
            try {
                // Fetch batches for this product ID and brand
                List<CurrentStockBatchDTO> batches = apiService.getAvailableBatches(product.getId(),
                        product.getBrand());

                Platform.runLater(() -> {
                    if (batches.isEmpty()) {
                        showAlert("Out of Stock", "No available batches for " + product.getName());
                        return;
                    }

                    // Count unique variations (e.g. "Small", "Large")
                    Set<String> uniqueVariations = batches.stream()
                            .map(b -> (b.getVariation() == null || b.getVariation().isEmpty()) ? "DEFAULT"
                                    : b.getVariation())
                            .collect(Collectors.toSet());

                    if (uniqueVariations.size() > 1) {
                        // Multiple variations - show popup
                        showVariationSelectionDialog(product.getName(), batches);
                    } else {
                        // Only one variation - pick the best batch (FIFO)
                        // Server already returns them sorted by expiry/creation
                        addBatchToSale(batches.get(0));
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Error", "Failed to fetch variations: " + e.getMessage()));
            }
        }).start();
    }

    private void showVariationSelectionDialog(String productName, List<CurrentStockBatchDTO> batches) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/qaldrin/posclient/ProductVariationSelect-form.fxml"));
            Parent root = loader.load();

            ProductVariationSelectController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED); // Modern look
            stage.setScene(new Scene(root));

            controller.setData(productName, batches);
            controller.setOnBatchSelected(selectedBatch -> {
                addBatchToSale(selectedBatch);
                stage.close();
            });
            controller.setOnCancel(() -> stage.close());

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("UI Error", "Could not load variation selection dialog.");
        }
    }

    private void addBatchToSale(CurrentStockBatchDTO batch) {
        if (batch.getQuantity() == null || batch.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            showAlert("Out of Stock", "Variation " + batch.getVariation() + " is out of stock!");
            return;
        }

        // Search by Batch ID for exact row match
        Optional<SaleItem> existingItem = saleItems.stream()
                .filter(item -> item.getBatchId() != null && item.getBatchId().equals(batch.getBatchId()))
                .findFirst();

        if (existingItem.isPresent()) {
            SaleItem item = existingItem.get();
            BigDecimal newQuantity = item.getQuantity().add(BigDecimal.ONE);
            if (newQuantity.compareTo(batch.getQuantity()) > 0) {
                showAlert("Insufficient Stock",
                        String.format("Only %s items available for this variation",
                                batch.getQuantity().stripTrailingZeros().toPlainString()));
                return;
            }
            item.setQuantity(newQuantity);
        } else {
            // Include variation in name for better visibility in the table
            String displayName = batch.getName();
            if (batch.getVariation() != null && !batch.getVariation().isEmpty()
                    && !batch.getVariation().equalsIgnoreCase("DEFAULT")) {
                displayName += " (" + batch.getVariation() + ")";
            }

            SaleItem newItem = new SaleItem(
                    batch.getId(), // Still keep Product ID as base ID
                    displayName,
                    batch.getCategory(),
                    batch.getBarcode(),
                    batch.getSalePrice(),
                    batch.getOurPrice(), // Added
                    BigDecimal.ONE,
                    batch.getUnitType(),
                    batch.getBatchId()); // Pass batchId for uniqueness
            saleItems.add(newItem);
        }

        saleTable.refresh();
        updateSummaryLabels();
    }

    public void updateSummaryLabels() {
        BigDecimal subtotal = saleItems.stream()
                .map(SaleItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(TAX_RATE));
        BigDecimal total = subtotal.add(tax);

        subtotalLabel.setText(String.format("Subtotal: Rs %.2f", subtotal));
        taxLabel.setText(String.format("TAX: Rs %.2f", tax));
        totalLabel.setText(String.format("Total: Rs %.2f", total));
    }

    public ObservableList<SaleItem> getSaleItems() {
        return saleItems;
    }

    public BigDecimal getSubtotal() {
        return saleItems.stream()
                .map(SaleItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTax() {
        return getSubtotal().multiply(BigDecimal.valueOf(TAX_RATE));
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getTax());
    }

    public SaleItem getSelectedItem() {
        return saleTable.getSelectionModel().getSelectedItem();
    }

    public void removeSelectedItem() {
        SaleItem selectedItem = saleTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            saleItems.remove(selectedItem);
            updateSummaryLabels();
        } else {
            showAlert("No Selection", "Please select an item to remove");
        }
    }

    public void clearSale() {
        saleItems.clear();
        updateSummaryLabels();
    }

    private void showSearchDropdown(boolean show) {
        searchDropdown.setVisible(show);
        searchDropdown.setManaged(show);
        if (show) {
            searchDropdown.toFront();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}