package com.example.medica.View.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.Model.AppointmentData
import com.example.medica.R
import com.example.medica.ViewModel.AppointmentViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToArticles: () -> Unit, // NEW: Add Articles navigation
    onNavigateToReschedule: (String) -> Unit,  // Changed to accept appointmentId
    viewModel: AppointmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredAppointments = uiState.appointments.filter { it.status == uiState.selectedTab }

    // Get current user name from Firebase Auth
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val patientName = currentUser?.displayName ?: "Pasien"

    // --- STATE DIALOG ---
    var showQueueDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSuccessCancelDialog by remember { mutableStateOf(false) }

    // Simpan ID appointment yang mau di-cancel (String sekarang)
    var selectedAppointmentId by remember { mutableStateOf<String?>(null) }

    // Simulasi Antrian - Count UP (mendekat ke nomor user)
    val yourNumber = 12
    var currentQueue by remember { mutableIntStateOf(10) }

    LaunchedEffect(key1 = showQueueDialog) {
        if (showQueueDialog) {
            // Start from current number, COUNT UP towards yourNumber
            currentQueue = 10
            while (currentQueue < yourNumber && showQueueDialog) {
                delay(5000) // Wait 5 seconds
                currentQueue += 1 // INCREASE by 1 (approaches your number)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, tint = Color(0xFF00C4B4), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("My Appointment", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentTab = "Appointment",
                onHomeClick = onNavigateToHome,
                onArticlesClick = onNavigateToArticles, // Connect Articles navigation
                onProfileClick = onNavigateToProfile,
                onAppointmentClick = {}
            )
        },
        containerColor = Color.White
    ) { innerPadding ->

        // 1. DIALOG ANTRIAN (QUEUE)
        if (showQueueDialog) {
            QueueDialog(
                onDismiss = { showQueueDialog = false },
                onRescheduleClick = {
                    showQueueDialog = false
                    // Pass the selected appointment ID for reschedule
                    selectedAppointmentId?.let { appointmentId ->
                        onNavigateToReschedule(appointmentId)
                    }
                },
                patientName = patientName, // Pass real patient name
                yourNumber = yourNumber,
                currentNumber = currentQueue // Use live countdown value
            )
        }

        // 2. DIALOG ALASAN CANCEL
        if (showCancelDialog) {
            CancelReasonDialog(
                onDismiss = { showCancelDialog = false },
                onConfirmCancel = {
                    // Eksekusi Cancel di ViewModel (String ID)
                    selectedAppointmentId?.let { id ->
                        viewModel.cancelAppointment(id) {}
                    }
                    showCancelDialog = false
                    showSuccessCancelDialog = true
                }
            )
        }

        // 3. DIALOG SUKSES CANCEL
        if (showSuccessCancelDialog) {
            CancelSuccessDialog(
                onDismiss = {
                    showSuccessCancelDialog = false
                    // Pindah ke tab Cancelled otomatis
                    viewModel.onTabSelected("cancelled")
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            AppointmentTabs(uiState.selectedTab) { viewModel.onTabSelected(it) }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00C4B4))
                }
            } else if (filteredAppointments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Belum ada appointment",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredAppointments) { item ->
                        AppointmentCardItem(
                            data = item,
                            onViewDetailsClick = { 
                                selectedAppointmentId = item.id
                                showQueueDialog = true 
                            },
                            onCancelClick = {
                                selectedAppointmentId = item.id
                                showCancelDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentTabs(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("upcoming", "completed", "cancelled")
    val tabLabels = listOf("Upcoming", "Completed", "Cancelled")
    val selectedIndex = tabs.indexOf(selectedTab)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, tab ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLabels[index],
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == tab) Color(0xFF00C4B4) else Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.LightGray.copy(alpha = 0.3f))) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (selectedIndex > 0) Spacer(modifier = Modifier.weight(selectedIndex.toFloat()))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF00C4B4), RoundedCornerShape(2.dp)))
                if (selectedIndex < tabs.size - 1) Spacer(modifier = Modifier.weight((tabs.size - 1 - selectedIndex).toFloat()))
            }
        }
    }
}

@Composable
fun AppointmentCardItem(
    data: AppointmentData,
    onViewDetailsClick: () -> Unit = {},
    onCancelClick: () -> Unit = {} // Parameter Baru
) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = data.imageRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.White))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(data.doctorName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)
                    Text(data.specialty, fontSize = 14.sp)
                    Text("${data.date} | ${data.time}", fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (data.status == "upcoming") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCancelClick, // Panggil fungsi cancel
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFF00C4B4)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00C4B4))
                    ) {
                        Text("Cancel Appointment", fontSize = 11.sp, maxLines = 1)
                    }
                    Button(onClick = onViewDetailsClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4))) {
                        Text("View Details", fontSize = 11.sp, color = Color.White)
                    }
                }
            } else {
                Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4))) {
                    Text("Book Again", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun QueueDialog(
    onDismiss: () -> Unit,
    onRescheduleClick: () -> Unit,
    patientName: String, // NEW: Accept patient name as parameter
    yourNumber: Int,
    currentNumber: Int,
    avgTimePerPatient: Int = 15
) {
    val diff = yourNumber - currentNumber
    val waitingTimeMinutes = if (diff > 0) diff * avgTimePerPatient else 0
    val timeText = if (diff <= 0) "Giliran Anda!" else if (waitingTimeMinutes >= 60) {
        val hours = waitingTimeMinutes / 60
        val mins = waitingTimeMinutes % 60
        if (mins > 0) "$hours Jam $mins Menit" else "$hours Jam"
    } else "$waitingTimeMinutes Menit"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nama Pasien:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(patientName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))
                Text("No Antrian", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                Text(text = "$yourNumber", fontSize = 80.sp, color = Color(0xFF00C4B4), fontWeight = FontWeight.Light, lineHeight = 80.sp)
                Text("POLI UMUM", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C4B4))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Antrian saat ini: $currentNumber", fontSize = 12.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(20.dp))
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = if (diff <= 0) Color(0xFFC8E6C9) else Color(0xFFE0F7FA))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, tint = Color(0xFF00C4B4), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Estimasi Menunggu", fontSize = 11.sp, color = Color.Gray)
                            Text(if (diff <= 0) "SILAHKAN MASUK" else "± $timeText", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C4B4))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRescheduleClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("RESCHEDULE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// --- UPDATE: DIALOG ALASAN CANCEL DENGAN TEXTFIELD ---
@Composable
fun CancelReasonDialog(onDismiss: () -> Unit, onConfirmCancel: () -> Unit) {
    val reasons = listOf(
        "I want to change to another doctor",
        "I have recovered from the disease",
        "I just want to cancel",
        "Others"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }

    // STATE BARU: Untuk menyimpan teks alasan "Others"
    var otherReasonText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Cancel Appointment", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                Text("What is the reason for the cancellation?", textAlign = TextAlign.Center, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                reasons.forEach { reason ->
                    // Bungkus dalam Column agar TextField bisa muncul di bawah RadioButton
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (selectedReason == reason),
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00C4B4))
                            )
                            Text(text = reason, fontSize = 13.sp)
                        }

                        // LOGIKA BARU: Jika "Others" dipilih, munculkan TextField
                        if (reason == "Others" && selectedReason == "Others") {
                            OutlinedTextField(
                                value = otherReasonText,
                                onValueChange = { otherReasonText = it },
                                placeholder = { Text("Enter your reason...", fontSize = 12.sp, color = Color.Gray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                    .height(100.dp), // Tinggi kotak isian
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00C4B4),
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedContainerColor = Color(0xFFF9F9F9),
                                    unfocusedContainerColor = Color(0xFFF9F9F9)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)).padding(12.dp)) {
                    Text("Are you sure want to cancel your appointment?", textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB2DFDB))) { Text("Back", color = Color.White) }
                    Button(onClick = onConfirmCancel, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4))) { Text("Yes, Cancel", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun CancelSuccessDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF00C4B4).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF00C4B4)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Cancel Appointment Success", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF00C4B4), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your appointment has been confirmed as canceled.", textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("View Appointment", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}