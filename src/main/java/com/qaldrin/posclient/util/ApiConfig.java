package com.qaldrin.posclient.util;

import java.util.prefs.Preferences;

/**
 * API Configuration for connecting to the POS Server.
 * Change the IP/port via the Settings form on the cashier PC.
 */
public class ApiConfig {

    // Keys for storing preferences
    private static final String PREF_IP_ADDRESS = "server_ip_address";
    private static final String PREF_PORT = "server_port";
    private static final String DEFAULT_IP = "localhost";
    private static final String DEFAULT_PORT = "8080";

    // Base URL - loaded from preferences or default
    private static String BASE_URL;

    // Static initializer
    static {
        loadSavedSettings();
    }

    // -------------------------------------------------------------------------
    // Endpoint constants (match POS Server REST controllers exactly)
    // -------------------------------------------------------------------------

    // Products
    public static final String PRODUCTS_ENDPOINT = "/api/products";
    public static final String PRODUCTS_SEARCH_ENDPOINT = "/api/products/search";
    public static final String PRODUCTS_BARCODE_ENDPOINT = "/api/products/barcode";
    public static final String PRODUCTS_CATEGORIES_ENDPOINT = "/api/products/categories";
    public static final String PRODUCTS_BY_CATEGORY_ENDPOINT = "/api/products/category";

    // Customers
    public static final String CUSTOMERS_ENDPOINT = "/api/customers";
    public static final String CUSTOMER_BY_CONTACT_ENDPOINT = "/api/customers/contact";

    // Payments
    public static final String PAYMENTS_ENDPOINT = "/api/payments/process";

    // Stock
    public static final String STOCK_UPDATE_ENDPOINT = "/api/stock/update";

    // Wallet
    public static final String WALLET_BY_CONTACT_ENDPOINT = "/api/wallets/contact";
    public static final String WALLET_BALANCE_ENDPOINT = "/api/wallets/balance";
    public static final String WALLET_ADD_ENDPOINT = "/api/wallets/add";
    public static final String WALLET_DEDUCT_ENDPOINT = "/api/wallets/deduct";
    public static final String WALLET_EXISTS_ENDPOINT = "/api/wallets/exists";

    // Password / PIN
    public static final String PIN_HAS_PIN_ENDPOINT = "/api/password/has-pin";
    public static final String PIN_VALIDATE_ENDPOINT = "/api/password/validate";

    // -------------------------------------------------------------------------
    // Settings helpers
    // -------------------------------------------------------------------------

    private static void loadSavedSettings() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(ApiConfig.class);
            String savedIp = prefs.get(PREF_IP_ADDRESS, DEFAULT_IP);
            String savedPort = prefs.get(PREF_PORT, DEFAULT_PORT);
            BASE_URL = "http://" + savedIp + ":" + savedPort;
            System.out.println("ApiConfig loaded: " + BASE_URL);
        } catch (Exception e) {
            BASE_URL = "http://" + DEFAULT_IP + ":" + DEFAULT_PORT;
            System.err.println("Error loading preferences: " + e.getMessage());
        }
    }

    private static void saveSettings(String ip, String port) {
        Preferences prefs = Preferences.userNodeForPackage(ApiConfig.class);
        prefs.put(PREF_IP_ADDRESS, ip);
        prefs.put(PREF_PORT, port);
        System.out.println("ApiConfig saved: " + ip + ":" + port);
    }

    public static String getBaseUrl() {
        if (BASE_URL == null || BASE_URL.isEmpty()) {
            loadSavedSettings();
        }
        return BASE_URL;
    }

    /**
     * Set the base URL (called from Settings form).
     * Parses and persists IP + port automatically.
     */
    public static void setBaseUrl(String url) {
        BASE_URL = url;
        try {
            String cleanUrl = url.replaceFirst("^https?://", "");
            String[] parts = cleanUrl.split(":");
            if (parts.length == 2) {
                saveSettings(parts[0], parts[1]);
            }
        } catch (Exception e) {
            System.err.println("Error parsing URL for saving: " + e.getMessage());
        }
        System.out.println("ApiConfig updated to: " + BASE_URL);
    }

    public static String getFullUrl(String endpoint) {
        return getBaseUrl() + endpoint;
    }

    // -------------------------------------------------------------------------
    // URL builders
    // -------------------------------------------------------------------------

    // --- Products ---

    public static String getProductsUrl() {
        return getFullUrl(PRODUCTS_ENDPOINT);
    }

    public static String getProductsSearchUrl(String query, int limit) {
        return getFullUrl(PRODUCTS_SEARCH_ENDPOINT)
                + "?query=" + encode(query) + "&limit=" + limit;
    }

    public static String getProductByIdUrl(String id) {
        return getFullUrl(PRODUCTS_ENDPOINT) + "/" + encode(id);
    }

    public static String getProductByBarcodeUrl(String barcode) {
        return getFullUrl(PRODUCTS_BARCODE_ENDPOINT) + "/" + encode(barcode);
    }

    public static String getProductsCategoriesUrl() {
        return getFullUrl(PRODUCTS_CATEGORIES_ENDPOINT);
    }

    public static String getProductsByCategoryUrl(String category) {
        return getFullUrl(PRODUCTS_BY_CATEGORY_ENDPOINT) + "/" + encode(category);
    }

    public static String getProductBatchesUrl(String productId, String brand) {
        String url = getFullUrl(PRODUCTS_ENDPOINT) + "/" + encode(productId) + "/batches";
        if (brand != null && !brand.isEmpty()) {
            url += "?brand=" + encode(brand);
        }
        return url;
    }

    // --- Customers ---

    public static String getCustomersUrl() {
        return getFullUrl(CUSTOMERS_ENDPOINT);
    }

    public static String getCustomerByContactUrl(String contact) {
        return getFullUrl(CUSTOMER_BY_CONTACT_ENDPOINT) + "/" + encode(contact);
    }

    // --- Payments ---

    public static String getPaymentsUrl() {
        return getFullUrl(PAYMENTS_ENDPOINT);
    }

    // --- Stock ---

    public static String getStockUpdateUrl() {
        return getFullUrl(STOCK_UPDATE_ENDPOINT);
    }

    // --- Wallet ---

    public static String getWalletByContactUrl(String contact) {
        return getFullUrl(WALLET_BY_CONTACT_ENDPOINT) + "/" + encode(contact);
    }

    public static String getWalletBalanceUrl(String contact) {
        return getFullUrl(WALLET_BALANCE_ENDPOINT) + "/" + encode(contact);
    }

    public static String getWalletAddUrl() {
        return getFullUrl(WALLET_ADD_ENDPOINT);
    }

    public static String getWalletDeductUrl() {
        return getFullUrl(WALLET_DEDUCT_ENDPOINT);
    }

    public static String getWalletExistsUrl(String customerId) {
        return getFullUrl(WALLET_EXISTS_ENDPOINT) + "/" + encode(customerId);
    }

    // --- PIN / Password ---

    public static String getPinHasPinUrl() {
        return getFullUrl(PIN_HAS_PIN_ENDPOINT);
    }

    public static String getPinValidateUrl() {
        return getFullUrl(PIN_VALIDATE_ENDPOINT);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /** Simple URL-encode for path segments. */
    private static String encode(String value) {
        if (value == null)
            return "";
        try {
            return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }
}