package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

/**
 * Bookings Content Screen for CMS
 * Displays all bookings from the online booking form
 * Allows filtering by status and searching
 */
@Composable
fun BookingsContent(
    bookings: List<Booking>,
    selectedStatusFilter: String,
    onFilterStatus: (String) -> Unit,
    onSearchBooking: (String) -> Unit,
    onUpdateStatus: (Int, String) -> Unit,
    isFetching: Boolean
) {
    val tealColor = Color(0xFF17A2B8)
    
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            
            // ===== Header =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Booking Requests",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                // Refresh button
                Button(
                    onClick = { onSearchBooking("") },
                    colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isFetching
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isFetching) "Refreshing..." else "Refresh", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ===== Status Filter Tabs =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all", "pending", "approved", "rejected", "completed").forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { onFilterStatus(status) },
                        label = { Text(status.uppercase(), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tealColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            // ===== Bookings List =====
            if (bookings.isEmpty() && !isFetching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventNote,
                            null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No bookings found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(bookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onStatusChange = onUpdateStatus
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Individual Booking Card
 */
@Composable
fun BookingCard(
    booking: Booking,
    onStatusChange: (Int, String) -> Unit
) {
    val tealColor = Color(0xFF17A2B8)
    val statusColor = when (booking.status) {
        "pending" -> Color(0xFFFFC107)
        "approved" -> Color(0xFF28A745)
        "rejected" -> Color(0xFFDC3545)
        "completed" -> Color(0xFF6C757D)
        else -> Color.Gray
    }
    
    var showStatusDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            
            // ===== Header Row =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Booking #${booking.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "${booking.firstName} ${booking.lastName}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
                
                // Status Badge
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = booking.status.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // ===== Details Grid =====
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BookingDetailItem("Email", booking.email)
                BookingDetailItem("Phone", booking.phone)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BookingDetailItem("Institution", booking.institution)
                BookingDetailItem("Facility", booking.facility)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BookingDetailItem("Date", booking.dateRequested)
                BookingDetailItem("Time", booking.timeRequested)
                BookingDetailItem("Participants", booking.participants.toString())
            }
            
            if (booking.purpose.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Purpose: ${booking.purpose}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            // ===== Action Buttons =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showStatusDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17A2B8)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Change Status", fontSize = 11.sp)
                }
                
                Button(
                    onClick = { onStatusChange(booking.id, "completed") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Complete", fontSize = 11.sp)
                }
            }
        }
    }
    
    // Status Change Dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Booking Status") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("pending", "approved", "rejected", "completed").forEach { status ->
                        Button(
                            onClick = {
                                onStatusChange(booking.id, status)
                                showStatusDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (status) {
                                    "approved" -> Color(0xFF28A745)
                                    "rejected" -> Color(0xFFDC3545)
                                    "completed" -> Color(0xFF6C757D)
                                    else -> Color(0xFFFFC107)
                                }
                            )
                        ) {
                            Text(status.uppercase(), color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BookingDetailItem(label: String, value: String) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
