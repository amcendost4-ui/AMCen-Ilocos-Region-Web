package com.example.myapplication.api

import retrofit2.Call
import retrofit2.http.*

// Booking data models
data class Booking(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val institution: String?,
    val dateRequested: String,
    val timeRequested: String,
    val purpose: String,
    val facility: String,
    val participants: Int?,
    val requests: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val notes: String?
)

data class BookingsResponse(
    val success: Boolean,
    val data: List<Booking>
)

data class BookingDetailResponse(
    val success: Boolean,
    val data: Booking
)

data class LoginRequest(
    val databaseId: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: User
) {
    data class User(
        val id: Int,
        val databaseId: String,
        val name: String,
        val role: String
    )
}

data class StatusUpdateRequest(
    val status: String,
    val notes: String?
)

data class StatusUpdateResponse(
    val success: Boolean,
    val message: String
)

// API Service Interface
interface BookingService {
    
    @POST("api/admin/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    
    @POST("api/admin/logout")
    fun logout(@Header("Authorization") token: String): Call<Map<String, Any>>
    
    @GET("api/bookings")
    fun getAllBookings(@Header("Authorization") token: String): Call<BookingsResponse>
    
    @GET("api/bookings/{id}")
    fun getBookingById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Call<BookingDetailResponse>
    
    @GET("api/bookings/filter/status/{status}")
    fun getBookingsByStatus(
        @Path("status") status: String,
        @Header("Authorization") token: String
    ): Call<BookingsResponse>
    
    @GET("api/bookings/search")
    fun searchBookings(
        @Query("query") query: String,
        @Header("Authorization") token: String
    ): Call<BookingsResponse>
    
    @PUT("api/bookings/{id}/status")
    fun updateBookingStatus(
        @Path("id") id: Int,
        @Body request: StatusUpdateRequest,
        @Header("Authorization") token: String
    ): Call<StatusUpdateResponse>
    
    @DELETE("api/bookings/{id}")
    fun deleteBooking(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Call<Map<String, Any>>
    
    @GET("api/health")
    fun healthCheck(): Call<Map<String, Any>>
}
