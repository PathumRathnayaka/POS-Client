package com.qaldrin.posclient.dto;

import java.math.BigDecimal;

public class WalletTransactionDTO {
    private String customerContact;
    private BigDecimal amount;

    public WalletTransactionDTO() {}

    public WalletTransactionDTO(String customerContact, BigDecimal amount) {
        this.customerContact = customerContact;
        this.amount = amount;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
