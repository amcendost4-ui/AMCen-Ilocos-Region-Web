package com.example.myapplication.api

import retrofit2.http.*
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val institution: String = "",
    val dateRequested: String = "",
    val timeRequested: String = "",
    val purpose: String = "",
    val facility: String = "",
    val participants: Int = 0,
    val requests: String = "",
    val notes: String = "",
    val status: String = "pending",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class BookingsResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: List<Booking>? = null,
    val booking: Booking? = null
)

@Serializable
data class LoginRequest(
    val databaseId: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean = false,
    val message: String = "",
    val token: String? = null,
    val user: Map<String, Any>? = null
)

@Serializable
data class StatusUpdateRequest(
    val status: String
)

@Serializable
data class StatusUpdateResponse(
    val success: Boolean = false,
    val message: String = "",
    val booking: Booking? = null
)

@Serializable
data class Invoice(
    val id: Int = 0,
    val bookingId: Int = 0,
    val email: String = "",
    val subject: String = "",
    val content: String = "",
    val type: String = "confirmation",
    val status: String = "pending",
    val createdAt: String = "",
    val sentAt: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val facility: String = ""
)

@Serializable
data class InvoicesResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: List<Invoice>? = null
)

interface BookingService {
    @GET("api/health")
    suspend fun healthCheck(): BookingsResponse

    @POST("api/admin/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/admin/logout")
    @Headers("Content-Type: application/json")
    suspend fun logout(@Header("Authorization") token: String): BookingsResponse

    @GET("api/bookings")
    suspend fun getAllBookings(@Header("Authorization") token: String): BookingsResponse

    @GET("api/bookings/status/{status}")
    suspend fun getBookingsByStatus(
        @Path("status") status: String,
        @Header("Authorization") token: String
    ): BookingsResponse

    @GET("api/bookings/search")
    suspend fun searchBookings(
        @Query("q") query: String,
        @Header("Authorization") token: String
    ): BookingsResponse

    @PUT("api/bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") id: Int,
        @Body request: StatusUpdateRequest,
        @Header("Authorization") token: String
    ): StatusUpdateResponse

    @DELETE("api/bookings/{id}")
    suspend fun deleteBooking(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): BookingsResponse

    @POST("api/bookings/submit")
    suspend fun submitBooking(@Body booking: Booking): BookingsResponse

    @GET("api/invoices")
    suspend fun getAllInvoices(@Header("Authorization") token: String): InvoicesResponse

    @GET("api/invoices/booking/{bookingId}")
    suspend fun getInvoicesByBooking(
        @Path("bookingId") bookingId: Int,
        @Header("Authorization") token: String
    ): InvoicesResponse

    @GET("api/invoices/type/{type}")
    suspend fun getInvoicesByType(
        @Path("type") type: String,
        @Header("Authorization") token: String
    ): InvoicesResponse
}
