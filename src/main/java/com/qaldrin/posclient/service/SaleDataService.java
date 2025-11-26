package com.qaldrin.posclient.service;

import com.qaldrin.posclient.controller.AddCustomerFormController;
import com.qaldrin.posclient.dto.CustomerDTO;
import com.qaldrin.posclient.model.PausedSaleData;
import com.qaldrin.posclient.model.SaleItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    private List<PausedSaleData> pausedSales = new ArrayList<>();

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

    public void pauseCurrentSale() {
        if (currentSaleItems == null || currentSaleItems.isEmpty()) {
            return;
        }

        String saleId = AddCustomerFormController.getTempSaleId();
        CustomerDTO customer = AddCustomerFormController.getTempCustomerDTO();

        if (saleId == null || customer == null) {
            return;
        }

        PausedSaleData pausedSale = new PausedSaleData(
                saleId, customer, currentSaleItems, subtotal, tax, total
        );

        pausedSales.add(pausedSale);
        System.out.println("Sale paused: " + saleId + " for customer: " + customer.getContact());

        // Clear current sale data
        clearSaleData();
        AddCustomerFormController.clearTempCustomerDTO();
    }

    public List<PausedSaleData> getPausedSales() {
        return pausedSales;
    }
    public void resumePausedSale(PausedSaleData pausedSale) {
        // Restore the sale data
        this.currentSaleItems = FXCollections.observableArrayList(pausedSale.getSaleItems());
        this.currentCustomer = pausedSale.getCustomer();
        this.subtotal = pausedSale.getSubtotal();
        this.tax = pausedSale.getTax();
        this.total = pausedSale.getTotal();

        // Restore temp storage
        AddCustomerFormController.setTempCustomerDTO(
                pausedSale.getCustomer(),
                pausedSale.getSaleId()
        );

        // Remove from paused list
        pausedSales.remove(pausedSale);

        System.out.println("Resumed sale: " + pausedSale.getSaleId());
    }
    public void removePausedSale(PausedSaleData pausedSale) {
        pausedSales.remove(pausedSale);
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
