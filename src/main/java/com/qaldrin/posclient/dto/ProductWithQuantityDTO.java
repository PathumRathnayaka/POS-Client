package com.qaldrin.posclient.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * DTO for Product with Quantity information
 * Matches the backend ProductWithQuantityDTO
 */
public class ProductWithQuantityDTO {
    private Long id;
    private String name;
    private String barcode;
    private String category;
    private BigDecimal buyPrice;
    private BigDecimal salePrice;

    // Map both "quantity" and "availableQuantity" from backend
    @SerializedName(value = "availableQuantity", alternate = { "quantity" })
    private BigDecimal availableQuantity;

    private String unitType;

    // Constructors
    public ProductWithQuantityDTO() {
    }

    public ProductWithQuantityDTO(Long id, String name, String barcode, String category,
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
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    @Override
    public String toString() {
        return name + " (" + category + ") - $" + salePrice + " [Stock: " +
                (availableQuantity != null ? availableQuantity.stripTrailingZeros().toPlainString() : "0") + "]";
    }
}