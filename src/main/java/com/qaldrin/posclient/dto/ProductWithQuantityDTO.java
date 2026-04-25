package com.qaldrin.posclient.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * DTO for Product with Quantity information
 * Matches the backend ProductWithQuantityDTO exactly.
 *
 * Product hierarchy: Product → ProductVariation → ProductQuantityBatch
 * The 'id' field is the batch ID (String), not a numeric Long.
 */
public class ProductWithQuantityDTO {
    // Server returns id as String (batch UUID / composite key)
    private String id;
    private String name;
    private String barcode;
    private String category;
    private BigDecimal buyPrice;
    private BigDecimal salePrice;
    private BigDecimal discount;
    private BigDecimal tax;

    // Map both "quantity" and "availableQuantity" from backend
    @SerializedName(value = "quantity", alternate = { "availableQuantity" })
    private BigDecimal availableQuantity;

    private String unitType;
    private String brand;
    private String variation;
    private String searchableBarcodes;

    // Constructors
    public ProductWithQuantityDTO() {
    }

    public ProductWithQuantityDTO(String id, String name, String barcode, String category,
            BigDecimal buyPrice, BigDecimal salePrice, BigDecimal availableQuantity) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.category = category;
        this.buyPrice = buyPrice;
        this.salePrice = salePrice;
        this.availableQuantity = availableQuantity;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }

    public String getSearchableBarcodes() {
        return searchableBarcodes;
    }

    public void setSearchableBarcodes(String searchableBarcodes) {
        this.searchableBarcodes = searchableBarcodes;
    }

    @Override
    public String toString() {
        String qty = availableQuantity != null
                ? availableQuantity.stripTrailingZeros().toPlainString()
                : "0";
        return name + " (" + category + ") - Rs." + salePrice + " [Stock: " + qty + "]";
    }
}