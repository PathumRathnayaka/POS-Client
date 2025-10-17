# POS Client Setup Instructions

## Prerequisites

### Server PC (Backend)
- Spring Boot POS application running
- MySQL/MongoDB databases configured
- Server listening on port 8080
- REST controllers deployed:
  - ProductRestController
  - CustomerRestController
  - PaymentRestController
  - StockRestController

### Client PC (This Application)
- Java JDK 11 or higher
- JavaFX SDK
- Maven (for building)

## Network Configuration

### Step 1: Connect PCs via RJ45 Cable
1. Connect both PCs using an Ethernet (RJ45) cable
2. Both PCs should be on the same local network

### Step 2: Find Server PC IP Address
On the server PC, run:
```bash
# Windows
ipconfig

# Linux/Mac
ifconfig
```
Look for the IPv4 address (e.g., 192.168.1.100)

### Step 3: Configure Client
Edit `ApiConfig.java`:
```java
// Line 12: Update with your server IP
private static String BASE_URL = "http://192.168.1.100:8080";
```
Replace `192.168.1.100` with your actual server IP address.

### Step 4: Test Connection
1. Ensure server is running
2. Open a browser on client PC
3. Try accessing: `http://SERVER_IP:8080/api/products`
4. Should see product list in JSON format

## Building the Application

```bash
# Make Maven wrapper executable
chmod +x mvnw

# Clean and compile
./mvnw clean compile

# Package the application
./mvnw clean package

# Run the application
./mvnw javafx:run
```

## Firewall Configuration

### Server PC Firewall
Allow incoming connections on port 8080:

**Windows:**
```powershell
netsh advfirewall firewall add rule name="POS Server" dir=in action=allow protocol=TCP localport=8080
```

**Linux:**
```bash
sudo ufw allow 8080/tcp
```

## Testing the Integration

### Test 1: Product Search
1. Launch the client application
2. Type a product name in the search field
3. Verify products appear in dropdown
4. Select a product
5. Verify it's added to the sale table

### Test 2: Customer Creation
1. Click "Add Customer" button
2. Enter phone number (10-15 digits)
3. Enter email (optional)
4. Click "Save"
5. Verify success message appears
6. Check server database for new customer record

### Test 3: Complete Sale
1. Add products to sale (Test 1)
2. Add customer information (Test 2)
3. Click "Payment" button
4. Select payment method (Cash or Card)
5. Enter paid amount (should be ≥ total)
6. Click "Done"
7. Verify success message
8. Check server database:
   - New customer record
   - New sale record
   - Sale items records
   - Updated product quantities

## Troubleshooting

### Problem: "Connection refused" error
**Solutions:**
- Verify server is running
- Check firewall settings
- Verify IP address in ApiConfig.java
- Ensure both PCs are on same network
- Try pinging server: `ping SERVER_IP`

### Problem: "Failed to save customer"
**Solutions:**
- Check server logs for errors
- Verify database connection on server
- Ensure REST controllers are deployed
- Check @CrossOrigin annotation on controllers

### Problem: Products not appearing
**Solutions:**
- Verify products exist in server database
- Check GET /api/products endpoint directly in browser
- Verify ProductService is returning data
- Check console logs for errors

### Problem: Payment fails
**Solutions:**
- Ensure customer is added first
- Verify sale items are in table
- Check payment amount is sufficient
- Review server logs for validation errors
- Verify MongoDB connection for sale records

### Problem: Stock not updating
**Solutions:**
- Check QuantityService on server
- Verify database permissions
- Check server logs for stock update errors
- Ensure product IDs are correct

## API Endpoints Reference

All endpoints relative to `BASE_URL` (e.g., http://192.168.1.100:8080):

### Products
- `GET /api/products` - Get all products
- `GET /api/products/search?query={text}&limit={n}` - Search products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/barcode/{barcode}` - Get product by barcode

### Customers
- `POST /api/customers` - Create customer
- `GET /api/customers/contact/{contact}` - Get customer by contact

### Payments
- `POST /api/payments/process` - Process complete payment

### Stock
- `POST /api/stock/update` - Update stock quantities

## Database Schema

### Customer Table (MySQL)
```sql
CREATE TABLE customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id VARCHAR(255) NOT NULL,
    contact VARCHAR(15) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Sale Collection (MongoDB)
```javascript
{
    saleId: "SALE-20251017123456",
    customerId: 1,
    customerContact: "1234567890",
    saleItems: [
        {
            productId: 1,
            productName: "Product Name",
            barcode: "123456",
            quantity: 2,
            unitPrice: 10.00,
            totalPrice: 20.00
        }
    ],
    subTotal: 100.00,
    taxAmount: 10.00,
    discountAmount: 0.00,
    totalAmount: 110.00,
    paidAmount: 120.00,
    changeAmount: 10.00,
    paymentMethod: "Cash",
    createdAt: ISODate("2025-10-17T12:34:56Z")
}
```

## Production Deployment

### Client PC Setup
1. Build application: `./mvnw clean package`
2. Create desktop shortcut to JAR file
3. Configure ApiConfig with production server IP
4. Distribute to all client PCs
5. Test thoroughly before going live

### Security Considerations
- Use HTTPS for production (update BASE_URL to https://)
- Implement authentication/authorization
- Add request rate limiting
- Encrypt sensitive data
- Regular database backups
- Monitor API access logs

## Support

For issues or questions:
1. Check server logs: `tail -f /var/log/pos-server.log`
2. Check client console output
3. Verify network connectivity
4. Review this documentation
5. Contact system administrator

## Summary

This POS client application provides a complete point-of-sale solution that:
- Searches and selects products from server inventory
- Captures customer information
- Processes payments (Cash/Card)
- Updates inventory in real-time
- Maintains complete transaction history

All data is stored on the server PC, ensuring data integrity and enabling multiple client PCs to work simultaneously.
