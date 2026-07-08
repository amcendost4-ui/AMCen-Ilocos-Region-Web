package com.example.myapplication

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Data models for the CMS
data class Client(
    val id: String,
    val sn: Int,
    val accountId: String,
    val accountType: String,
    val contactName: String,
    val companyName: String,
    val mobileNumber: String
)

data class Invoice(
    val id: String,
    val sender: String,
    val subject: String,
    val amount: String,
    val date: String,
    val status: String,
    val description: String
)

data class Meeting(
    val id: String = java.util.UUID.randomUUID().toString(),
    val clientName: String,
    val title: String,
    val date: String,
    val time: String,
    val description: String
)

class MainViewModel : ViewModel() {
    
    private val bookingService = ApiClient.getBookingService()
    
    // UI State
    var currentScreen: CmsScreen by mutableStateOf(CmsScreen.Dashboard)
    
    // Authentication
    var authToken: String? by mutableStateOf(null)
    var isAuthenticated: Boolean by mutableStateOf(false)
    var currentUser: LoginResponse.User? by mutableStateOf(null)
    
    // Invoice authentication
    var isInvoiceAuthenticated: Boolean by mutableStateOf(false)
    var authenticatedEmail: String? by mutableStateOf(null)
    
    // Data
    var clients by mutableStateOf<List<Client>>(emptyList())
    var invoices by mutableStateOf<List<Invoice>>(emptyList())
    var meetings by mutableStateOf<List<Meeting>>(emptyList())
    var bookings by mutableStateOf<List<Booking>>(emptyList())
    
    // Filter & Search
    var searchQuery: String by mutableStateOf("")
    var selectedStatusFilter: String by mutableStateOf("all")
    
    // Loading states
    var isFetchingInvoices: Boolean by mutableStateOf(false)
    var isFetchingBookings: Boolean by mutableStateOf(false)
    var isLoggingIn: Boolean by mutableStateOf(false)
    
    // Error handling
    var errorMessage: String? by mutableStateOf(null)
    
    init {
        // Initialize with sample data
        loadSampleClients()
        loadSampleInvoices()
        loadSampleMeetings()
        
        // Try to login automatically with default credentials
        loginWithDefaults()
    }
    
    // ============= BOOKING API METHODS =============
    
    fun loginWithDefaults() {
        loginUser("admin", "admin123")
    }
    
    fun loginUser(databaseId: String, password: String) {
        isLoggingIn = true
        val request = LoginRequest(databaseId, password)
        
        bookingService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                isLoggingIn = false
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        authToken = data.token
                        currentUser = data.user
                        isAuthenticated = true
                        Log.d("Auth", "✓ Login successful: ${data.user.name}")
                        
                        // Fetch bookings after login
                        fetchBookings()
                    }
                } else {
                    errorMessage = "Login failed: ${response.message()}"
                    Log.e("Auth", "Login error: ${response.errorBody()?.string()}")
                }
            }
            
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                isLoggingIn = false
                errorMessage = "Connection error: ${t.message}"
                Log.e("Auth", "Login failure: ${t.message}")
            }
        })
    }
    
    fun fetchBookings() {
        if (authToken == null) {
            Log.w("Bookings", "No auth token available")
            return
        }
        
        isFetchingBookings = true
        val token = "Bearer $authToken"
        
        bookingService.getAllBookings(token).enqueue(object : Callback<BookingsResponse> {
            override fun onResponse(call: Call<BookingsResponse>, response: Response<BookingsResponse>) {
                isFetchingBookings = false
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.success) {
                        bookings = data.data
                        Log.d("Bookings", "✓ Fetched ${bookings.size} bookings")
                    }
                } else {
                    Log.e("Bookings", "Fetch failed: ${response.message()}")
                }
            }
            
            override fun onFailure(call: Call<BookingsResponse>, t: Throwable) {
                isFetchingBookings = false
                Log.e("Bookings", "Fetch error: ${t.message}")
            }
        })
    }
    
    fun filterBookingsByStatus(status: String) {
        if (authToken == null) return
        
        if (status == "all") {
            fetchBookings()
            return
        }
        
        val token = "Bearer $authToken"
        bookingService.getBookingsByStatus(status, token).enqueue(object : Callback<BookingsResponse> {
            override fun onResponse(call: Call<BookingsResponse>, response: Response<BookingsResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.success) {
                        bookings = data.data
                        selectedStatusFilter = status
                        Log.d("Bookings", "✓ Filtered by status: $status (${bookings.size} results)")
                    }
                }
            }
            
            override fun onFailure(call: Call<BookingsResponse>, t: Throwable) {
                Log.e("Bookings", "Filter error: ${t.message}")
            }
        })
    }
    
    fun searchBookings(query: String) {
        if (authToken == null || query.isBlank()) {
            fetchBookings()
            return
        }
        
        val token = "Bearer $authToken"
        bookingService.searchBookings(query, token).enqueue(object : Callback<BookingsResponse> {
            override fun onResponse(call: Call<BookingsResponse>, response: Response<BookingsResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.success) {
                        bookings = data.data
                        Log.d("Bookings", "✓ Search results: ${bookings.size} bookings")
                    }
                }
            }
            
            override fun onFailure(call: Call<BookingsResponse>, t: Throwable) {
                Log.e("Bookings", "Search error: ${t.message}")
            }
        })
    }
    
    fun updateBookingStatus(bookingId: Int, newStatus: String, notes: String = "") {
        if (authToken == null) return
        
        val token = "Bearer $authToken"
        val request = StatusUpdateRequest(newStatus, if (notes.isBlank()) null else notes)
        
        bookingService.updateBookingStatus(bookingId, request, token)
            .enqueue(object : Callback<StatusUpdateResponse> {
                override fun onResponse(call: Call<StatusUpdateResponse>, response: Response<StatusUpdateResponse>) {
                    if (response.isSuccessful) {
                        Log.d("Bookings", "✓ Booking #$bookingId status updated to $newStatus")
                        fetchBookings() // Refresh list
                    }
                }
                
                override fun onFailure(call: Call<StatusUpdateResponse>, t: Throwable) {
                    Log.e("Bookings", "Update error: ${t.message}")
                }
            })
    }
    
    fun deleteBooking(bookingId: Int) {
        if (authToken == null) return
        
        val token = "Bearer $authToken"
        bookingService.deleteBooking(bookingId, token).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Log.d("Bookings", "✓ Booking #$bookingId deleted")
                    fetchBookings()
                }
            }
            
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Log.e("Bookings", "Delete error: ${t.message}")
            }
        })
    }
    
    fun logout() {
        if (authToken != null) {
            val token = "Bearer $authToken"
            bookingService.logout(token).enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    authToken = null
                    currentUser = null
                    isAuthenticated = false
                    bookings = emptyList()
                    Log.d("Auth", "✓ Logged out")
                }
                
                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Log.e("Auth", "Logout error: ${t.message}")
                }
            })
        }
    }
    
    // ============= CMS METHODS (Sample Data) =============
    
    private fun loadSampleClients() {
        clients = listOf(
            Client("1", 1, "ACC001", "Gold", "John Doe", "Tech Solutions Inc", "09123456789"),
            Client("2", 2, "ACC002", "Silver", "Jane Smith", "Creative Services Ltd", "09198765432"),
            Client("3", 3, "ACC003", "Bronze", "Robert Johnson", "Digital Marketing Pro", "09111111111")
        )
    }
    
    private fun loadSampleInvoices() {
        invoices = listOf(
            Invoice("1", "Tech Solutions Inc", "Web Development Services", "$5,000", "2026-06-05", "Pending", "Website redesign project"),
            Invoice("2", "Creative Services Ltd", "UI/UX Design", "$3,500", "2026-06-03", "Paid", "Mobile app design"),
            Invoice("3", "Digital Marketing Pro", "SEO Optimization", "$2,000", "2026-05-28", "Pending", "Search engine optimization")
        )
    }
    
    private fun loadSampleMeetings() {
        meetings = listOf(
            Meeting(clientName = "Tech Solutions Inc", title = "Project Kickoff", date = "2026-07-15", time = "10:00 AM", description = "Initial project meeting"),
            Meeting(clientName = "Creative Services Ltd", title = "Design Review", date = "2026-07-10", time = "2:00 PM", description = "Review design mockups")
        )
    }
    
    fun saveClient(client: Client) {
        val index = clients.indexOfFirst { it.id == client.id }
        if (index >= 0) {
            clients = clients.toMutableList().apply { set(index, client) }
        } else {
            clients = clients + client
        }
    }
    
    fun deleteClient(client: Client) {
        clients = clients.filter { it.id != client.id }
    }
    
    fun createInvoice(invoice: Invoice) {
        invoices = invoices + invoice
    }
    
    fun addMeeting(meeting: Meeting) {
        meetings = meetings + meeting
    }
    
    fun deleteMeeting(meeting: Meeting) {
        meetings = meetings.filter { it.id != meeting.id }
    }
    
    fun authenticateInvoice(email: String, password: String, rememberMe: Boolean) {
        if (email.isNotBlank()) {
            isInvoiceAuthenticated = true
            authenticatedEmail = email
        }
    }
    
    fun getFilteredClients(): List<Client> {
        return if (searchQuery.isBlank()) {
            clients
        } else {
            clients.filter {
                it.contactName.contains(searchQuery, ignoreCase = true) ||
                        it.companyName.contains(searchQuery, ignoreCase = true) ||
                        it.accountId.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}

// CMS Screen enum
enum class CmsScreen(val title: String, val icon: androidx.compose.material.icons.Icons) {
    Dashboard("Dashboard", androidx.compose.material.icons.Icons.Default.Home),
    Schedules("Schedules", androidx.compose.material.icons.Icons.Default.DateRange),
    ClientsList("Clients List", androidx.compose.material.icons.Icons.Default.Person),
    Invoices("Invoices", androidx.compose.material.icons.Icons.Default.Email),
    Reports("Reports", androidx.compose.material.icons.Icons.Default.Info),
    SearchInvoice("Search", androidx.compose.material.icons.Icons.Default.Search)
}
