# SETUP CHECKLIST - Booking System Android Integration

## ✅ Backend Status
- [x] Express API running on port 5000
- [x] SQLite database initialized
- [x] CORS enabled for cross-origin requests
- [x] Default admin user created (admin / admin123)
- [x] Test booking exists (Salvador Client, ID 4)
- [x] All endpoints functional

## 📋 Files to Copy to Android Studio

Copy these files from `backend/` folder to your Android project's `app/src/main/java/com/example/myapplication/` folder:

| File | Destination | Purpose |
|------|-------------|---------|
| `BookingService.kt` | `api/BookingService.kt` | Retrofit API endpoints |
| `ApiClient.kt` | `api/ApiClient.kt` | Retrofit client setup |
| `MainViewModel.kt` | `MainViewModel.kt` | ViewModel with booking logic |

## 🔧 Android Studio Configuration

### Step 1: Add Dependencies
File: `build.gradle` (Module: app)
```gradle
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### Step 2: Update AndroidManifest.xml
```xml
<!-- Add these lines -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- In <application> tag, add: -->
android:networkSecurityConfig="@xml/network_security_config"
```

### Step 3: Create Network Security Config
Create file: `res/xml/network_security_config.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168</domain>
    </domain-config>
</network-security-config>
```

## 🚀 Testing

### 1. Start Backend Server
```bash
cd backend
npm start
```
Wait for: "✓ API server running on http://localhost:5000"

### 2. Run Android App
- Open Android Studio
- Click "Run" → Android Emulator
- App auto-logs in with admin/admin123

### 3. Check Logcat
Watch Android Studio Logcat (bottom panel) for:
```
D/Auth: ✓ Login successful: System Administrator
D/Bookings: ✓ Fetched 4 bookings
```

### 4. Verify Data
In your Kotlin code:
```kotlin
val viewModel: MainViewModel = viewModel()
println(viewModel.bookings) // Should show bookings from database
```

## 🔑 Default Credentials
```
Database ID: admin
Password:    admin123
```

## 🌐 URL Configuration

### For Android Emulator ✓ (Recommended)
`http://10.0.2.2:5000/` (Already configured)

### For Physical Device
Edit `ApiClient.kt` and replace BASE_URL:
```kotlin
private const val BASE_URL = "http://192.168.1.100:5000/"
```
Replace `192.168.1.100` with your computer's actual IP from `ipconfig`

## 📡 Available API Methods

From `MainViewModel`, you have access to:

```kotlin
// Authentication
viewModel.loginUser(id, password)
viewModel.logout()

// Fetch Data
viewModel.fetchBookings()
viewModel.filterBookingsByStatus("pending")  // pending, approved, rejected, completed
viewModel.searchBookings("John")

// Modify Data
viewModel.updateBookingStatus(bookingId = 4, newStatus = "approved")
viewModel.deleteBooking(bookingId = 4)

// State Variables
viewModel.bookings              // List<Booking>
viewModel.authToken             // String?
viewModel.isAuthenticated       // Boolean
viewModel.isFetchingBookings    // Boolean
viewModel.errorMessage          // String?
```

## 📊 Database Schema

### bookings table
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER | Auto-increment |
| firstName | TEXT | Client first name |
| lastName | TEXT | Client last name |
| email | TEXT | Client email |
| phone | TEXT | Client phone |
| institution | TEXT | Organization |
| dateRequested | DATE | Booking date |
| timeRequested | TIME | Booking time |
| purpose | TEXT | Booking purpose |
| facility | TEXT | Facility requested |
| participants | INTEGER | Number of participants |
| requests | TEXT | Special requests |
| status | TEXT | pending/approved/rejected/completed |
| createdAt | TIMESTAMP | Creation date |
| updatedAt | TIMESTAMP | Last update |
| notes | TEXT | Admin notes |

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Connection refused" | Start backend with `npm start` |
| "404 Not Found" | Check API endpoint URL in BookingService |
| "401 Unauthorized" | Login failed - check credentials |
| "No fields in Booking" | Restart backend, delete `amcen_bookings.db` |
| "Timeout" | Increase timeout in ApiClient.kt |
| Cannot reach 10.0.2.2 | Run `adb shell ping 10.0.2.2` in terminal |

## 📁 Project Structure

```
backend/
├── server.js                    ← Express API
├── amcen_bookings.db           ← SQLite Database
├── BookingService.kt           ← Copy to Android
├── ApiClient.kt                ← Copy to Android
├── MainViewModel.kt            ← Copy to Android
└── ANDROID_INTEGRATION.md      ← Detailed guide

Android Project/
└── app/src/main/
    ├── java/com/example/myapplication/
    │   ├── api/
    │   │   ├── BookingService.kt
    │   │   └── ApiClient.kt
    │   ├── MainViewModel.kt
    │   └── MainActivity.kt
    ├── res/xml/
    │   └── network_security_config.xml
    └── AndroidManifest.xml
```

## 🎯 Next Steps

1. ✅ Copy 3 Kotlin files to Android project
2. ✅ Add dependencies to build.gradle and sync
3. ✅ Update AndroidManifest.xml with permissions
4. ✅ Create network_security_config.xml
5. ✅ Run Android Emulator
6. ✅ Verify Logcat shows successful login
7. ⏭️ Add Bookings screen to your CMS UI
8. ⏭️ Add booking filters and search
9. ⏭️ Test approval/rejection workflow

## 💡 Pro Tips

- The ViewModel auto-logs in on app startup using default credentials
- Bookings automatically refresh after status updates
- Search is case-insensitive and searches both names and facilities
- All API calls are async - no UI blocking
- Check Logcat tag "Auth" and "Bookings" for debugging

## ⚠️ Important Notes

1. **Always use 10.0.2.2:5000 in Android Emulator, NOT localhost:5000**
2. **Network permissions must be in AndroidManifest.xml or requests will fail**
3. **network_security_config.xml is required to allow HTTP (not HTTPS)**
4. **For production, use HTTPS and proper authentication**

## 📞 Quick Reference

Backend Health Check:
```bash
curl http://localhost:5000/api/health
```

Test Login:
```bash
curl -X POST http://localhost:5000/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{"databaseId":"admin","password":"admin123"}'
```

Get All Bookings (requires Bearer token):
```bash
curl http://localhost:5000/api/bookings \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## ✨ Success Indicators

Your setup is working when you see:
- ✅ Android app starts without errors
- ✅ Logcat shows "Login successful"
- ✅ Logcat shows "Fetched X bookings"
- ✅ `viewModel.bookings` is not empty
- ✅ `viewModel.isAuthenticated` is true
- ✅ Can filter and search bookings

Enjoy! 🎉
