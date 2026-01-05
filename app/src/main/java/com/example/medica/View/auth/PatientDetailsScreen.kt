package com.example.medica.View.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medica.ViewModel.PatientViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNext: () -> Unit,
    viewModel: PatientViewModel
) {
    // Ambil data dari ViewModel
    val nama by viewModel.name.collectAsState()
    val nik by viewModel.nik.collectAsState()
    val tglLahir by viewModel.dob.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val noTelepon by viewModel.phone.collectAsState()

    // --- STATE UNTUK DATE PICKER ---
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // --- LOGIKA DATE PICKER ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Format Tanggal: "5 Juli 2000"
                            val formatter = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
                            val formattedDate = formatter.format(Date(millis))
                            viewModel.onDobChange(formattedDate)
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4))
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color(0xFF00C4B4))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF00C4B4),
                    todayDateBorderColor = Color(0xFF00C4B4),
                    todayContentColor = Color(0xFF00C4B4)
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                onClick = onNavigateToNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. NAMA (Keyboard Biasa / QWERTY)
            PatientInputItem(
                label = "Nama Lengkap (sesuai KTP)",
                value = nama,
                onValueChange = { viewModel.onNameChange(it) },
                keyboardType = KeyboardType.Text
            )

            // 2. NIK (Keyboard Angka)
            PatientInputItem(
                label = "Nomor Induk Kewarganegaraan",
                value = nik,
                onValueChange = { viewModel.onNikChange(it) },
                keyboardType = KeyboardType.Number // Hanya Angka
            )

            // 3. TANGGAL LAHIR (Pake Date Picker)
            PatientDateInput(
                label = "Tanggal Lahir",
                value = tglLahir,
                onClick = { showDatePicker = true } // Klik untuk buka kalender
            )

            // 4. GENDER (Pake Dropdown)
            PatientDropdownInput(
                label = "Gender",
                currentValue = gender,
                options = listOf("Pria", "Wanita"),
                onOptionSelected = { viewModel.onGenderChange(it) }
            )

            // 5. NO TELEPON (Keyboard Angka/Phone)
            PatientInputItem(
                label = "No telepon",
                value = noTelepon,
                onValueChange = { viewModel.onPhoneChange(it) },
                keyboardType = KeyboardType.Phone // Keyboard khusus telepon
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// --- KOMPONEN INPUT TEXT BIASA & ANGKA ---
@Composable
fun PatientInputItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            // Setting Keyboard disini
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00C4B4),
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

// --- KOMPONEN INPUT TANGGAL (READ ONLY + ICON) ---
@Composable
fun PatientDateInput(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))

        // Box agar bisa di-klik area-nya
        Box(modifier = Modifier.clickable { onClick() }) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true, // Tidak bisa diketik manual
                enabled = false, // Agar event klik ditangani oleh Box
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF00C4B4))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.LightGray,
                    disabledContainerColor = Color.White,
                    disabledTrailingIconColor = Color(0xFF00C4B4)
                )
            )
        }
    }
}

// --- KOMPONEN INPUT DROPDOWN (GENDER) ---
@Composable
fun PatientDropdownInput(
    label: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))

        Box {
            OutlinedTextField(
                value = currentValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Black)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C4B4),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            // Overlay transparan agar seluruh textfield bisa diklik untuk buka dropdown
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Lebar menyesuaikan (sedikit trik UI)
                    .background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}