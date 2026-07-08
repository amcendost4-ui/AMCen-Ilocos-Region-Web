# Quick Start Guide

## Step-by-Step Setup

### 1. Open PowerShell in Backend Folder
```powershell
# Navigate to your backend folder
cd "C:\Users\amcen\OneDrive\Desktop\AMCENXDOSTWEBSITE\backend"
```

### 2. Install Dependencies
```powershell
npm install
```

### 3. Verify MySQL is Running
- Open MySQL command line or MySQL Workbench
- Make sure the MySQL service is running

### 4. Create Database
```sql
CREATE DATABASE amcen_bookings;
```

### 5. Update .env File
Edit `.env` with your MySQL credentials:
```
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_actual_password
DB_NAME=amcen_bookings
PORT=5000
```

### 6. Start the Server
```powershell
npm start
```

You should see:
```
Database initialized successfully
Server running on http://localhost:5000
```

## Testing the API

### Open Browser or Postman

**Test 1: Health Check**
```
GET http://localhost:5000/api/health
```
Expected response:
```json
{"status":"API is running"}
```

**Test 2: Submit a Booking**
```
POST http://localhost:5000/api/bookings/submit
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "09123456789",
  "institution": "Test University",
  "dateRequested": "2024-07-15",
  "timeRequested": "14:30",
  "purpose": "Research",
  "facility": "Lab A",
  "participants": 5,
  "requests": "Test booking"
}
```

**Test 3: Get All Bookings**
```
GET http://localhost:5000/api/bookings
```

## Connecting from Your HTML Form

The HTML form will now submit to:
```
POST http://localhost:5000/api/bookings/submit
```

Make sure:
1. The server is running
2. Backend server URL matches in your HTML form
3. CORS is enabled (it is by default)

## Common Issues

**Port 5000 already in use:**
- Change PORT in .env to 5001 or any available port
- Update URL in HTML form accordingly

**MySQL connection failed:**
```
Check:
- MySQL service is running
- Credentials in .env are correct
- Database exists (run: CREATE DATABASE amcen_bookings;)
```

**Form submission fails:**
- Open browser console (F12)
- Check Network tab to see API response
- Verify server URL in HTML matches backend server

## Database Inspection

```powershell
# Connect to MySQL
mysql -u root -p

# Use database
USE amcen_bookings;

# View bookings
SELECT * FROM bookings;

# View recent bookings
SELECT * FROM bookings ORDER BY createdAt DESC LIMIT 5;

# Filter by status
SELECT * FROM bookings WHERE status = 'pending';
```

## Stop Server
Press `Ctrl + C` in PowerShell to stop the server
