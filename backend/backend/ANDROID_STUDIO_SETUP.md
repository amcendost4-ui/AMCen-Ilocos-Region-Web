# Android Studio Setup for DOST AMCen Booking API

## 1. Server URL Configuration

### For Android Emulator (Recommended for Testing)
```
http://10.0.2.2:5000
```

### For Physical Device (If server is on same network)
```
http://192.168.x.x:5000
```
Replace `192.168.x.x` with your computer's actual IP address.

**To find your computer's IP:**
```powershell
ipconfig
```
Look for "IPv4 Address" under your network adapter.

## 2. Add Internet Permission

In `AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.bookingapp">

    <!-- Add these permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:networkSecurityConfig="@xml/network_security_config"
        ...>
        <!-- Your activities here -->
    </application>

</manifest>
```

## 3. Create Network Security Configuration

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

## 4. Add Retrofit and Dependencies

In `build.gradle` (Module: app):
```gradle
dependencies {
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
    
    // JSON
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

## 5. Create Model Classes

Create `models/Booking.java`:
```java
import com.google.gson.annotations.SerializedName;

public class Booking {
    public int id;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public String institution;
    public String dateRequested;
    public String timeRequested;
    public String purpose;
    public String facility;
    public int participants;
    public String requests;
    public String status;
    public String createdAt;
    public String updatedAt;
    public String notes;
}
```

Create `models/BookingsResponse.java`:
```java
import java.util.List;

public class BookingsResponse {
    public boolean success;
    public List<Booking> data;
}
```

Create `models/LoginRequest.java`:
```java
public class LoginRequest {
    public String databaseId;
    public String password;
    
    public LoginRequest(String databaseId, String password) {
        this.databaseId = databaseId;
        this.password = password;
    }
}
```

Create `models/LoginResponse.java`:
```java
public class LoginResponse {
    public boolean success;
    public String message;
    public String token;
    public User user;
    
    public static class User {
        public int id;
        public String databaseId;
        public String name;
        public String role;
    }
}
```

## 6. Create API Service Interface

Create `api/BookingService.java`:
```java
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface BookingService {
    
    @POST("api/admin/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    @GET("api/bookings")
    Call<BookingsResponse> getAllBookings(@Header("Authorization") String token);
    
    @GET("api/bookings/{id}")
    Call<BookingResponse> getBookingById(
        @Path("id") int id,
        @Header("Authorization") String token
    );
    
    @GET("api/bookings/filter/status/{status}")
    Call<BookingsResponse> getBookingsByStatus(
        @Path("status") String status,
        @Header("Authorization") String token
    );
    
    @GET("api/bookings/search")
    Call<BookingsResponse> searchBookings(
        @Query("query") String query,
        @Header("Authorization") String token
    );
    
    @PUT("api/bookings/{id}/status")
    Call<StatusUpdateResponse> updateBookingStatus(
        @Path("id") int id,
        @Body StatusUpdateRequest request,
        @Header("Authorization") String token
    );
    
    @DELETE("api/bookings/{id}")
    Call<DeleteResponse> deleteBooking(
        @Path("id") int id,
        @Header("Authorization") String token
    );
}
```

## 7. Create API Client

Create `api/ApiClient.java`:
```java
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.squareup.okhttp3.OkHttpClient;
import com.squareup.okhttp3.logging.HttpLoggingInterceptor;

public class ApiClient {
    
    private static final String BASE_URL = "http://10.0.2.2:5000/"; // For Emulator
    // For Physical Device: "http://192.168.x.x:5000/"
    
    private static Retrofit retrofit = null;
    
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            
            // Create logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
            
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
    
    public static BookingService getBookingService() {
        return getRetrofit().create(BookingService.class);
    }
}
```

## 8. Example Login Activity

Create `LoginActivity.java`:
```java
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private EditText databaseIdInput;
    private EditText passwordInput;
    private Button loginButton;
    private BookingService service;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        databaseIdInput = findViewById(R.id.database_id);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        
        service = ApiClient.getBookingService();
        
        loginButton.setOnClickListener(v -> login());
    }
    
    private void login() {
        String databaseId = databaseIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        if (databaseId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LoginRequest request = new LoginRequest(databaseId, password);
        
        service.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse data = response.body();
                    
                    // Save token
                    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                    prefs.edit().putString("token", data.token).apply();
                    
                    Toast.makeText(LoginActivity.this, 
                        "Welcome " + data.user.name, 
                        Toast.LENGTH_SHORT).show();
                    
                    // Navigate to main activity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, 
                        "Login failed: " + response.message(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, 
                    "Error: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}
```

## 9. Example Main Activity (Display Bookings)

Create `MainActivity.java`:
```java
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    
    private ListView bookingsList;
    private BookingService service;
    private String token;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        bookingsList = findViewById(R.id.bookings_list);
        service = ApiClient.getBookingService();
        
        // Get token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("token", null);
        
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        loadBookings();
    }
    
    private void loadBookings() {
        service.getAllBookings("Bearer " + token).enqueue(new Callback<BookingsResponse>() {
            @Override
            public void onResponse(Call<BookingsResponse> call, Response<BookingsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body().data;
                    
                    BookingAdapter adapter = new BookingAdapter(MainActivity.this, bookings);
                    bookingsList.setAdapter(adapter);
                    
                    Toast.makeText(MainActivity.this, 
                        "Loaded " + bookings.size() + " bookings", 
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, 
                        "Failed to load bookings", 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<BookingsResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, 
                    "Error: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
```

## 10. Test the Connection

To test if your Android app can reach the server:

```java
// In your activity or fragment
private void testConnection() {
    service.getAllBookings("Bearer test").enqueue(new Callback<BookingsResponse>() {
        @Override
        public void onResponse(Call<BookingsResponse> call, Response<BookingsResponse> response) {
            Log.d("Connection", "✓ Connected! Status: " + response.code());
        }
        
        @Override
        public void onFailure(Call<BookingsResponse> call, Throwable t) {
            Log.e("Connection", "✗ Failed: " + t.getMessage());
        }
    });
}
```

## Troubleshooting

### Can't connect to server:
1. Check if backend is running: `npm start`
2. Verify emulator can ping host:
   - In Android Studio Terminal: `adb shell ping 10.0.2.2`
3. Check firewall isn't blocking port 5000

### Check Server Logs:
Look at terminal running `npm start` for error messages

### Test API directly from Postman:
```
POST http://localhost:5000/api/admin/login
{
  "databaseId": "admin",
  "password": "admin123"
}
```

## Default Credentials for Testing:
```
Database ID: admin
Password:    admin123
```
