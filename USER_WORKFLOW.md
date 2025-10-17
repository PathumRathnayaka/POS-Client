# POS Client - User Workflow Guide

## Quick Start

### Complete Sale Process (5 Simple Steps)

#### Step 1: Search and Add Products
1. Type product name or barcode in search field at top
2. Matching products appear in dropdown below
3. Click on a product to add it to the sale
4. Product appears in the sale table
5. Repeat for all products customer wants to buy

**Tips:**
- Click on quantity in table to change it
- Products show available stock - cannot exceed this
- If product already in table, quantity increases by 1

---

#### Step 2: Add Customer Information
1. Click "Add Customer" button (top navigation)
2. Customer form opens
3. Enter customer phone number (required)
4. Enter customer email (optional)
5. Click "Save" button
6. Success message appears
7. Form closes automatically

**Tips:**
- Sale ID is auto-generated (SALE-YYYYMMDDHHMMSS)
- Phone must be 10-15 digits
- You can regenerate Sale ID if needed
- Customer data is saved to server immediately

---

#### Step 3: Navigate to Payment
1. Review items in sale table
2. Check subtotal, tax, and total amounts
3. Click "Payment" button (top navigation)
4. Payment screen loads

**Important:**
- Must have at least one item in sale
- Must have customer information saved
- Cannot proceed without both

---

#### Step 4: Select Payment Method
1. Click "Cash" or "Card" button
2. Selected button turns green
3. "Paid Amount" field becomes enabled
4. Enter the amount customer paid
5. Change amount calculates automatically

**Payment Types:**
- **Cash**: Customer pays with physical money
- **Card**: Customer pays with credit/debit card

**Tips:**
- Paid amount must be ≥ total amount
- Change shows in green when sufficient
- Change shows in red when insufficient

---

#### Step 5: Complete Transaction
1. Verify all information is correct
2. Click "Done" button
3. Wait for processing (usually 1-2 seconds)
4. Success message appears with:
   - Sale ID
   - Total amount
   - Paid amount
   - Change amount
   - Payment method
5. Invoice options appear (Print/Email/PDF)

**After Completion:**
- Sale is saved to server database
- Customer information is recorded
- Stock inventory is updated automatically
- Cannot process same sale twice

---

## Detailed Workflow

### Dashboard Screen

```
┌─────────────────────────────────────────────────────┐
│  [Dashboard] [Products] [Settings] [Add Customer]  │
├─────────────────────────────────────────────────────┤
│  Search: [________________] 🔍                      │
│          └─ Dropdown shows results                  │
│                                                      │
│  Sale Table:                                        │
│  ┌────┬───────────┬──────────┬───────┬─────────┐   │
│  │ ID │ Name      │ Category │ Price │ Qty │Amt│   │
│  ├────┼───────────┼──────────┼───────┼─────┼───┤   │
│  │ 1  │ Product A │ Food     │ 10.00 │  2  │ 20│   │
│  │ 2  │ Product B │ Drink    │  5.00 │  1  │  5│   │
│  └────┴───────────┴──────────┴───────┴─────┴───┘   │
│                                                      │
│  Subtotal: $25.00                                   │
│  TAX (10%): $2.50                                   │
│  Total: $27.50                                      │
│                                                      │
│  [Delete] [Quantity] [Payment] [Sync]              │
└─────────────────────────────────────────────────────┘
```

### Add Customer Form

```
┌──────────────────────────────────────┐
│  New Customer - Sale Information     │
├──────────────────────────────────────┤
│                                       │
│  Sale ID: [SALE-20251017123456] [🔄] │
│                                       │
│  Customer Contact: [______________]  │
│  (Phone number - required)           │
│                                       │
│  Customer Email: [_______________]   │
│  (Optional)                          │
│                                       │
│         [Save]  [Cancel]             │
└──────────────────────────────────────┘
```

### Payment Screen

```
┌─────────────────────────────────────────────────────┐
│  Sale ID: SALE-20251017123456                       │
│  Customer: 1234567890                               │
├─────────────────────────────────────────────────────┤
│  Items Purchased:                                   │
│  ┌────────────────────────────────────────────┐    │
│  │ Product A    Qty: 2   $10.00   $20.00     │    │
│  │ Product B    Qty: 1    $5.00    $5.00     │    │
│  └────────────────────────────────────────────┘    │
│                                                      │
│  Sub Total: $25.00                                  │
│  Tax: $2.50                                         │
│  Total: $27.50                                      │
│                                                      │
│  Select Payment Method:                             │
│  [ Cash ]  [ Card ]  [ Check ]                     │
│                                                      │
│  Total: $27.50                                      │
│  Paid: [_______]                                    │
│  Change: $0.00                                      │
│                                                      │
│  [Cancel] [Discount] [Taxes] [Done]                │
│                                                      │
│  Invoice: [Print] [Email] [PDF]                    │
└─────────────────────────────────────────────────────┘
```

---

## Common Scenarios

### Scenario 1: Simple Cash Sale
```
1. Search "bread" → Select "Wheat Bread - $2.50"
2. Search "milk" → Select "Whole Milk - $3.99"
3. Click "Add Customer"
   - Enter phone: "5551234567"
   - Click "Save"
4. Click "Payment"
5. Click "Cash"
6. Enter paid: "10.00"
7. Change shows: "$3.51"
8. Click "Done"
✓ Sale complete!
```

### Scenario 2: Card Payment with Multiple Items
```
1. Search "laptop" → Select "Dell Laptop - $599.99"
2. Search "mouse" → Select "Wireless Mouse - $29.99"
3. Search "keyboard" → Select "Mechanical Keyboard - $79.99"
4. Click "Add Customer"
   - Enter phone: "5559876543"
   - Enter email: "customer@email.com"
   - Click "Save"
5. Click "Payment"
6. Verify total: $781.87 (includes 10% tax)
7. Click "Card"
8. Enter paid: "781.87"
9. Change shows: "$0.00"
10. Click "Done"
✓ Sale complete!
```

### Scenario 3: Adjusting Quantities
```
1. Search "soda" → Select "Cola 12oz - $1.50"
2. Item appears in table with Qty: 1
3. Click on quantity field (shows "1")
4. Type "6" and press Enter
5. Amount updates to $9.00
6. Continue with customer and payment as normal
```

---

## Button Reference

### Navigation Buttons
- **Dashboard**: Main sales screen (default view)
- **Products**: View product catalog (future feature)
- **Settings**: Application settings (future feature)
- **Add Customer**: Opens customer form
- **Payment**: Proceed to payment (validates first)
- **Sync Database**: Sync local data with server

### Sale Table Buttons
- **Delete**: Remove selected item from sale
- **Quantity**: Quick quantity adjustment for selected item

### Payment Buttons
- **Cancel**: Cancel current payment (with confirmation)
- **Discount**: Apply discount to sale (future feature)
- **Taxes**: Tax information (currently 10% fixed)
- **Cash**: Select cash payment method
- **Card**: Select card payment method
- **Check**: Select check payment method (future feature)
- **Done**: Complete and process payment

### Invoice Buttons (After Payment)
- **Print**: Print invoice (future feature)
- **Email**: Email invoice to customer (future feature)
- **PDF**: Generate PDF invoice (future feature)

---

## Keyboard Shortcuts (Future Feature)

Planned shortcuts for faster operation:
- `F1` - Add Customer
- `F2` - Payment
- `F3` - Search Field Focus
- `Delete` - Remove Selected Item
- `Enter` - Confirm Payment (when ready)
- `Esc` - Cancel Current Operation

---

## Error Messages Explained

### "Please add items to the sale before proceeding to payment"
- **Cause**: No items in sale table
- **Solution**: Search and add at least one product

### "Please add customer information before proceeding to payment"
- **Cause**: Customer data not saved
- **Solution**: Click "Add Customer" and save customer info

### "Payment method required"
- **Cause**: No payment method selected
- **Solution**: Click "Cash" or "Card" button

### "Paid amount is less than total"
- **Cause**: Customer hasn't paid enough
- **Solution**: Enter correct amount or adjust sale

### "Failed to save customer"
- **Cause**: Server connection problem
- **Solution**: Check network connection, verify server is running

### "Failed to process payment"
- **Cause**: Server error or network issue
- **Solution**: Check server logs, verify database connection

---

## Best Practices

### ✅ Do:
- Always add customer information before payment
- Double-check quantities before proceeding
- Verify payment amount with customer
- Wait for success confirmation
- Keep server connection stable

### ❌ Don't:
- Don't skip customer information
- Don't process payment without verifying items
- Don't click "Done" multiple times
- Don't close application during payment processing
- Don't modify sale after proceeding to payment

---

## Tips for Fast Operation

1. **Product Search**: Type partial names (e.g., "col" for "Cola")
2. **Quantity Changes**: Click directly on number in table
3. **Repeat Customers**: Phone number can be reused
4. **Quick Payment**: Most customers pay exact or slightly over
5. **Stay Organized**: Complete one sale before starting next

---

## Training Checklist

For new users, practice:
- [ ] Search and add 3 different products
- [ ] Change quantity of an item
- [ ] Remove an item from sale
- [ ] Add customer with phone only
- [ ] Add customer with phone and email
- [ ] Complete cash sale with change
- [ ] Complete card sale with exact amount
- [ ] Handle "insufficient payment" scenario
- [ ] Verify success message and sale ID

---

## Quick Troubleshooting

| Problem | Quick Fix |
|---------|-----------|
| Search shows no results | Check server connection |
| Can't add customer | Verify phone format (10-15 digits) |
| Payment button disabled | Add items and customer first |
| Change amount wrong | Re-enter paid amount |
| "Already processed" error | Start new sale |

---

## Summary

The POS client provides a simple, reliable way to:
✓ Search products quickly
✓ Build customer sales
✓ Capture customer information
✓ Process payments (Cash/Card)
✓ Update inventory automatically
✓ Maintain complete records

All operations are saved to the server, ensuring data is never lost.
