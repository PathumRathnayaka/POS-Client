# POS Client Implementation Summary

## Overview
This implementation integrates the POS client application with the REST controllers on the server PC. Both systems communicate over the same local network using RJ45 cable connection.

## Implementation Flow

### 1. Product Search and Selection
**Location:** `DashboardContentController.java`

- User searches products in the `searchField`
- Search triggers API call: `GET /api/products/search?query={searchText}&limit=10`
- Results display in dropdown showing: product name, category, price, and available stock
- User clicks on a product to add it to the sale table
- Selected product is added to `saleTable` with quantity 1 (or increments if already present)
- Stock validation ensures quantity doesn't exceed available inventory

### 2. Add Customer
**Location:** `DashboardFormController.java` → `AddCustomerFormController.java`

- User clicks "Add Customer" button in Dashboard
- Opens popup form (`AddCustomer-form.fxml`)
- Auto-generates unique Sale ID: `SALE-{timestamp}`
- User enters:
  - Customer Contact (required, 10-15 digits)
  - Customer Email (optional)
- Validates input and saves to server
- API call: `POST /api/customers`
- Request body:
```json
{
  "saleId": "SALE-20251017123456",
  "contact": "1234567890",
  "email": "customer@example.com"
}
```
- On success, customer data is stored in `SaleDataService` singleton
- Shows success message to user

### 3. Payment Button Validation
**Location:** `DashboardFormController.java`

Before proceeding to payment, validates:
- Sale table must have at least one item
- Customer information must be saved
- If validation fails, shows appropriate error message
- If successful, stores sale data in `SaleDataService` and loads payment form

### 4. Payment Form
**Location:** `PaymentFormController.java`

#### Display Sale Information
- Loads sale items from `SaleDataService`
- Displays customer info (Sale ID, Contact)
- Shows all sale items in a VBox with:
  - Product name
  - Quantity
  - Unit price
  - Total amount
- Displays subtotal, tax (10%), and total

#### Payment Method Selection
- User selects payment method: Cash or Card
- Selected button highlights with green background
- Enables the "Paid Amount" text field
- Updates `selectedPaymentMethod` variable

#### Enter Paid Amount
- User enters the amount paid by customer
- Real-time calculation of change amount
- Change displays in green if sufficient, red if insufficient
- Validates that paid amount ≥ total amount

#### Done Button - Process Payment
When user clicks "Done":

1. **Validation:**
   - Payment method is selected
   - Paid amount is entered
   - Paid amount ≥ total amount

2. **Build Payment Request:**
   - Converts `SaleItem` list to `SaleItemDTO` list
   - Creates `PaymentRequestDTO` with all required fields

3. **API Call:** `POST /api/payments/process`
   Request body:
```json
{
  "saleId": "SALE-20251017123456",
  "customerContact": "1234567890",
  "customerEmail": "customer@example.com",
  "saleItems": [
    {
      "productId": 1,
      "productName": "Product Name",
      "barcode": "123456",
      "quantity": 2,
      "unitPrice": 10.00,
      "totalPrice": 20.00
    }
  ],
  "subTotal": 100.00,
  "taxAmount": 10.00,
  "discountAmount": 0.00,
  "totalAmount": 110.00,
  "paidAmount": 120.00,
  "changeAmount": 10.00,
  "paymentMethod": "Cash"
}
```

4. **Server Processing:**
   - Creates customer record in database
   - Creates sale record with all items
   - Returns success response

5. **Stock Update:**
   - After successful payment, updates stock inventory
   - API call: `POST /api/stock/update`
   Request body:
```json
[
  {
    "productId": 1,
    "quantity": 2
  },
  {
    "productId": 2,
    "quantity": 1
  }
]
```

6. **Success Display:**
   - Shows invoice message panel
   - Displays success alert with payment details
   - Clears sale data from `SaleDataService`
   - Prevents duplicate processing

## Key Components

### Services

#### `ApiService.java`
- Handles all REST API calls to server
- Methods:
  - `searchProducts(query, limit)` - Search products
  - `saveCustomer(customer)` - Save customer data
  - `processPayment(paymentRequest)` - Process complete payment
  - `updateStock(stockUpdates)` - Update inventory after sale

#### `SaleDataService.java` (NEW)
- Singleton service for sharing data between forms
- Stores:
  - Current sale items
  - Customer information
  - Financial totals (subtotal, tax, total)
- Provides data to payment form
- Cleared after successful payment

#### `ApiConfig.java`
- Centralized API configuration
- Base URL: `http://192.168.1.100:8080` (configure for your network)
- Endpoint definitions for all REST APIs

### Data Transfer Objects (DTOs)

#### `CustomerDTO.java`
- saleId, contact, email

#### `SaleItemDTO.java`
- productId, productName, barcode, quantity, unitPrice, totalPrice

#### `PaymentRequestDTO.java`
- Complete payment information including customer, items, and amounts

#### `PaymentResponseDTO.java`
- success, message, saleId, totalAmount, paymentMethod

## Configuration

### Network Setup
1. Connect client PC and server PC via RJ45 cable
2. Ensure both PCs are on the same subnet
3. Update `ApiConfig.java` with server PC's IP address:
```java
private static String BASE_URL = "http://SERVER_IP:8080";
```

### Server Requirements
- Spring Boot application running on port 8080
- REST controllers must be accessible:
  - ProductRestController
  - CustomerRestController
  - PaymentRestController
  - StockRestController

## Workflow Summary

1. **Start Sale:** User searches and selects products → Added to sale table
2. **Add Customer:** Click "Add Customer" → Enter details → Save to server
3. **Proceed to Payment:** Click "Payment" → Validates items and customer
4. **Select Payment Method:** Click "Cash" or "Card"
5. **Enter Amount:** Type paid amount → See change calculated
6. **Complete:** Click "Done" → Process payment → Update stock → Show success

## Error Handling

- Network errors: Shows user-friendly error messages
- Validation errors: Prevents invalid operations
- Server errors: Displays error details with troubleshooting tips
- Stock validation: Prevents overselling
- Duplicate payment prevention: Disables processing after first submission

## Benefits

- **Data Integrity:** All sales and customer data saved to server database
- **Inventory Management:** Automatic stock updates after each sale
- **Transaction Tracking:** Complete audit trail with sale IDs
- **User-Friendly:** Clear validation messages and success feedback
- **Reliable:** Asynchronous API calls don't block UI
- **Scalable:** Multiple client PCs can connect to same server
