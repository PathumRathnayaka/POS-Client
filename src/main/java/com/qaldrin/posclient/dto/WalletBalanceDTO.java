package com.qaldrin.posclient.dto;

import java.math.BigDecimal;

/**
 * DTO for wallet balance responses
 * Matches server-side WalletBalanceDTO
 */
public class WalletBalanceDTO {
    private boolean success;
    private String message;
    private String customerContact;
    private BigDecimal balance;

    // Constructors
    public WalletBalanceDTO() {}

    // Getters and Setters
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

    @Override
    public String toString() {
        return "WalletBalanceDTO{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", customerContact='" + customerContact + '\'' +
                ", balance=" + balance +
                '}';
    }
}