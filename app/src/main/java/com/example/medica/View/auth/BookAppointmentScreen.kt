package com.example.medica.View.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.R
import com.example.medica.ViewModel.BookAppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    onNavigateBack: () -> Unit,
    // 1. UBAH INI: Menerima 2 String (Tanggal & Jam)
    onNavigateToNext: (String, String) -> Unit,
    viewModel: BookAppointmentViewModel = viewModel(),
    isReschedule: Boolean = false
) {
    val state by viewModel.state.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isReschedule) "Reschedule Appointment" else "Book Appointment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (isReschedule) {
                        // Jika Reschedule, tampilkan dialog sukses dulu
                        showSuccessDialog = true
                    } else {
                        // Use ViewModel formatted date instead of hardcoded
                        val formattedDate = viewModel.getFormattedDate()
                        onNavigateToNext(formattedDate, state.selectedTime)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = if (isReschedule) "Confirm Reschedule" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = Color.White
    ) { innerPadding ->

        if (showSuccessDialog) {
            RescheduleSuccessDialog(
                onDismiss = {
                    showSuccessDialog = false
                    // Use ViewModel formatted date
                    val formattedDate = viewModel.getFormattedDate()
                    onNavigateToNext(formattedDate, state.selectedTime)
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // 1. SELECT DATE SECTION
            Text("Select Date", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "Previous Month",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(state.currentMonth, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Next Month",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                    Text(day, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            val dates = (1..state.daysInMonth).toList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp)
            ) {
                // Add empty cells for first day offset
                items(state.firstDayOfWeek) {
                    Box(modifier = Modifier.size(40.dp))
                }
                
                items(dates) { date ->
                    val isSelected = date == state.selectedDate

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF00C4B4) else Color.Transparent)
                            .clickable { viewModel.onDateSelected(date) }
                    ) {
                        Text(
                            text = date.toString(),
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Select Hour", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            val times = listOf("10.00", "11.00", "12.00", "13.00", "14.00", "15.00", "16.00")

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(150.dp)
            ) {
                items(times) { time ->
                    val isSelected = time == state.selectedTime

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(40.dp)
                            .border(1.dp, if (isSelected) Color(0xFF00C4B4) else Color.LightGray, RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF00C4B4) else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { viewModel.onTimeSelected(time) }
                    ) {
                        Text(
                            text = time,
                            color = if (isSelected) Color.White else Color(0xFF00C4B4),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RescheduleSuccessDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00C4B4).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C4B4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Rescheduling Success!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF00C4B4))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Appointment successfully changed. You will receive a notification and the doctor you selected will contact you.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("View Appointment", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}