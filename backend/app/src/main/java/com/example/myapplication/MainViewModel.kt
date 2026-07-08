package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Client(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val institution: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class Mail(
    val id: String = UUID.randomUUID().toString(),
    val sender: String = "",
    val recipient: String = "",
    val subject: String = "",
    val content: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isRead: Boolean = false
)

data class Meeting(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val attendees: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

class MainViewModel : ViewModel() {
    var currentScreen by mutableStateOf(CmsScreen.Dashboard)
    var clients by mutableStateOf<List<Client>>(emptyList())
    var mails by mutableStateOf<List<Mail>>(emptyList())
    var meetings by mutableStateOf<List<Meeting>>(emptyList())
    var bookings by mutableStateOf<List<Booking>>(emptyList())
    
    var searchQuery by mutableStateOf("")
    var selectedStatusFilter by mutableStateOf("all")
    
    var isMailAuthenticated by mutableStateOf(false)
    var authenticatedEmail by mutableStateOf<String?>(null)
    var isFetchingMails by mutableStateOf(false)
    var isFetchingBookings by mutableStateOf(false)
    var isFetchingInvoices by mutableStateOf(false)
    
    var authToken by mutableStateOf<String?>(null)
    var isAuthenticated by mutableStateOf(false)
    var currentUser by mutableStateOf<Map<String, Any>?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var lastRefreshTime by mutableStateOf<String?>(null)
    var autoRefreshEnabled by mutableStateOf(true)

    private val bookingService = ApiClient.getBookingService()

    // Booking API Methods
    fun loginWithDefaults() {
        viewModelScope.launch {
            try {
                val response = bookingService.login(LoginRequest("admin", "admin123"))
                if (response.success && response.token != null) {
                    authToken = response.token
                    isAuthenticated = true
                    currentUser = response.user
                    fetchBookings()
                    fetchInvoices()
                    startAutoRefresh()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.message}"
            }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (autoRefreshEnabled && isAuthenticated) {
                delay(15000) // Refresh every 15 seconds
                fetchBookings()
                fetchInvoices()
                updateLastRefreshTime()
            }
        }
    }

    private fun updateLastRefreshTime() {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        lastRefreshTime = now.format(formatter)
    }

    fun loginUser(id: String, password: String) {
        viewModelScope.launch {
            try {
                val response = bookingService.login(LoginRequest(id, password))
                if (response.success && response.token != null) {
                    authToken = response.token
                    isAuthenticated = true
                    currentUser = response.user
                    fetchBookings()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.message}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                autoRefreshEnabled = false
                if (authToken != null) {
                    bookingService.logout("Bearer $authToken")
                }
            } finally {
                authToken = null
                isAuthenticated = false
                currentUser = null
                bookings = emptyList()
                lastRefreshTime = null
            }
        }
    }

    fun fetchBookings() {
        isFetchingBookings = true
        viewModelScope.launch {
            try {
                if (authToken == null) {
                    loginWithDefaults()
                    return@launch
                }
                val response = bookingService.getAllBookings("Bearer $authToken")
                if (response.success && response.data != null) {
                    bookings = response.data!!
                    errorMessage = null
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Failed to fetch bookings: ${e.message}"
            } finally {
                isFetchingBookings = false
            }
        }
    }

    fun filterBookingsByStatus(status: String) {
        selectedStatusFilter = status
        isFetchingBookings = true
        viewModelScope.launch {
            try {
                if (authToken == null) {
                    loginWithDefaults()
                    return@launch
                }
                val response = if (status == "all") {
                    bookingService.getAllBookings("Bearer $authToken")
                } else {
                    bookingService.getBookingsByStatus(status, "Bearer $authToken")
                }
                if (response.success && response.data != null) {
                    bookings = response.data!!
                    errorMessage = null
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Failed to filter bookings: ${e.message}"
            } finally {
                isFetchingBookings = false
            }
        }
    }

    fun searchBookings(query: String) {
        isFetchingBookings = true
        viewModelScope.launch {
            try {
                if (authToken == null) {
                    loginWithDefaults()
                    return@launch
                }
                val response = bookingService.searchBookings(query, "Bearer $authToken")
                if (response.success && response.data != null) {
                    bookings = response.data!!
                    errorMessage = null
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Failed to search bookings: ${e.message}"
            } finally {
                isFetchingBookings = false
            }
        }
    }

    fun updateBookingStatus(id: Int, status: String) {
        viewModelScope.launch {
            try {
                if (authToken == null) {
                    loginWithDefaults()
                    return@launch
                }
                val response = bookingService.updateBookingStatus(
                    id,
                    StatusUpdateRequest(status),
                    "Bearer $authToken"
                )
                if (response.success) {
                    // Update the booking in the list
                    bookings = bookings.map {
                        if (it.id == id) it.copy(status = status) else it
                    }
                    // Refresh invoices after status change
                    fetchInvoices()
                    errorMessage = null
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Failed to update booking status: ${e.message}"
            }
        }
    }

    fun deleteBooking(id: Int) {
        viewModelScope.launch {
            try {
                if (authToken == null) {
                    loginWithDefaults()
                    return@launch
                }
                val response = bookingService.deleteBooking(id, "Bearer $authToken")
                if (response.success) {
                    bookings = bookings.filter { it.id != id }
                    errorMessage = null
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Failed to delete booking: ${e.message}"
            }
        }
    }

    // Client Management Methods
    fun getFilteredClients(): List<Client> {
        return if (searchQuery.isEmpty()) {
            clients
        } else {
            clients.filter { client ->
                client.name.contains(searchQuery, ignoreCase = true) ||
                client.email.contains(searchQuery, ignoreCase = true) ||
                client.phone.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    fun saveClient(client: Client) {
        clients = if (clients.any { it.id == client.id }) {
            clients.map { if (it.id == client.id) client else it }
        } else {
            clients + client
        }
    }

    fun deleteClient(client: Client) {
        clients = clients.filter { it.id != client.id }
    }

    // Mail Management Methods
    fun authenticateMail(email: String, password: String, rememberMe: Boolean) {
        // Simulate email authentication
        isMailAuthenticated = true
        authenticatedEmail = email
    }

    fun fetchMails() {
        isFetchingMails = true
        viewModelScope.launch {
            try {
                // If authenticated to booking system, fetch invoices
                if (authToken != null) {
                    try {
                        val invoicesResponse = bookingService.getAllInvoices("Bearer $authToken")
                        if (invoicesResponse.success && invoicesResponse.data != null) {
                            // Convert invoices to mails
                            val invoiceMails = invoicesResponse.data!!.map { invoice ->
                                Mail(
                                    id = "invoice_${invoice.id}",
                                    sender = "bookings@amcen.com",
                                    recipient = invoice.email,
                                    subject = invoice.subject,
                                    content = invoice.content,
                                    timestamp = LocalDateTime.now(),
                                    isRead = false
                                )
                            }
                            mails = invoiceMails
                        }
                    } catch (e: Exception) {
                        // Fall back to default mail
                        mails = listOf(
                            Mail(
                                sender = "admin@example.com",
                                recipient = authenticatedEmail ?: "",
                                subject = "Welcome to CMS",
                                content = "Welcome to our Content Management System!",
                                timestamp = LocalDateTime.now()
                            )
                        )
                    }
                } else {
                    // Simulate fetching mails if not authenticated
                    mails = listOf(
                        Mail(
                            sender = "admin@example.com",
                            recipient = authenticatedEmail ?: "",
                            subject = "Welcome to CMS",
                            content = "Welcome to our Content Management System!",
                            timestamp = LocalDateTime.now()
                        )
                    )
                }
            } finally {
                isFetchingMails = false
            }
        }
    }

    fun fetchInvoices() {
        isFetchingInvoices = true
        viewModelScope.launch {
            try {
                if (authToken == null) {
                    loginWithDefaults()
                    return@launch
                }
                val response = bookingService.getAllInvoices("Bearer $authToken")
                if (response.success && response.data != null) {
                    // Convert invoices to mails format
                    mails = response.data!!.map { invoice ->
                        Mail(
                            id = "invoice_${invoice.id}",
                            sender = "bookings@amcen.com",
                            recipient = invoice.email,
                            subject = invoice.subject,
                            content = invoice.content,
                            timestamp = LocalDateTime.now(),
                            isRead = false
                        )
                    }
                    errorMessage = null
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Failed to fetch invoices: ${e.message}"
            } finally {
                isFetchingInvoices = false
            }
        }
    }

    fun sendMail(sender: String, recipient: String, subject: String, content: String) {
        mails = mails + Mail(
            sender = sender,
            recipient = recipient,
            subject = subject,
            content = content,
            timestamp = LocalDateTime.now()
        )
    }

    fun markMailAsRead(mail: Mail) {
        mails = mails.map { if (it.id == mail.id) it.copy(isRead = true) else it }
    }

    // Meeting Management Methods
    fun scheduleMeeting(meeting: Meeting) {
        meetings = meetings + meeting
    }

    fun updateMeeting(meeting: Meeting) {
        meetings = meetings.map { if (it.id == meeting.id) meeting else it }
    }

    fun cancelMeeting(meeting: Meeting) {
        meetings = meetings.filter { it.id != meeting.id }
    }
}
