package com.qaldrin.posclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    @FXML private TableView<Object> saleTable;
    @FXML private TableColumn<Object, Integer> colId;
    @FXML private TableColumn<Object, String> colName;
    @FXML private TableColumn<Object, String> colCategory;
    @FXML private TableColumn<Object, Double> colSalePrice;
    @FXML private TableColumn<Object, Integer> colQuantity;
    @FXML private TableColumn<Object, Double> colAmount;

    @FXML private TextField searchField;
    @FXML private ListView<String> searchDropdown;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTableColumns();
        // Initialize table data
        // loadSaleData();
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSalePrice.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    // Method to load sale data into table
    public void loadSaleData() {
        // TODO: Implement data loading logic
        ObservableList<Object> saleData = FXCollections.observableArrayList();
        saleTable.setItems(saleData);
    }

    // Search functionality
    @FXML
    private void handleSearch() {
        // TODO: Implement search logic
        String searchText = searchField.getText();
        // Trigger search and show dropdown
        // performSearch(searchText);
    }

    // Update summary labels
    public void updateSummaryLabels(double subtotal, double tax, double total) {
        // TODO: Implement summary update logic
        subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
        taxLabel.setText(String.format("TAX: $%.2f", tax));
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    // Add item to sale
    public void addSaleItem(Object item) {
        // TODO: Implement add item logic
        // saleTable.getItems().add(item);
        // updateSummaryLabels();
    }

    // Remove selected item from sale
    @FXML
    private void removeSelectedItem() {
        // TODO: Implement remove item logic
        Object selectedItem = saleTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // saleTable.getItems().remove(selectedItem);
            // updateSummaryLabels();
        }
    }

    // Clear all sale items
    @FXML
    private void clearSale() {
        // TODO: Implement clear sale logic
        // saleTable.getItems().clear();
        // updateSummaryLabels();
    }

    // Calculate totals
    public void calculateTotals() {
        // TODO: Implement total calculation logic
        // double subtotal = calculateSubtotal();
        // double tax = calculateTax(subtotal);
        // double total = subtotal + tax;
        // updateSummaryLabels(subtotal, tax, total);
    }

    // Show/hide search dropdown
    public void showSearchDropdown(boolean show) {
        searchDropdown.setVisible(show);
        searchDropdown.setManaged(show);
    }

    // Handle dropdown item selection
    @FXML
    private void handleDropdownSelection() {
        // TODO: Implement dropdown selection logic
        String selectedItem = searchDropdown.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Add selected product to sale
            // addSaleItemFromSearch(selectedItem);
            searchField.setText(selectedItem);
            showSearchDropdown(false);
        }
    }

    // Refresh table data
    public void refreshTable() {
        // TODO: Implement table refresh logic
        // loadSaleData();
    }

    // Export sale data
    @FXML
    private void exportSaleData() {
        // TODO: Implement export functionality
    }

    // Print receipt
    @FXML
    private void printReceipt() {
        // TODO: Implement print functionality
    }

    // Handle table selection
    @FXML
    private void handleTableSelection() {
        // TODO: Handle row selection events
    }
}