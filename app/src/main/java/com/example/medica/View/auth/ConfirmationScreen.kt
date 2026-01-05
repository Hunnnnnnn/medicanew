package com.example.medica.View.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.medica.R
import com.example.medica.ViewModel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: PatientViewModel
) {
    val name by viewModel.name.collectAsState()
    val nik by viewModel.nik.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val dob by viewModel.dob.collectAsState()
    val phone by viewModel.phone.collectAsState()

    val date by viewModel.bookingDate.collectAsState()
    val time by viewModel.bookingTime.collectAsState()

    // Get doctor data from ViewModel
    val doctorName by viewModel.doctorName.collectAsState()
    val specialty by viewModel.specialty.collectAsState()
    val location by viewModel.location.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmation", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                onClick = { showSuccessDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Book Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = Color.White
    ) { innerPadding ->

        if (showSuccessDialog) {
            BookingSuccessDialog(
                onDismiss = {
                    showSuccessDialog = false
                    onNavigateToHome()
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // KARTU DOKTER (Dynamic data from ViewModel)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)).background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(doctorName.ifEmpty { "Doctor Name" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(specialty.ifEmpty { "Specialty" }, fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Appointment Info", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "Date & hour", value = "$date | $time")
            // Display poli name from specialty (e.g., "Poli Oftalmologi")
            InfoRow(label = "Location", value = if (specialty.isNotEmpty()) "Poli $specialty" else "Hospital")
            Spacer(modifier = Modifier.height(24.dp))
            Text("Patient Info", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailText(label = "Nama", value = name)
                    DetailText(label = "Gender", value = gender)
                    DetailText(label = "Tanggal Lahir", value = dob)
                    DetailText(label = "NIK", value = nik)
                    DetailText(label = "No HP", value = phone)
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailText(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row {
            Text("$label : ", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BookingSuccessDialog(onDismiss: () -> Unit) {
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
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF00C4B4).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF00C4B4)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Booking Success!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF00C4B4))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Appointment successfully Booked.", textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
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