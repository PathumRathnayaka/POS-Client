package com.qaldrin.posclient.dto;

import java.math.BigDecimal;

public class SaleItemDTO {
    private Long productId;
    private String name;           // Server uses "name", not "productName"
    private String category;
    private String barcode;
    private Integer quantity;
    private BigDecimal salePrice;  // Server uses "salePrice", not "unitPrice"
    private BigDecimal amount;     // Server uses "amount", not "totalPrice" or "subTotal"

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
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

    // Compatibility methods (optional - for backward compatibility)
    public String getProductName() {
        return name;
    }

    public void setProductName(String productName) {
        this.name = productName;
    }

    public BigDecimal getUnitPrice() {
        return salePrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.salePrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return amount;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.amount = totalPrice;
    }

    public BigDecimal getSubTotal() {
        return amount;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.amount = subTotal;
    }

    @Override
    public String toString() {
        return "SaleItemDTO{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", salePrice=" + salePrice +
                ", amount=" + amount +
                '}';
    }
}