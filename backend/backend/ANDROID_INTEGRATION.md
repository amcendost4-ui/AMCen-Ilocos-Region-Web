# Android Studio Integration Guide - Booking API

## Quick Start (5 Minutes)

### 1. Add Dependencies
Add to `build.gradle` (Module: app):
```gradle
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```
Click "Sync Now"

### 2. Copy Files to Project
1. **BookingService.kt** → `app/src/main/java/com/example/myapplication/api/`
2. **ApiClient.kt** → `app/src/main/java/com/example/myapplication/api/`
3. **MainViewModel.kt** → `app/src/main/java/com/example/myapplication/`

### 3. Update AndroidManifest.xml
Add permissions:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Add to `<application>` tag:
```xml
android:networkSecurityConfig="@xml/network_security_config"
```

### 4. Create Network Security Config
Create `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Android Emulator -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
    <!-- Physical Device -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168</domain>
    </domain-config>
</network-security-config>
```

## Configuration

### Android Emulator (Default)
URL is set to: `http://10.0.2.2:5000/`

This is the standard way Android Emulator accesses the host machine.

### Physical Device
Edit `ApiClient.kt`:
```kotlin
private const val BASE_URL = "http://YOUR_IP:5000/"
```

**Find your computer's IP:**
- Windows: `ipconfig` in PowerShell → look for "IPv4 Address"
- Example: `http://192.168.1.100:5000/`

## Kotlin Implementation

### Initialize ViewModel
```kotlin
val viewModel: MainViewModel = viewModel()

// Automatically logs in with admin/admin123
// Then fetches all bookings
```

### Display Bookings
```kotlin
// Access bookings
val bookings = viewModel.bookings
val isLoading = viewModel.isFetchingBookings

// Filter by status
viewModel.filterBookingsByStatus("pending")

// Search
viewModel.searchBookings("John")

// Update status
viewModel.updateBookingStatus(bookingId = 4, newStatus = "approved")

// Delete
viewModel.deleteBooking(bookingId = 4)
```

### Example UI
```kotlin
@Composable
fun BookingsScreen(viewModel: MainViewModel) {
    LazyColumn {
        items(viewModel.bookings) { booking ->
            BookingItem(
                booking = booking,
                onStatusChange = { newStatus ->
                    viewModel.updateBookingStatus(booking.id, newStatus)
                }
            )
        }
    }
}
```

## Testing

### 1. Start Backend
```bash
cd backend
npm start
```
Should show:
```
✓ API server running on http://localhost:5000
✓ SQLite database initialized
```

### 2. Run Android App
- Open Android Studio
- Click "Run" → select Android Emulator
- Watch Logcat (bottom panel) for:
  ```
  D/Auth: ✓ Login successful: System Administrator
  D/Bookings: ✓ Fetched 4 bookings
  ```

### 3. Test with Default Credentials
- **ID:** admin
- **Password:** admin123
- **Database:** SQLite at `backend/amcen_bookings.db`

## Retrofit API Calls

All methods in `BookingService.kt`:

| Method | Use Case |
|--------|----------|
| `login(request)` | Get auth token |
| `getAllBookings(token)` | Fetch all bookings |
| `getBookingsByStatus(status, token)` | Filter by pending/approved/rejected/completed |
| `searchBookings(query, token)` | Search by client name or facility |
| `updateBookingStatus(id, request, token)` | Approve/reject/complete booking |
| `deleteBooking(id, token)` | Remove booking |
| `logout(token)` | End session |
| `healthCheck()` | Test connection |

## Troubleshooting

### "Connection refused" or "Network unreachable"
1. Backend not running → `npm start` in backend folder
2. Wrong URL → check `ApiClient.kt` BASE_URL
3. Emulator network → run `adb shell ping 10.0.2.2`

### "401 Unauthorized" or "No auth token"
1. Login failed → check Logcat for error
2. Credentials wrong → use admin/admin123
3. Token expired → app auto-logs in on startup

### "Parse error" or "No fields"
1. API response format changed
2. Booking table schema changed → delete `backend/amcen_bookings.db` and restart
3. Retrofit models don't match response → check API response vs `Booking.kt`

### Timeout errors
Increase timeout in `ApiClient.kt`:
```kotlin
.connectTimeout(60, TimeUnit.SECONDS)
.readTimeout(60, TimeUnit.SECONDS)
```

## Important Notes

⚠️ **Android Emulator Address**
- Do NOT use `localhost:5000` or `127.0.0.1:5000`
- Must use `10.0.2.2:5000` (emulator's gateway to host)
- See: https://developer.android.com/studio/run/emulator-networking

⚠️ **Network Security**
- HTTP (cleartext) only allowed for 10.0.2.2 and 192.168.* in network_security_config.xml
- Production must use HTTPS

⚠️ **Token Format**
- Backend expects: `Authorization: Bearer TOKEN_HERE`
- ViewModel automatically adds "Bearer " prefix

## Files Created

```
android/app/src/main/
├── java/com/example/myapplication/
│   ├── api/
│   │   ├── BookingService.kt (API endpoints)
│   │   └── ApiClient.kt (Retrofit setup)
│   ├── MainViewModel.kt (Booking state management)
│   └── MainActivity.kt (existing CMS)
└── res/
    ├── xml/
    │   └── network_security_config.xml (HTTP config)
    └── AndroidManifest.xml (permissions)
```

## Next Steps

1. Add Bookings screen to your CMS navigation
2. Display bookings list with status filter
3. Add approval/rejection dialogs
4. Test with real Android Emulator or device
5. Deploy backend to cloud (Heroku, AWS, etc.)

## API Status

Backend running at: http://localhost:5000

**Health Check:**
```bash
curl http://localhost:5000/api/health
```

**Database Info:**
- Type: SQLite
- Location: `backend/amcen_bookings.db`
- Auto-creates tables on first run
- Contains test booking (Salvador Client, ID 4)
