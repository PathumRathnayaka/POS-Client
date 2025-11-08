package com.qaldrin.posclient.util;

import java.util.prefs.Preferences;

/**
 * API Configuration for connecting to backend server
 * Change BASE_URL to match your server's IP address in the local network
 */
public class ApiConfig {

    // Keys for storing preferences
    private static final String PREF_IP_ADDRESS = "server_ip_address";
    private static final String PREF_PORT = "server_port";
    private static final String DEFAULT_IP = "192.168.1.100";
    private static final String DEFAULT_PORT = "8080";

    // Base URL - will be loaded from preferences or use default
    private static String BASE_URL;

    // Static initializer to load saved settings
    static {
        loadSavedSettings();
    }

    // API Endpoints
    public static final String PRODUCTS_ENDPOINT = "/api/products";
    public static final String PRODUCTS_SEARCH_ENDPOINT = "/api/products/search";
    public static final String CUSTOMERS_ENDPOINT = "/api/customers";
    public static final String CUSTOMER_BY_SALE_ID_ENDPOINT = "/api/customers/sale-id";
    public static final String PAYMENTS_ENDPOINT = "/api/payments/process";
    public static final String STOCK_UPDATE_ENDPOINT = "/api/stock/update";
    public static final String WALLET_BY_CONTACT_ENDPOINT = "/api/wallets/contact";
    public static final String WALLET_BALANCE_ENDPOINT = "/api/wallets/balance";
    public static final String WALLET_ADD_ENDPOINT = "/api/wallets/add";
    public static final String WALLET_DEDUCT_ENDPOINT = "/api/wallets/deduct";
    public static final String WALLET_EXISTS_ENDPOINT = "/api/wallets/exists";

    /**
     * Load saved settings from preferences
     */
    private static void loadSavedSettings() {
        Preferences prefs = Preferences.userNodeForPackage(ApiConfig.class);
        String savedIp = prefs.get(PREF_IP_ADDRESS, DEFAULT_IP);
        String savedPort = prefs.get(PREF_PORT, DEFAULT_PORT);
        BASE_URL = "http://" + savedIp + ":" + savedPort;
        System.out.println("ApiConfig loaded: " + BASE_URL);
    }

    /**
     * Save settings to preferences
     */
    private static void saveSettings(String ip, String port) {
        Preferences prefs = Preferences.userNodeForPackage(ApiConfig.class);
        prefs.put(PREF_IP_ADDRESS, ip);
        prefs.put(PREF_PORT, port);
        System.out.println("ApiConfig saved: " + ip + ":" + port);
    }

    /**
     * Get the base URL for API calls
     */
    public static String getBaseUrl() {
        if (BASE_URL == null || BASE_URL.isEmpty()) {
            loadSavedSettings();
        }
        return BASE_URL;
    }

    /**
     * Set the base URL (useful for configuration)
     * Also extracts and saves IP and port to preferences
     */
    public static void setBaseUrl(String url) {
        BASE_URL = url;

        // Extract IP and port from URL and save to preferences
        try {
            // Remove http:// or https://
            String cleanUrl = url.replaceFirst("^https?://", "");

            // Split by colon to get IP and port
            String[] parts = cleanUrl.split(":");
            if (parts.length == 2) {
                String ip = parts[0];
                String port = parts[1];
                saveSettings(ip, port);
            }
        } catch (Exception e) {
            System.err.println("Error parsing URL for saving: " + e.getMessage());
        }

        System.out.println("ApiConfig updated to: " + BASE_URL);
    }

    /**
     * Get full URL for an endpoint
     */
    public static String getFullUrl(String endpoint) {
        return getBaseUrl() + endpoint;
    }

    /**
     * Get products URL
     */
    public static String getProductsUrl() {
        return getFullUrl(PRODUCTS_ENDPOINT);
    }

    /**
     * Get products search URL
     */
    public static String getProductsSearchUrl(String query, int limit) {
        return getFullUrl(PRODUCTS_SEARCH_ENDPOINT) + "?query=" + query + "&limit=" + limit;
    }

    /**
     * Get customers URL
     */
    public static String getCustomersUrl() {
        return getFullUrl(CUSTOMERS_ENDPOINT);
    }

    /**
     * Get customer by sale ID URL
     */
    public static String getCustomerBySaleIdUrl(String saleId) {
        return getFullUrl(CUSTOMER_BY_SALE_ID_ENDPOINT) + "/" + saleId;
    }

    /**
     * Get payments URL
     */
    public static String getPaymentsUrl() {
        return getFullUrl(PAYMENTS_ENDPOINT);
    }

    /**
     * Get stock update URL
     */
    public static String getStockUpdateUrl() {
        return getFullUrl(STOCK_UPDATE_ENDPOINT);
    }

    /**
     * Get wallet by contact URL
     */
    public static String getWalletByContactUrl(String contact) {
        return getFullUrl(WALLET_BY_CONTACT_ENDPOINT) + "/" + contact;
    }

    /**
     * Get wallet balance URL
     */
    public static String getWalletBalanceUrl(String contact) {
        return getFullUrl(WALLET_BALANCE_ENDPOINT) + "/" + contact;
    }

    /**
     * Get wallet add URL
     */
    public static String getWalletAddUrl() {
        return getFullUrl(WALLET_ADD_ENDPOINT);
    }

    /**
     * Get wallet deduct URL
     */
    public static String getWalletDeductUrl() {
        return getFullUrl(WALLET_DEDUCT_ENDPOINT);
    }

    /**
     * Get wallet exists URL
     */
    public static String getWalletExistsUrl(Long customerId) {
        return getFullUrl(WALLET_EXISTS_ENDPOINT) + "/" + customerId;
    }
}