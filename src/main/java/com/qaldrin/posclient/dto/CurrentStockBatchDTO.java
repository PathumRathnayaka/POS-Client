package com.qaldrin.posclient.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Current Stock Batch information
 * Matches the backend CurrentStockBatchDTO.
 */
public class CurrentStockBatchDTO {
    private String id; // Product ID
    private String batchId; // Direct Batch ID for targeted updates
    private String name;
    private String barcode; // batch-level barcode
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal salePrice;
    private String category;
    private String supplierName;
    private LocalDate expireDate;
    private LocalDateTime createdDate;
    private BigDecimal quantity;
    private String batchCode;
    private String unitType;

    // Variation / batch extras
    private String variationId;
    private String variation; // size label
    private String brand;
    private String warehouseNo;
    private BigDecimal ourPrice;
    private BigDecimal purchasePrice;
    private String sourceType;
    private String invoiceNo;

    // Constructors
    public CurrentStockBatchDTO() {
    }

    public CurrentStockBatchDTO(String id, String batchId, String name, String barcode, BigDecimal discount,
            BigDecimal tax, BigDecimal salePrice, String category, String supplierName,
            LocalDate expireDate, LocalDateTime createdDate, BigDecimal quantity,
            String batchCode, String unitType, String variationId, String variation,
            String brand, String warehouseNo, BigDecimal ourPrice, BigDecimal purchasePrice,
            String sourceType, String invoiceNo) {
        this.id = id;
        this.batchId = batchId;
        this.name = name;
        this.barcode = barcode;
        this.discount = discount;
        this.tax = tax;
        this.salePrice = salePrice;
        this.category = category;
        this.supplierName = supplierName;
        this.expireDate = expireDate;
        this.createdDate = createdDate;
        this.quantity = quantity;
        this.batchCode = batchCode;
        this.unitType = unitType;
        this.variationId = variationId;
        this.variation = variation;
        this.brand = brand;
        this.warehouseNo = warehouseNo;
        this.ourPrice = ourPrice;
        this.purchasePrice = purchasePrice;
        this.sourceType = sourceType;
        this.invoiceNo = invoiceNo;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
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

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getVariationId() {
        return variationId;
    }

    public void setVariationId(String variationId) {
        this.variationId = variationId;
    }

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getWarehouseNo() {
        return warehouseNo;
    }

    public void setWarehouseNo(String warehouseNo) {
        this.warehouseNo = warehouseNo;
    }

    public BigDecimal getOurPrice() {
        return ourPrice;
    }

    public void setOurPrice(BigDecimal ourPrice) {
        this.ourPrice = ourPrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
}
