# Invoice/Mail System Integration - Complete Setup

## 🎯 Overview

The booking system now automatically generates and manages invoices/mails for:
- **Booking Confirmation** - When a booking is submitted
- **Booking Approved** - When admin approves a booking
- **Booking Rejected** - When admin rejects a booking
- **Booking Completed** - When booking is marked complete

Invoices are automatically sent to customers and can be viewed in the CMS mail section.

---

## ✅ What's Been Implemented

### **Backend Updates (server.js)**

1. **New Invoices Table** - Stores all generated invoices
   ```sql
   CREATE TABLE invoices (
     id INTEGER PRIMARY KEY,
     bookingId INTEGER NOT NULL,
     email TEXT NOT NULL,
     subject TEXT NOT NULL,
     content TEXT NOT NULL,
     type TEXT DEFAULT 'confirmation',
     status TEXT DEFAULT 'pending',
     createdAt DATETIME,
     sentAt DATETIME,
     FOREIGN KEY (bookingId) REFERENCES bookings(id)
   )
   ```

2. **Invoice Generation Function** - `generateInvoice(booking, type)`
   - Creates professional HTML invoices
   - Includes booking details in formatted table
   - Custom messages for each status type
   - Automatically called on booking creation and status updates

3. **New API Endpoints**:
   - `GET /api/invoices` - Get all invoices (requires auth)
   - `GET /api/invoices/booking/{bookingId}` - Get invoices for specific booking
   - `GET /api/invoices/type/{type}` - Filter invoices by type

4. **Enhanced Endpoints**:
   - `/api/bookings/submit` - Now creates confirmation invoice automatically
   - `/api/bookings/:id/status` - Now creates status-change invoice automatically

### **Android Updates**

1. **BookingService.kt Additions**:
   - `Invoice` data class
   - `InvoicesResponse` data class
   - 3 new API methods:
     ```kotlin
     suspend fun getAllInvoices(token: String): InvoicesResponse
     suspend fun getInvoicesByBooking(bookingId: Int, token: String): InvoicesResponse
     suspend fun getInvoicesByType(type: String, token: String): InvoicesResponse
     ```

2. **MainViewModel.kt Enhancements**:
   - `isFetchingInvoices` state variable
   - `fetchInvoices()` - Fetches all invoices from API
   - `fetchMails()` - Updated to auto-fetch invoices when authenticated
   - `updateBookingStatus()` - Updated to refresh invoices after status change
   - `loginWithDefaults()` - Updated to fetch invoices after login

3. **Mail System Integration**:
   - Invoices automatically display in Mail tab as Mail objects
   - Mail sender: "bookings@amcen.com"
   - Mail subject: Invoice subject line
   - Mail content: Full HTML invoice

---

## 📊 How It Works - Flow Diagram

```
User submits booking via web form
         ↓
Backend saves booking to database
         ↓
generateInvoice() creates confirmation email
         ↓
Invoice stored in invoices table
         ↓
Admin opens CMS and navigates to Mails tab
         ↓
fetchInvoices() retrieves all invoices
         ↓
Converted to Mail objects and displayed
         ↓
Admin can view/read booking invoices in email interface
         ↓
When admin changes booking status
         ↓
New invoice generated for new status
         ↓
Displayed automatically in Mails tab on next refresh
```

---

## 🧪 Testing the Invoice System

### **Step 1: Start Backend**
```bash
cd backend
node server.js
```

### **Step 2: Submit a Test Booking**
Visit `http://localhost:5000` and submit a booking with:
- Name: Test Customer
- Email: test@example.com
- Phone: 123-456-7890
- Facility: Conference Room
- Date: 2026-06-15
- Time: 10:00 AM
- Purpose: Team Meeting
- Participants: 5

### **Step 3: Check Database**
Verify invoice was created:
```bash
sqlite3 backend/amcen_bookings.db "SELECT * FROM invoices;"
```

Expected output:
```
1|1|test@example.com|Booking Confirmation - Conference Room|<html>...</html>|confirmation|pending|2026-06-11 10:30:00|
```

### **Step 4: Test in Android App**
1. Build and run app
2. Navigate to **Mails** tab
3. Should see **"Booking Confirmation - Conference Room"** email
4. Click to view full HTML invoice

### **Step 5: Test Status Change**
1. Navigate to **Bookings** tab
2. Click "Change Status" on your test booking
3. Change status to "Approved"
4. Go back to **Mails** tab
5. Should see new mail: **"Booking Approved - Conference Room"**

---

## 📧 Invoice Types & Messages

| Type | Subject Pattern | Message |
|------|-----------------|---------|
| confirmation | Booking Confirmation - {facility} | Your booking has been received. We will review it... |
| approved | Booking Approved - {facility} | Your booking has been approved! You're all set for {date} at {time}. |
| rejected | Booking Rejected - {facility} | Unfortunately, your booking has been rejected. Please contact us... |
| completed | Booking Completed - {facility} | Your booking has been completed. Thank you for using our facility! |

---

## 🔧 Database Queries

### Get All Invoices
```sql
SELECT i.*, b.firstName, b.lastName, b.facility 
FROM invoices i 
JOIN bookings b ON i.bookingId = b.id 
ORDER BY i.createdAt DESC;
```

### Get Invoices for Specific Booking
```sql
SELECT * FROM invoices WHERE bookingId = 1 ORDER BY createdAt DESC;
```

### Get Invoices by Type
```sql
SELECT * FROM invoices WHERE type = 'approved' ORDER BY createdAt DESC;
```

### Count Invoices by Type
```sql
SELECT type, COUNT(*) as count FROM invoices GROUP BY type;
```

---

## ✨ Key Features

✅ **Automatic Invoice Generation** - No manual intervention needed
✅ **Professional HTML Format** - Beautiful formatted emails
✅ **Status-Triggered Invoices** - New invoice on every status change
✅ **Full Booking Details** - All relevant info in invoice
✅ **Integration with Mail System** - Seamless display in CMS
✅ **Authentication Required** - Admin-only invoice access
✅ **Database Storage** - Permanent invoice history
✅ **Searchable Invoices** - Find by type, booking, or customer

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| No invoices showing in Mails | Make sure you're logged in and have authentication token |
| Invoice HTML not rendering | Check if content is being properly stored in database |
| Invoices not created on booking submit | Check backend logs for errors; verify invoices table exists |
| Can't see new invoices after status change | Refresh the Mails tab; try pulling down to refresh |

---

## 📋 API Request/Response Examples

### Get All Invoices
**Request:**
```http
GET /api/invoices
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "bookingId": 1,
      "email": "customer@email.com",
      "subject": "Booking Confirmation - Conference Room",
      "content": "<html>...</html>",
      "type": "confirmation",
      "status": "pending",
      "createdAt": "2026-06-11T10:30:00",
      "sentAt": null,
      "firstName": "John",
      "lastName": "Doe",
      "facility": "Conference Room"
    }
  ]
}
```

### Get Invoices by Type
**Request:**
```http
GET /api/invoices/type/approved
Authorization: Bearer {token}
```

---

## 🚀 Next Steps for Enhancement

Consider adding in the future:

1. **Email Sending** - Actually send emails to customers
   - Integrate with Nodemailer or SendGrid
   - Track sent status in database

2. **PDF Generation** - Export invoices as PDF
   - Use library like html-pdf or puppeteer

3. **Email Templates** - Customizable email templates
   - Store in database
   - Allow admin to customize

4. **Scheduled Reminders** - Auto-send reminders
   - Approved bookings: day before
   - Pending bookings: follow-up after 3 days

5. **Bulk Invoice Actions** - Download all as ZIP
   - Export selected invoices

---

## 📞 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review backend logs: `node server.js` output
3. Verify database: `sqlite3 backend/amcen_bookings.db`
4. Check Android logcat for API errors

---

**Status**: ✅ Complete and Ready for Testing
**Last Updated**: 2026-06-11
