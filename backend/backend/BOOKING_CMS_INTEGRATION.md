# Complete Booking System Integration Guide

## 📋 What We've Created

We've created 3 key files to integrate the online booking form with your Android CMS:

1. **MainViewModel_Updated.kt** - Updated ViewModel with booking API integration
2. **BookingsScreen.kt** - Beautiful UI screen to display and manage bookings
3. **BookingService.kt** & **ApiClient.kt** - Already created for API calls

## 🚀 Setup Steps

### Step 1: Copy Files to Your Android Project

Copy these files from the `backend/` folder to your Android project:

```
backend/ 
├── BookingService.kt          → app/src/main/java/com/example/myapplication/api/
├── ApiClient.kt               → app/src/main/java/com/example/myapplication/api/
├── MainViewModel_Updated.kt   → app/src/main/java/com/example/myapplication/
├── BookingsScreen.kt          → app/src/main/java/com/example/myapplication/
└── (existing files)
```

**Steps:**
1. In Android Studio, expand `app/src/main/java/com/example/myapplication`
2. If `api` folder doesn't exist, right-click and create new Package named `api`
3. Copy `BookingService.kt` and `ApiClient.kt` into `api/` folder
4. Copy `MainViewModel_Updated.kt` and `BookingsScreen.kt` into main package

### Step 2: Update build.gradle

Add to `build.gradle` (Module: app) if not already added:

```gradle
dependencies {
    // Retrofit for API calls
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // OkHttp
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
    
    // Gson
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

Click **"Sync Now"**

### Step 3: Update AndroidManifest.xml

Add permissions:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

In `<application>` tag, add:
```xml
android:networkSecurityConfig="@xml/network_security_config"
```

### Step 4: Create Network Security Config

Create file: `res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

### Step 5: Update MainActivity.kt

Replace your existing MainActivity with updated code that includes BookingsScreen:

```kotlin
@Composable
fun CmsDashboard(viewModel: MainViewModel = viewModel()) {
    // ... existing code ...
    
    MainContent(
        // ... existing parameters ...
        currentScreen = viewModel.currentScreen,
        // ... add this:
        bookings = viewModel.bookings,
        onFilterBookingsStatus = { viewModel.filterBookingsByStatus(it) },
        onSearchBookings = { viewModel.searchBookings(it) },
        onUpdateBookingStatus = { id, status -> viewModel.updateBookingStatus(id, status) },
        isFetchingBookings = viewModel.isFetchingBookings,
    )
}
```

In `MainContent()` composable, add this case:

```kotlin
AnimatedContent(targetState = currentScreen) { screen ->
    when (screen) {
        // ... existing cases ...
        
        CmsScreen.Bookings -> BookingsContent(
            bookings = bookings,
            selectedStatusFilter = selectedStatusFilter,
            onFilterStatus = onFilterBookingsStatus,
            onSearchBooking = onSearchBookings,
            onUpdateStatus = onUpdateBookingStatus,
            isFetching = isFetchingBookings
        )
        
        // ... rest of cases ...
    }
}
```

### Step 6: Update MainViewModel

Replace your current MainViewModel.kt with `MainViewModel_Updated.kt` which includes:
- ✅ Booking API integration (login, fetch, filter, search, update)
- ✅ New CmsScreen enum with "Bookings" tab
- ✅ All state management for bookings

## 🔄 Data Flow

```
Online Booking Form (HTML)
        ↓
  Backend API (Node.js/SQLite)
        ↓
   MainViewModel
        ├─ fetchBookings()
        ├─ filterBookingsByStatus()
        ├─ searchBookings()
        └─ updateBookingStatus()
        ↓
   BookingsScreen (UI)
        ├─ Display all bookings
        ├─ Filter by status
        ├─ Change status
        └─ View details
```

## 🧪 Testing

### 1. Start Backend Server
```bash
cd backend
npm start
```

### 2. Run Android App
- Open Android Studio
- Click **Run** → Select Android Emulator
- App auto-logs in with admin/admin123

### 3. Submit a Test Booking
Open the booking form in a browser:
```
file:///C:/Users/amcen/OneDrive/Desktop/AMCENXDOSTWEBSITE/online%20booking.html
```

Fill out the form and submit. You should see it appear in the Android app!

### 4. Check Logcat
Watch for:
```
D/Auth: ✓ Login successful: System Administrator
D/Bookings: ✓ Fetched X bookings
```

## 📊 Booking Status Workflow

The CMS allows you to manage bookings through their lifecycle:

```
pending (Yellow)  → Change Status → approved (Green)
                                  → rejected (Red)
                                  → completed (Gray)
```

Click the **"Change Status"** button on any booking to update it.

## 🔑 Default Credentials

```
Database ID: admin
Password:    admin123
```

These are created automatically when the backend starts.

## 🌐 Configuration

### For Android Emulator (Default)
URL: `http://10.0.2.2:5000/`

### For Physical Device
Edit `ApiClient.kt`:
```kotlin
private const val BASE_URL = "http://YOUR_IP:5000/"
```

Get your IP: Open PowerShell → `ipconfig` → Look for IPv4 Address

## 📱 Available Features

✅ View all bookings from online form
✅ Filter by status (pending, approved, rejected, completed)
✅ Search bookings by name or facility
✅ Change booking status
✅ Mark bookings as completed
✅ Automatic refresh on updates
✅ Real-time sync with database

## 🛠️ Troubleshooting

| Problem | Solution |
|---------|----------|
| "Connection refused" | Make sure backend is running: `npm start` |
| "401 Unauthorized" | Restart backend - admin user auto-created |
| "No bookings showing" | Check Logcat for errors, make sure form submissions worked |
| App crashes on startup | Check all 4 files are in correct folders |

## 📁 Final Project Structure

```
app/src/main/
├── java/com/example/myapplication/
│   ├── api/
│   │   ├── BookingService.kt ✓
│   │   └── ApiClient.kt ✓
│   ├── MainActivity.kt (updated with BookingsScreen)
│   ├── MainViewModel.kt (replaced with updated version)
│   └── BookingsScreen.kt ✓
├── res/
│   ├── xml/
│   │   └── network_security_config.xml ✓
│   └── AndroidManifest.xml (updated with permissions)
└── build.gradle (updated with dependencies)
```

## 🎯 Next Steps

1. ✅ Copy all 4 Kotlin files to Android Studio
2. ✅ Add Gradle dependencies
3. ✅ Update AndroidManifest.xml
4. ✅ Create network_security_config.xml
5. ✅ Replace MainViewModel and MainActivity
6. ✅ Run Android Emulator
7. ✅ Submit test booking from HTML form
8. ✅ Watch it appear in CMS!

## 💡 Pro Tips

- Bookings auto-refresh after status change
- Click "Refresh" button to manually refresh all bookings
- Use filters to manage pending approvals
- Status changes are saved to SQLite database immediately
- All activity is logged in Logcat for debugging

## ✨ Success Checklist

- [ ] All 4 Kotlin files copied to Android Studio
- [ ] Gradle dependencies added and synced
- [ ] AndroidManifest.xml updated
- [ ] network_security_config.xml created
- [ ] MainViewModel and MainActivity updated
- [ ] Backend running: `npm start`
- [ ] Android app starts without errors
- [ ] Logcat shows successful login
- [ ] Test booking appears in Bookings screen
- [ ] Can change booking status from CMS

**Congratulations! Your booking system is now integrated! 🎉**

## 📞 Support

If you run into issues:
1. Check Logcat for error messages
2. Verify backend is running: `curl http://localhost:5000/api/health`
3. Check network security config allows cleartext HTTP
4. Ensure INTERNET permission is in manifest
5. Test with default credentials: admin/admin123

Good luck! 🚀
