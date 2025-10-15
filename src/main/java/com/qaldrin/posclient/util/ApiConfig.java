package com.qaldrin.posclient.util;

/**
 * API Configuration for connecting to backend server
 */
public class ApiConfig {

    // Change this to your server PC's IP address
    private static String BASE_URL = "http://192.168.1.100:8080";

    // API Endpoints
    public static final String PRODUCTS_ENDPOINT = "/api/products";
    public static final String PRODUCTS_SEARCH_ENDPOINT = "/api/products/search";
    public static final String CUSTOMERS_ENDPOINT = "/api/customers";
    public static final String PAYMENTS_ENDPOINT = "/api/payments/process";
    public static final String STOCK_UPDATE_ENDPOINT = "/api/stock/update";

    /**
     * Get the base URL for API calls
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }

    /**
     * Set the base URL (useful for configuration)
     */
    public static void setBaseUrl(String url) {
        BASE_URL = url;
    }

    /**
     * Get full URL for an endpoint
     */
    public static String getFullUrl(String endpoint) {
        return BASE_URL + endpoint;
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
}