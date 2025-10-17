package com.qaldrin.posclient.service;

import com.qaldrin.posclient.dto.CustomerDTO;
import com.qaldrin.posclient.model.SaleItem;
import javafx.collections.ObservableList;

import java.math.BigDecimal;

/**
 * Singleton service to manage sale data across different forms
 */
public class SaleDataService {

    private static SaleDataService instance;

    private ObservableList<SaleItem> currentSaleItems;
    private CustomerDTO currentCustomer;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;

    private SaleDataService() {}

    public static SaleDataService getInstance() {
        if (instance == null) {
            instance = new SaleDataService();
        }
        return instance;
    }

    public void setSaleData(ObservableList<SaleItem> saleItems, BigDecimal subtotal,
                           BigDecimal tax, BigDecimal total) {
        this.currentSaleItems = saleItems;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
    }

    public void setCustomer(CustomerDTO customer) {
        this.currentCustomer = customer;
    }

    public ObservableList<SaleItem> getCurrentSaleItems() {
        return currentSaleItems;
    }

    public CustomerDTO getCurrentCustomer() {
        return currentCustomer;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void clearSaleData() {
        currentSaleItems = null;
        currentCustomer = null;
        subtotal = null;
        tax = null;
        total = null;
    }
}
