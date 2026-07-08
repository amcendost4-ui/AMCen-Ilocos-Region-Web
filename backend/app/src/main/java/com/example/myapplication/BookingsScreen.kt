package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.Booking

@Composable
fun BookingsContent(
    bookings: List<Booking>,
    selectedStatusFilter: String,
    onFilterStatus: (String) -> Unit,
    onSearchBooking: (String) -> Unit,
    onUpdateStatus: (Int, String) -> Unit,
    isFetching: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var newStatus by remember { mutableStateOf("pending") }

    val pendingCount = bookings.count { it.status == "pending" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with new bookings count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Bookings Management",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (pendingCount > 0) {
                Badge(
                    containerColor = Color(0xFFFF4444),
                    contentColor = Color.White
                ) {
                    Text("$pendingCount New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchBooking(it)
            },
            placeholder = { Text("Search bookings...") },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val statuses = listOf("all", "pending", "approved", "rejected", "completed")
            statuses.forEach { status ->
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = { onFilterStatus(status) },
                    label = { Text(status.capitalize()) },
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        // Bookings List
        if (isFetching) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No bookings found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings) { booking ->
                    BookingCard(
                        booking = booking,
                        onStatusChange = {
                            selectedBooking = booking
                            newStatus = booking.status
                            showStatusDialog = true
                        }
                    )
                }
            }
        }
    }

    // Status Change Dialog
    if (showStatusDialog && selectedBooking != null) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Booking Status") },
            text = {
                Column {
                    val statuses = listOf("pending", "approved", "rejected", "completed")
                    statuses.forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = newStatus == status,
                                onClick = { newStatus = status }
                            )
                            Text(status.capitalize(), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(selectedBooking!!.id, newStatus)
                        showStatusDialog = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                Button(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onStatusChange: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Name
            Text(
                "${booking.firstName} ${booking.lastName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Booking Details
            BookingDetailItem("Email", booking.email)
            BookingDetailItem("Phone", booking.phone)
            BookingDetailItem("Institution", booking.institution)
            BookingDetailItem("Date Requested", booking.dateRequested)
            BookingDetailItem("Time Requested", booking.timeRequested)
            BookingDetailItem("Purpose", booking.purpose)
            BookingDetailItem("Facility", booking.facility)
            BookingDetailItem("Participants", booking.participants.toString())
            
            if (booking.requests.isNotEmpty()) {
                BookingDetailItem("Requests", booking.requests)
            }
            
            if (booking.notes.isNotEmpty()) {
                BookingDetailItem("Notes", booking.notes)
            }

            // Status Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = when (booking.status) {
                    "pending" -> Color(0xFFFFC107)
                    "approved" -> Color(0xFF4CAF50)
                    "rejected" -> Color(0xFFF44336)
                    "completed" -> Color(0xFF2196F3)
                    else -> Color.Gray
                }

                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        booking.status.capitalize(),
                        color = Color.White,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status Change Button
                Button(
                    onClick = onStatusChange,
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Edit, "Change Status", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change Status", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BookingDetailItem(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                label,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(120.dp),
                fontSize = 12.sp
            )
            Text(
                value,
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
