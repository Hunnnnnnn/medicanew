package com.example.medica.View.doctor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medica.Model.DoctorDetailData
import com.example.medica.View.ui.DoctorItem
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorListScreen(
    specialty: String?,
    onNavigateBack: () -> Unit,
    onDoctorSelected: (DoctorDetailData) -> Unit
) {
    var doctors by remember { mutableStateOf<List<DoctorDetailData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()

    // Fetch doctors filtered by specialty
    LaunchedEffect(specialty) {
        isLoading = true
        errorMessage = null
        
        android.util.Log.d("DoctorListScreen", "Fetching doctors for specialty: $specialty")

        try {
            val query = if (specialty.isNullOrEmpty() || specialty == "All") {
                android.util.Log.d("DoctorListScreen", "Querying ALL doctors")
                db.collection("doctors")
            } else {
                android.util.Log.d("DoctorListScreen", "Querying doctors with specialty: $specialty")
                db.collection("doctors")
                    .whereEqualTo("specialty", specialty)
            }

            query.get()
                .addOnSuccessListener { result ->
                    android.util.Log.d("DoctorListScreen", "Query successful, found ${result.size()} doctors")
                    
                    val allDoctors = result.documents.mapNotNull { doc ->
                        try {
                            val doctor = doc.toObject(DoctorDetailData::class.java)?.copy(id = doc.id)
                            android.util.Log.d("DoctorListScreen", "Doctor: ${doctor?.name}, Available: ${doctor?.isAvailable}")
                            doctor
                        } catch (e: Exception) {
                            android.util.Log.e("DoctorListScreen", "Error parsing doctor: ${e.message}")
                            null
                        }
                    }
                    
                    // TEMPORARY: Show all doctors regardless of availability for debugging
                    doctors = allDoctors
                    // Original code (commented for debugging):
                    // doctors = allDoctors.filter { it.isAvailable }
                    
                    android.util.Log.d("DoctorListScreen", "Total doctors: ${allDoctors.size}, Showing: ${doctors.size}")
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("DoctorListScreen", "Query failed: ${e.message}", e)
                    errorMessage = e.message
                    isLoading = false
                }
        } catch (e: Exception) {
            android.util.Log.e("DoctorListScreen", "Exception in LaunchedEffect: ${e.message}", e)
            errorMessage = e.message
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = specialty?.let { "Dokter $it" } ?: "Semua Dokter",
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
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF00C4B4)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading doctors",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Unknown error",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                doctors.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tidak ada dokter tersedia",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = specialty?.let { "untuk spesialis $it" } ?: "",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(doctors) { doctor ->
                            Box(
                                modifier = Modifier.clickable {
                                    android.util.Log.d("DoctorListScreen", "Doctor clicked: ${doctor.name}, ID: ${doctor.id}")
                                    onDoctorSelected(doctor)
                                }
                            ) {
                                DoctorItem(doctor = doctor)
                            }
                        }
                    }
                }
            }
        }
    }
}
