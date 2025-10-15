package com.qaldrin.posclient;

import com.qaldrin.posclient.dto.ProductWithQuantityDTO;
import com.qaldrin.posclient.model.SaleItem;
import com.qaldrin.posclient.service.ApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    @FXML private TableView<SaleItem> saleTable;
    @FXML private TableColumn<SaleItem, Long> colId;
    @FXML private TableColumn<SaleItem, String> colName;
    @FXML private TableColumn<SaleItem, String> colCategory;
    @FXML private TableColumn<SaleItem, BigDecimal> colSalePrice;
    @FXML private TableColumn<SaleItem, Integer> colQuantity;
    @FXML private TableColumn<SaleItem, BigDecimal> colAmount;

    @FXML private TextField searchField;
    @FXML private ListView<ProductWithQuantityDTO> searchDropdown;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    private final ApiService apiService = new ApiService();
    private final ObservableList<SaleItem> saleItems = FXCollections.observableArrayList();
    private static final double TAX_RATE = 0.10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableColumns();
        saleTable.setItems(saleItems);
        setupSearchListener();
        setupDropdownClickHandler();
        updateSummaryLabels();
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        colSalePrice.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty());
        colQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty());

        colQuantity.setCellFactory(column -> new TableCell<SaleItem, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    SaleItem saleItem = getTableView().getItems().get(getIndex());
                    TextField quantityField = new TextField(item.toString());
                    quantityField.setPrefWidth(60);
                    quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal.matches("\\d*")) {
                            quantityField.setText(oldVal);
                        }
                    });
                    quantityField.setOnAction(e -> {
                        try {
                            int newQuantity = Integer.parseInt(quantityField.getText());
                            if (newQuantity > 0) {
                                saleItem.setQuantity(newQuantity);
                                updateSummaryLabels();
                            } else {
                                showAlert("Invalid Quantity", "Quantity must be greater than 0");
                                quantityField.setText(item.toString());
                            }
                        } catch (NumberFormatException ex) {
                            showAlert("Invalid Input", "Please enter a valid number");
                            quantityField.setText(item.toString());
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
                } else {
                    setText(String.format("%s - %s ($%.2f) [Stock: %d]",
                            product.getName(),
                            product.getCategory(),
                            product.getSalePrice(),
                            product.getAvailableQuantity()));
                }
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
        if (product.getAvailableQuantity() == null || product.getAvailableQuantity() <= 0) {
            showAlert("Out of Stock", "Product " + product.getName() + " is out of stock!");
            return;
        }

        Optional<SaleItem> existingItem = saleItems.stream()
                .filter(item -> item.getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            SaleItem item = existingItem.get();
            int newQuantity = item.getQuantity() + 1;
            if (newQuantity > product.getAvailableQuantity()) {
                showAlert("Insufficient Stock",
                        String.format("Only %d items available in stock", product.getAvailableQuantity()));
                return;
            }
            item.setQuantity(newQuantity);
        } else {
            SaleItem newItem = new SaleItem(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getBarcode(),
                    product.getSalePrice(),
                    1
            );
            saleItems.add(newItem);
        }

        saleTable.refresh();
        updateSummaryLabels();
    }

    private void updateSummaryLabels() {
        BigDecimal subtotal = saleItems.stream()
                .map(SaleItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(TAX_RATE));
        BigDecimal total = subtotal.add(tax);

        subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
        taxLabel.setText(String.format("TAX: $%.2f", tax));
        totalLabel.setText(String.format("Total: $%.2f", total));
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

    @FXML
    private void removeSelectedItem() {
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
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}