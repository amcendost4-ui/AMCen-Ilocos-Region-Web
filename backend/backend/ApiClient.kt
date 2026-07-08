package com.example.myapplication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.squareup.okhttp3.OkHttpClient
import com.squareup.okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ApiClient {
    
    // For Android Emulator (use 10.0.2.2 as host)
    private const val BASE_URL = "http://10.0.2.2:5000/"
    
    // For Physical Device: uncomment and use your actual IP
    // private const val BASE_URL = "http://192.168.x.x:5000/"
    
    private var retrofit: Retrofit? = null
    private var bookingService: BookingService? = null
    
    fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            
            // Create logging interceptor for debugging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
    
    fun getBookingService(): BookingService {
        if (bookingService == null) {
            bookingService = getRetrofit().create(BookingService::class.java)
        }
        return bookingService!!
    }
}
