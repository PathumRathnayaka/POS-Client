package com.qaldrin.posclient.model;


import com.qaldrin.posclient.dto.CustomerDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;

public class PausedSaleData {
    private String saleId;
    private CustomerDTO customer;
    private ObservableList<SaleItem> saleItems;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String pauseTime;

    public PausedSaleData(String saleId, CustomerDTO customer, ObservableList<SaleItem> saleItems,
                          BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
        this.saleId = saleId;
        this.customer = customer;
        // ✅ Create a copy of the list to avoid reference issues
        this.saleItems = FXCollections.observableArrayList(saleItems);
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.pauseTime = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );
    }

    // Getters
    public String getSaleId() { return saleId; }
    public CustomerDTO getCustomer() { return customer; }
    public ObservableList<SaleItem> getSaleItems() { return saleItems; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public String getPauseTime() { return pauseTime; }

    public String getDisplayText() {
        return String.format("%s - %s - Rs. %.2f (Paused at %s)",
                saleId, customer.getContact(), total, pauseTime);
    }
}
