# Client Management System - Authentication Integration

## Default Login Credentials

After starting the server, use these credentials to login:

```
Database ID: admin
Password: admin123
```

## Authentication Flow

### 1. Login Endpoint
**POST** `/api/admin/login`

**Request Body:**
```json
{
  "databaseId": "admin",
  "password": "admin123"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "abc123def456...",
  "user": {
    "id": 1,
    "databaseId": "admin",
    "name": "System Administrator",
    "role": "admin"
  }
}
```

**Response (Failed):**
```json
{
  "error": "Invalid database ID or password"
}
```

### 2. Using the Token

After login, include the token in all protected requests:

**Header Method (Recommended):**
```
Authorization: Bearer <token>
```

**Example:**
```javascript
fetch('http://localhost:5000/api/bookings', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer abc123def456...'
  }
})
```

### 3. Logout Endpoint
**POST** `/api/admin/logout`

**Header:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

## Protected Endpoints (Require Authentication)

All these endpoints now require a valid token:

- **GET** `/api/bookings` - Get all bookings
- **GET** `/api/bookings/:id` - Get specific booking
- **GET** `/api/bookings/filter/status/:status` - Filter by status
- **GET** `/api/bookings/search?query=keyword` - Search bookings
- **PUT** `/api/bookings/:id/status` - Update booking status
- **DELETE** `/api/bookings/:id` - Delete booking

## Public Endpoints (No Authentication Required)

- **POST** `/api/bookings/submit` - Submit new booking (for HTML form)
- **GET** `/api/health` - Health check

## Android Integration Example

### Java/Kotlin Retrofit Implementation

```java
public class AuthInterceptor implements Interceptor {
    private String token;
    
    public AuthInterceptor(String token) {
        this.token = token;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer " + token)
            .build();
        return chain.proceed(request);
    }
}

// Setup Retrofit with interceptor
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new AuthInterceptor(token))
    .build();

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("http://192.168.x.x:5000/")
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

### Login Flow in Android

```java
public class LoginActivity extends AppCompatActivity {
    
    private BookingService service;
    
    private void loginUser(String databaseId, String password) {
        LoginRequest request = new LoginRequest(databaseId, password);
        
        service.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse data = response.body();
                    String token = data.token;
                    
                    // Save token to SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                    prefs.edit().putString("token", token).apply();
                    
                    // Navigate to main activity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
```

### Retrofit Service Interface

```java
public interface BookingService {
    
    @POST("api/admin/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    @POST("api/admin/logout")
    Call<LogoutResponse> logout(@Header("Authorization") String token);
    
    @GET("api/bookings")
    Call<BookingsResponse> getAllBookings(@Header("Authorization") String token);
    
    @GET("api/bookings/filter/status/{status}")
    Call<BookingsResponse> getBookingsByStatus(
        @Path("status") String status,
        @Header("Authorization") String token
    );
    
    @PUT("api/bookings/{id}/status")
    Call<StatusUpdateResponse> updateBookingStatus(
        @Path("id") int id,
        @Body StatusUpdateRequest request,
        @Header("Authorization") String token
    );
}

public class LoginRequest {
    public String databaseId;
    public String password;
    
    public LoginRequest(String databaseId, String password) {
        this.databaseId = databaseId;
        this.password = password;
    }
}

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

## Security Notes

1. **Default Password:** Change the default admin password immediately
2. **HTTPS:** Use HTTPS in production
3. **Token Expiration:** Currently tokens don't expire (implement expiration for security)
4. **Password Hashing:** Passwords are hashed with SHA-256

## Creating Additional Admin Users

Connect to MySQL and run:

```sql
-- To create a new admin user, first hash the password
-- For password "newpassword", use this sha256 hash:
INSERT INTO admin_users (databaseId, password, name, role, active) 
VALUES ('username', 'sha256_hash_here', 'User Name', 'manager', TRUE);

-- Example with hashed password for 'manager123':
INSERT INTO admin_users (databaseId, password, name, role, active) 
VALUES ('manager', '8d969eef6ecad3c29a3a873fba8fe92241d3b1357bda1f923f58fe7c4abc67c1', 'Manager User', 'manager', TRUE);
```

## Troubleshooting

**"No token provided":**
- Make sure to include Authorization header
- Format: `Authorization: Bearer <token>`

**"Invalid or expired token":**
- Token may have been lost (restart app/login again)
- Make sure token is stored correctly

**"Invalid database ID or password":**
- Check credentials
- Make sure user is active in database
