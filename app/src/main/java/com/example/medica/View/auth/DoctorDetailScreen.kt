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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // PENTING
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.R
import com.example.medica.ViewModel.DoctorDetailViewModel
// IMPORT MODEL INI WAJIB ADA
import com.example.medica.Model.DoctorDetailData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailScreen(
    doctorId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToBook: () -> Unit,
    viewModel: DoctorDetailViewModel = viewModel(),
    patientViewModel: com.example.medica.ViewModel.PatientViewModel? = null // NEW: Accept PatientViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Load doctor data when screen is first displayed
    LaunchedEffect(doctorId) {
        android.util.Log.d("DoctorDetailScreen", "LaunchedEffect triggered with doctorId: $doctorId")
        if (doctorId != null && doctorId.isNotEmpty()) {
            android.util.Log.d("DoctorDetailScreen", "Calling viewModel.loadDoctor($doctorId)")
            viewModel.loadDoctor(doctorId)
        } else {
            android.util.Log.w("DoctorDetailScreen", "doctorId is null or empty, not loading")
        }
    }
    
    // Default doctor if null
    val doctor = uiState.doctor ?: DoctorDetailData()
    
    // Use default image since imageUrl is empty from Firestore
    val imageId = R.drawable.ic_launcher_foreground

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = doctor.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more),
                            contentDescription = "More",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    // Save doctor details to PatientViewModel BEFORE navigating
                    if (patientViewModel != null && doctor.id.isNotEmpty()) {
                        patientViewModel.setDoctorDetails(
                            doctorId = doctor.id,
                            doctorName = doctor.name,
                            specialty = doctor.specialty,
                            location = "Poli ${doctor.specialty}"
                        )
                    }
                    // Then navigate
                    onNavigateToBook()
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. CARD DOKTER
            // Gunakan imageId (Int) bukan doctor.imageRes (String)
            DoctorProfileCard(doctor.name, doctor.specialty, imageId)

            Spacer(modifier = Modifier.height(24.dp))

            DoctorStatsRow(doctor)

            Spacer(modifier = Modifier.height(24.dp))

            Text("About me", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${doctor.name} adalah dokter spesialis ${doctor.specialty} dengan pengalaman ${doctor.yearsExperience} tahun di ${doctor.hospital}. Melayani dengan profesional dan ramah.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. WORKING TIME (Hardcode dulu atau tambah field di Model)
            Text("Working time", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Monday - Friday, 08:00 AM - 18:00 PM", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // 5. REVIEWS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("See All", fontSize = 14.sp, color = Color(0xFF00C4B4), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            ReviewItem()

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// --- KOMPONEN PENDUKUNG ---

@Composable
fun DoctorProfileCard(name: String, specialty: String, imageResId: Int) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = imageResId), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.White))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray)
                Text(specialty, fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun DoctorStatsRow(doctor: DoctorDetailData) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatItem(iconRes = R.drawable.ic_launcher_foreground, value = "${doctor.patientsCount}+", label = "patients", color = Color(0xFFE0F7FA), iconTint = Color(0xFF00C4B4))
        StatItem(iconRes = R.drawable.ic_launcher_foreground, value = "${doctor.yearsExperience}+", label = "years exp.", color = Color(0xFFFCE4EC), iconTint = Color.Red)
        StatItem(iconRes = R.drawable.ic_launcher_foreground, value = doctor.rating.toString(), label = "rating", color = Color(0xFFFFF3E0), iconTint = Color.Yellow)
        StatItem(iconRes = R.drawable.ic_launcher_foreground, value = "${(doctor.rating * 100).toInt()}", label = "reviews", color = Color(0xFFE3F2FD), iconTint = Color.Blue)
    }
}

@Composable
fun StatItem(iconRes: Int, value: String, label: String, color: Color, iconTint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp).clip(CircleShape).background(color)) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF00C4B4))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ReviewItem() {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Muhammad waskito", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFFE0F7FA)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF00C4B4), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("5", color = Color(0xFF00C4B4), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dr. Raihan sangat ramah dan tepat dalam memberikan obat dan sabar menjelaskan penyakit saya", fontSize = 13.sp, color = Color.Gray)
        }
    }
}