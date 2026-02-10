package com.qaldrin.posclient.model;

import javafx.beans.property.*;
import java.math.BigDecimal;

/**
 * Model class for Sale Item displayed in TableView
 */
public class SaleItem {
    private final LongProperty id;
    private final StringProperty name;
    private final StringProperty category;
    private final ObjectProperty<BigDecimal> salePrice;
    private final ObjectProperty<BigDecimal> quantity;
    private final ObjectProperty<BigDecimal> amount;
    private final StringProperty barcode;
    private final StringProperty unitType;

    public SaleItem(Long id, String name, String category, String barcode,
            BigDecimal salePrice, BigDecimal quantity, String unitType) {
        this.id = new SimpleLongProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.barcode = new SimpleStringProperty(barcode);
        this.unitType = new SimpleStringProperty(unitType);
        this.salePrice = new SimpleObjectProperty<>(salePrice);
        this.quantity = new SimpleObjectProperty<>(quantity);
        this.amount = new SimpleObjectProperty<>(salePrice.multiply(quantity));
    }

    // ID Property
    public Long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public void setId(Long id) {
        this.id.set(id);
    }

    // Name Property
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    // Category Property
    public String getCategory() {
        return category.get();
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    // Barcode Property
    public String getBarcode() {
        return barcode.get();
    }

    public StringProperty barcodeProperty() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode.set(barcode);
    }

    // Unit Type Property
    public String getUnitType() {
        return unitType.get();
    }

    public StringProperty unitTypeProperty() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType.set(unitType);
    }

    // Sale Price Property
    public BigDecimal getSalePrice() {
        return salePrice.get();
    }

    public ObjectProperty<BigDecimal> salePriceProperty() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice.set(salePrice);
        updateAmount();
    }

    // Quantity Property
    public BigDecimal getQuantity() {
        return quantity.get();
    }

    public ObjectProperty<BigDecimal> quantityProperty() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity.set(quantity);
        updateAmount();
    }

    // Amount Property
    public BigDecimal getAmount() {
        return amount.get();
    }

    public ObjectProperty<BigDecimal> amountProperty() {
        return amount;
    }

    private void updateAmount() {
        BigDecimal newAmount = salePrice.get().multiply(quantity.get());
        this.amount.set(newAmount);
    }
}