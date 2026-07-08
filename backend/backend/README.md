# DOST AMCen Booking System - Backend API

Complete backend API for the booking system with MySQL database integration.

## Setup Instructions

### 1. Prerequisites
- Node.js (v14 or higher)
- MySQL Server installed and running
- npm or yarn

### 2. Installation

```bash
# Navigate to backend folder
cd backend

# Install dependencies
npm install
```

### 3. Database Setup

```bash
# Open MySQL command line
mysql -u root -p

# Create database
CREATE DATABASE amcen_bookings;

# Exit MySQL
EXIT;
```

### 4. Configure Environment Variables

Edit `.env` file with your MySQL credentials:
```
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=amcen_bookings
PORT=5000
```

### 5. Start the Server

```bash
npm start
```

Server will run on `http://localhost:5000`

## API Endpoints

### Submit Booking
- **POST** `/api/bookings/submit`
- **Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "09123456789",
  "institution": "ABC University",
  "dateRequested": "2024-07-15",
  "timeRequested": "14:30",
  "purpose": "Research",
  "facility": "Lab A",
  "participants": 5,
  "requests": "Additional notes here"
}
```

### Get All Bookings
- **GET** `/api/bookings`

### Get Specific Booking
- **GET** `/api/bookings/:id`

### Update Booking Status
- **PUT** `/api/bookings/:id/status`
- **Body:**
```json
{
  "status": "approved",
  "notes": "Approved for July 15"
}
```

### Filter by Status
- **GET** `/api/bookings/filter/status/:status`
- Status: `pending`, `approved`, `rejected`, `completed`

### Search Bookings
- **GET** `/api/bookings/search?query=keyword`

### Delete Booking
- **DELETE** `/api/bookings/:id`

### Health Check
- **GET** `/api/health`

## Android App Integration

Your Android client can use these endpoints to:
1. Fetch all bookings: `GET /api/bookings`
2. Filter by status: `GET /api/bookings/filter/status/pending`
3. Update booking: `PUT /api/bookings/:id/status`
4. Search bookings: `GET /api/bookings/search?query=search_term`

## Database Schema

```
bookings table:
- id (INT, Primary Key)
- firstName (VARCHAR 100)
- lastName (VARCHAR 100)
- email (VARCHAR 100)
- phone (VARCHAR 20)
- institution (VARCHAR 150)
- dateRequested (DATE)
- timeRequested (TIME)
- purpose (VARCHAR 200)
- facility (VARCHAR 200)
- participants (INT)
- requests (LONGTEXT)
- status (ENUM: pending, approved, rejected, completed)
- notes (LONGTEXT)
- createdAt (TIMESTAMP)
- updatedAt (TIMESTAMP)
```

## Troubleshooting

**Port already in use:**
```bash
# Change PORT in .env file
PORT=5001
```

**Database connection failed:**
- Check MySQL is running
- Verify credentials in .env
- Ensure database exists

**CORS errors:**
- Frontend should be able to access `http://localhost:5000`
- For production, update CORS settings in server.js
