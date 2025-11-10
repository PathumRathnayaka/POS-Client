package org.example.pos.service;

import org.example.pos.dao.WalletDAO;
import org.example.pos.entity.mySQLEntity.Customer;
import org.example.pos.entity.mySQLEntity.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletDAO walletDAO;

    /**
     * Get or create wallet for a customer
     */
    public Wallet getOrCreateWallet(Customer customer) {
        Optional<Wallet> existingWallet = walletDAO.findByCustomerId(customer.getId());

        if (existingWallet.isPresent()) {
            System.out.println("Found existing wallet for customer: " + customer.getContact() +
                    " with balance: " + existingWallet.get().getBalance());
            return existingWallet.get();
        } else {
            // Create new wallet with zero balance
            Wallet newWallet = new Wallet(customer, BigDecimal.ZERO);
            Wallet savedWallet = walletDAO.save(newWallet);
            System.out.println("Created new wallet for customer: " + customer.getContact());
            return savedWallet;
        }
    }
    public BigDecimal getTotalWalletBalance() {
        LocalDate today = LocalDate.now();
        return walletDAO.getTodayTotalWalletBalance(today);
    }

    /**
     * Set wallet balance to a specific amount (replaces existing balance)
     * Used when adding change to wallet - sets NEW balance instead of adding
     */
    @Transactional
    public Wallet setWalletBalance(Customer customer, BigDecimal newBalance) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (newBalance == null || newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        Optional<Wallet> walletOpt = walletDAO.findByCustomerId(customer.getId());
        Wallet wallet;

        if (walletOpt.isPresent()) {
            wallet = walletOpt.get();
            System.out.println("Setting wallet balance from Rs. " + wallet.getBalance() + " to Rs. " + newBalance);
        } else {
            wallet = new Wallet();
            wallet.setCustomer(customer);
            System.out.println("Creating new wallet with balance Rs. " + newBalance);
        }

        wallet.setBalance(newBalance);
        wallet.setLastUpdated(LocalDateTime.now());

        return walletDAO.save(wallet);
    }
    /**
     * Get wallet by customer ID
     */
    public Optional<Wallet> getWalletByCustomerId(Long customerId) {
        return walletDAO.findByCustomerId(customerId);
    }

    /**
     * Get wallet by customer contact
     */
    public Optional<Wallet> getWalletByCustomerContact(String contact) {
        return walletDAO.findByCustomerContact(contact);
    }

    /**
     * Add amount to wallet balance
     */
    public Wallet addToWallet(Customer customer, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = getOrCreateWallet(customer);
        BigDecimal oldBalance = wallet.getBalance();
        wallet.addToBalance(amount);
        Wallet savedWallet = walletDAO.save(wallet);

        System.out.println("Added " + amount + " to wallet for customer: " + customer.getContact() +
                " | Old Balance: " + oldBalance + " | New Balance: " + savedWallet.getBalance());

        return savedWallet;
    }

    /**
     * Deduct amount from wallet balance
     */
    public Wallet deductFromWallet(Customer customer, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = getOrCreateWallet(customer);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient wallet balance");
        }

        BigDecimal oldBalance = wallet.getBalance();
        wallet.deductFromBalance(amount);
        Wallet savedWallet = walletDAO.save(wallet);

        System.out.println("Deducted " + amount + " from wallet for customer: " + customer.getContact() +
                " | Old Balance: " + oldBalance + " | New Balance: " + savedWallet.getBalance());

        return savedWallet;
    }

    /**
     * Get wallet balance
     */
    public BigDecimal getBalance(Customer customer) {
        Wallet wallet = getOrCreateWallet(customer);
        return wallet.getBalance();
    }

    /**
     * Check if customer has wallet
     */
    public boolean hasWallet(Long customerId) {
        return walletDAO.existsByCustomerId(customerId);
    }

    /**
     * Update wallet balance directly
     */
    public Wallet updateBalance(Customer customer, BigDecimal newBalance) {
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        Wallet wallet = getOrCreateWallet(customer);
        wallet.setBalance(newBalance);
        return walletDAO.save(wallet);
    }
}