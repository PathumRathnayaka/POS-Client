package com.qaldrin.posclient.dto;

import java.math.BigDecimal;

/**
 * Wallet DTO for client-server communication
 */
public class WalletDTO {
    private Long walletId;
    private String customerContact;
    private BigDecimal balance;
    private String lastUpdated;
    private boolean success;
    private String message;
    private Boolean walletExists;

    // Constructors
    public WalletDTO() {}

    public WalletDTO(String customerContact, BigDecimal balance) {
        this.customerContact = customerContact;
        this.balance = balance;
    }

    // Getters and Setters
    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getWalletExists() {
        return walletExists;
    }

    public void setWalletExists(Boolean walletExists) {
        this.walletExists = walletExists;
    }
}
