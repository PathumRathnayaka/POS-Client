package com.qaldrin.posclient.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qaldrin.posclient.dto.CustomerDTO;
import com.qaldrin.posclient.dto.PaymentRequestDTO;
import com.qaldrin.posclient.dto.ProductWithQuantityDTO;
import com.qaldrin.posclient.util.ApiConfig;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service class for making REST API calls to the backend server
 */
public class ApiService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson;

    public ApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Test connection to server
     */
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

    /**
     * Get all products from server
     */
    public List<ProductWithQuantityDTO> getAllProducts() throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getProductsUrl())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch products: " + response);
            }

            String responseBody = response.body().string();
            Type listType = new TypeToken<List<ProductWithQuantityDTO>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    /**
     * Search products by query
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
                throw new IOException("Failed to search products: " + response);
            }

            String responseBody = response.body().string();
            Type listType = new TypeToken<List<ProductWithQuantityDTO>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    /**
     * Save customer to server
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
                System.err.println("Failed to save customer: " + errorBody);
                throw new IOException("Failed to save customer: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            System.out.println("Customer saved successfully: " + responseBody);
            return gson.fromJson(responseBody, CustomerDTO.class);
        }
    }

    /**
     * Get customer by sale ID
     */
    public CustomerDTO getCustomerBySaleId(String saleId) throws IOException {
        String url = ApiConfig.getCustomerBySaleIdUrl(saleId);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return null; // Customer not found
                }
                throw new IOException("Failed to fetch customer: " + response);
            }

            String responseBody = response.body().string();
            return gson.fromJson(responseBody, CustomerDTO.class);
        }
    }

    /**
     * Get all customers
     */
    public List<CustomerDTO> getAllCustomers() throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.getCustomersUrl())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch customers: " + response);
            }

            String responseBody = response.body().string();
            Type listType = new TypeToken<List<CustomerDTO>>(){}.getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    /**
     * Process payment
     */
    public boolean processPayment(PaymentRequestDTO paymentRequest) throws IOException {
        String json = gson.toJson(paymentRequest);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.getPaymentsUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("Payment failed: " + errorBody);
                throw new IOException("Failed to process payment: " + response.code());
            }

            String responseBody = response.body().string();
            System.out.println("Payment response: " + responseBody);
            return true;
        }
    }

    /**
     * Update stock after sale
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
     * Inner class for stock update
     */
    public static class StockUpdateItem {
        private Long productId;
        private Integer quantity;

        public StockUpdateItem(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}