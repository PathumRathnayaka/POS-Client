package com.qaldrin.posclient.dto;

import java.math.BigDecimal;

/**
 * DTO for sale line items sent to the server during payment processing.
 * productId is a String (batch ID) matching the server's SaleItemDTO.
 */
public class SaleItemDTO {
    private String productId; // String – matches server StockUpdateDTO.productId
    private String name;
    private String category;
    private String barcode;
    private BigDecimal quantity;
    private BigDecimal salePrice;
    private BigDecimal amount;

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Compatibility aliases
    public String getProductName() {
        return name;
    }

    public void setProductName(String n) {
        this.name = n;
    }

    public BigDecimal getUnitPrice() {
        return salePrice;
    }

    public void setUnitPrice(BigDecimal p) {
        this.salePrice = p;
    }

    public BigDecimal getTotalPrice() {
        return amount;
    }

    public void setTotalPrice(BigDecimal t) {
        this.amount = t;
    }

    public BigDecimal getSubTotal() {
        return amount;
    }

    public void setSubTotal(BigDecimal s) {
        this.amount = s;
    }

    @Override
    public String toString() {
        return "SaleItemDTO{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", salePrice=" + salePrice +
                ", amount=" + amount +
                '}';
    }
}