package com.qaldrin.posclient.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.qaldrin.posclient.dto.*;
import com.qaldrin.posclient.util.ApiConfig;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service class for making REST API calls to the POS Server.
 *
 * All IDs (product batch id, customer id, stock product id) are Strings
 * to match the server's data model.
 */
public class ApiService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson;

    // Global session state
    private static String currentCashierId;
    private static String currentCashierName;

    public ApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    // =========================================================================
    // Connection
    // =========================================================================

    /** Test connection to server. */
    public boolean testConnection() {
        try {
            Request request = new Request.Builder()
                    .url(ApiConfig.getProductsUrl())
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // PIN / Password
    // =========================================================================

    /**
     * Check whether an admin/cashier PIN has been set on the server.
     * GET /api/password/has-pin
     */
    public boolean hasPin() throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getPinHasPinUrl())
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return false;
            }
            String body = response.body().string();
            JsonObject json = gson.fromJson(body, JsonObject.class);
            return json.has("hasPin") && json.get("hasPin").getAsBoolean();
        }
    }

    /**
     * Validate a PIN entered by the cashier.
     * POST /api/password/validate
     *
     * @return true if PIN is valid (or no PIN is set)
     */
    public boolean validatePin(String pin) throws IOException {
        PinRequestDTO requestDTO = new PinRequestDTO(pin);
        String json = gson.toJson(requestDTO);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.getPinValidateUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                currentCashierId = null;
                currentCashierName = null;
                return false;
            }
            String responseBody = response.body().string();
            JsonObject jsonObj = gson.fromJson(responseBody, JsonObject.class);
            boolean valid = jsonObj.has("valid") && jsonObj.get("valid").getAsBoolean();

            if (valid) {
                if (jsonObj.has("userId") && !jsonObj.get("userId").isJsonNull()) {
                    currentCashierId = jsonObj.get("userId").getAsString();
                }
                if (jsonObj.has("cashierName") && !jsonObj.get("cashierName").isJsonNull()) {
                    currentCashierName = jsonObj.get("cashierName").getAsString();
                }
                System.out.println(
                        "Login successful for cashier: " + currentCashierName + " (ID: " + currentCashierId + ")");
            } else {
                currentCashierId = null;
                currentCashierName = null;
            }

            return valid;
        }
    }

    public static String getCurrentCashierId() {
        return currentCashierId;
    }

    public static String getCurrentCashierName() {
        return currentCashierName;
    }

    // =========================================================================
    // Products
    // =========================================================================

    /**
     * Get all products from server.
     * GET /api/products
     */
    public List<ProductWithQuantityDTO> getAllProducts() throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getProductsUrl())
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch products: " + response.code());
            }
            String responseBody = response.body().string();
            Type listType = new TypeToken<List<ProductWithQuantityDTO>>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    /**
     * Search products by name, barcode or searchableBarcodes.
     * GET /api/products/search?query=&limit=
     */
    public List<ProductWithQuantityDTO> searchProducts(String query, int limit) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String url = ApiConfig.getProductsSearchUrl(query, limit);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to search products: " + response.code());
            }
            String responseBody = response.body().string();
            Type listType = new TypeToken<List<ProductWithQuantityDTO>>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    /**
     * Get a single product (batch) by its ID.
     * GET /api/products/{id}
     */
    public ProductWithQuantityDTO getProductById(String id) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getProductByIdUrl(id))
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 404)
                return null;
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch product by id: " + response.code());
            }
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, ProductWithQuantityDTO.class);
        }
    }

    /**
     * Look up a product by barcode (exact match on primary or searchable barcodes).
     * GET /api/products/barcode/{barcode}
     */
    public ProductWithQuantityDTO getProductByBarcode(String barcode) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getProductByBarcodeUrl(barcode))
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 404)
                return null;
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch product by barcode: " + response.code());
            }
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, ProductWithQuantityDTO.class);
        }
    }

    /**
     * Get all unique product categories.
     * GET /api/products/categories
     */
    public List<String> getCategories() throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getProductsCategoriesUrl())
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch categories: " + response.code());
            }
            String responseBody = response.body().string();
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    /**
     * Get all products in a specific category.
     * GET /api/products/category/{category}
     */
    public List<ProductWithQuantityDTO> getProductsByCategory(String category) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getProductsByCategoryUrl(category))
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch products by category: " + response.code());
            }
            String responseBody = response.body().string();
            Type listType = new TypeToken<List<ProductWithQuantityDTO>>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    // =========================================================================
    // Customers
    // =========================================================================

    /**
     * Save (get-or-create) customer on server.
     * POST /api/customers
     * Server returns CustomerResponseDTO with customerId as String.
     */
    public CustomerDTO saveCustomer(CustomerDTO customer) throws IOException {
        String json = gson.toJson(customer);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.getCustomersUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Failed to save customer: " + response.code() + " - " + errorBody);
            }
            String responseBody = response.body().string();
            System.out.println("Customer response: " + responseBody);

            // Parse server's CustomerResponseDTO { success, message, customerId, contact,
            // email }
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            CustomerDTO savedCustomer = new CustomerDTO();
            // customerId is a String on the server side
            if (jsonResponse.has("customerId") && !jsonResponse.get("customerId").isJsonNull()) {
                savedCustomer.setId(jsonResponse.get("customerId").getAsString());
            }
            if (jsonResponse.has("contact") && !jsonResponse.get("contact").isJsonNull()) {
                savedCustomer.setContact(jsonResponse.get("contact").getAsString());
            }
            if (jsonResponse.has("email") && !jsonResponse.get("email").isJsonNull()) {
                savedCustomer.setEmail(jsonResponse.get("email").getAsString());
            }

            System.out.println("Customer processed - ID: " + savedCustomer.getId());
            return savedCustomer;
        }
    }

    /**
     * Find customer by contact number.
     * GET /api/customers/contact/{contact}
     * Returns null if not found.
     */
    public CustomerDTO getCustomerByContact(String contact) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getCustomerByContactUrl(contact))
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 404)
                return null;
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch customer: " + response.code());
            }
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            CustomerDTO dto = new CustomerDTO();
            if (jsonResponse.has("customerId") && !jsonResponse.get("customerId").isJsonNull()) {
                dto.setId(jsonResponse.get("customerId").getAsString());
            }
            if (jsonResponse.has("contact") && !jsonResponse.get("contact").isJsonNull()) {
                dto.setContact(jsonResponse.get("contact").getAsString());
            }
            if (jsonResponse.has("email") && !jsonResponse.get("email").isJsonNull()) {
                dto.setEmail(jsonResponse.get("email").getAsString());
            }
            return dto;
        }
    }

    /**
     * Get all customers.
     * GET /api/customers
     */
    public List<CustomerDTO> getAllCustomers() throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getCustomersUrl())
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch customers: " + response.code());
            }
            String responseBody = response.body().string();
            Type listType = new TypeToken<List<CustomerDTO>>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    // =========================================================================
    // Payments
    // =========================================================================

    /**
     * Process payment.
     * POST /api/payments/process
     */
    public boolean processPayment(PaymentRequestDTO paymentRequest) throws IOException {
        String json = gson.toJson(paymentRequest);
        System.out.println("Sending payment request: " + json);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.getPaymentsUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("Payment failed: " + errorBody);
                throw new IOException("Failed to process payment: " + response.code() + " - " + errorBody);
            }
            String responseBody = response.body().string();
            System.out.println("Payment response: " + responseBody);
            PaymentResponseDTO paymentResponse = gson.fromJson(responseBody, PaymentResponseDTO.class);
            return paymentResponse != null && paymentResponse.isSuccess();
        }
    }

    // =========================================================================
    // Stock
    // =========================================================================

    /**
     * Update (reduce) stock after a sale.
     * POST /api/stock/update
     *
     * Uses String productId to match server's StockUpdateDTO.
     */
    public boolean updateStock(List<StockUpdateItem> stockUpdates) throws IOException {
        String json = gson.toJson(stockUpdates);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.getStockUpdateUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("Stock update failed: " + errorBody);
                throw new IOException("Failed to update stock: " + response.code());
            }
            System.out.println("Stock updated successfully");
            return true;
        }
    }

    /**
     * Item used in stock update requests.
     * productId is a String matching server's StockUpdateDTO.
     */
    public static class StockUpdateItem {
        private String productId;
        private BigDecimal quantity;

        public StockUpdateItem() {
        }

        public StockUpdateItem(String productId, BigDecimal quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "StockUpdateItem{productId='" + productId + "', quantity=" + quantity + '}';
        }
    }

    // =========================================================================
    // Wallet
    // =========================================================================

    public WalletDTO getWalletByContact(String contact) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getWalletByContactUrl(contact))
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404)
                    return null;
                throw new IOException("Failed to fetch wallet: " + response.code());
            }
            String responseBody = response.body().string();
            WalletResponseDTO walletResponse = gson.fromJson(responseBody, WalletResponseDTO.class);
            return toWalletDTO(walletResponse);
        }
    }

    public BigDecimal getWalletBalance(String contact) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getWalletBalanceUrl(contact))
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404)
                    return BigDecimal.ZERO;
                throw new IOException("Failed to fetch wallet balance: " + response.code());
            }
            String responseBody = response.body().string();
            WalletBalanceDTO walletDTO = gson.fromJson(responseBody, WalletBalanceDTO.class);
            return walletDTO.getBalance() != null ? walletDTO.getBalance() : BigDecimal.ZERO;
        }
    }

    public WalletDTO addToWallet(String customerContact, BigDecimal amount) throws IOException {
        WalletTransactionDTO transactionDTO = new WalletTransactionDTO(customerContact, amount);
        String json = gson.toJson(transactionDTO);
        RequestBody body = RequestBody.create(json, JSON);

        Request httpRequest = new Request.Builder()
                .url(ApiConfig.getWalletAddUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Failed to add to wallet: " + response.code() + " - " + errorBody);
            }
            String responseBody = response.body().string();
            System.out.println("Add to wallet response: " + responseBody);
            WalletResponseDTO walletResponse = gson.fromJson(responseBody, WalletResponseDTO.class);
            return toWalletDTO(walletResponse);
        }
    }

    public WalletDTO deductFromWallet(String customerContact, BigDecimal amount) throws IOException {
        WalletTransactionDTO transactionDTO = new WalletTransactionDTO(customerContact, amount);
        String json = gson.toJson(transactionDTO);
        RequestBody body = RequestBody.create(json, JSON);

        Request httpRequest = new Request.Builder()
                .url(ApiConfig.getWalletDeductUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Failed to deduct from wallet: " + response.code() + " - " + errorBody);
            }
            String responseBody = response.body().string();
            System.out.println("Deduct from wallet response: " + responseBody);
            WalletResponseDTO walletResponse = gson.fromJson(responseBody, WalletResponseDTO.class);
            return toWalletDTO(walletResponse);
        }
    }

    /**
     * Check if customer has a wallet.
     * GET /api/wallets/exists/{customerId}
     * customerId is a String on the server.
     */
    public boolean checkWalletExists(String customerId) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getWalletExistsUrl(customerId))
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                return false;
            String responseBody = response.body().string();
            WalletExistsDTO walletDTO = gson.fromJson(responseBody, WalletExistsDTO.class);
            return walletDTO.isExists();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private WalletDTO toWalletDTO(WalletResponseDTO walletResponse) {
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setSuccess(walletResponse.isSuccess());
        walletDTO.setMessage(walletResponse.getMessage());
        walletDTO.setCustomerContact(walletResponse.getCustomerContact());
        walletDTO.setBalance(walletResponse.getBalance());
        walletDTO.setLastUpdated(walletResponse.getLastUpdated());
        return walletDTO;
    }
}