package com.example.medica.ui.auth

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.Model.PhoneAuthClient
import com.example.medica.ViewModel.SignUpViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOtp: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()

    val phoneAuthClient = remember {
        PhoneAuthClient(context as Activity)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Pria", "Wanita")

    val primaryColor = Color(0xFF00C4B4)

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
                            viewModel.onDobChange(formatter.format(Date(millis)))
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) { Text("OK", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = primaryColor) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(primaryColor))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.LightGray))
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Sign up", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(24.dp))

            SignUpInputItem(
                label = "Nama Lengkap (sesuai KTP)",
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) }
            )

            SignUpInputItem(
                label = "Nomor Induk Kewarganegaraan",
                value = uiState.nik,
                onValueChange = { viewModel.onNikChange(it) },
                keyboardType = KeyboardType.Number
            )

            SignUpInputItem(
                label = "Email",
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                keyboardType = KeyboardType.Email
            )

            Text("Tanggal Lahir", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }.padding(bottom = 16.dp)) {
                OutlinedTextField(
                    value = uiState.dob,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Black) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Black,
                        disabledContainerColor = Color.White,
                        disabledTrailingIconColor = Color.Black
                    )
                )
            }

            Text("Gender", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                OutlinedTextField(
                    value = uiState.gender,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { genderExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Black)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { genderExpanded = true })

                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.onGenderChange(option)
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            SignUpInputItem(
                label = "No telepon",
                value = uiState.phone,
                onValueChange = { viewModel.onPhoneChange(it) },
                keyboardType = KeyboardType.Phone
            )

            SignUpInputItem(
                label = "Password",
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            SignUpInputItem(
                label = "Confirm Password",
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // ============================================
                    // FEATURE FLAG: Toggle antara Email/Password vs Phone OTP
                    // ============================================
                    if (com.example.medica.Config.FeatureFlags.ENABLE_PHONE_OTP) {
                        // ===== PHONE OTP AUTH (requires Blaze Plan) =====
                        viewModel.registerAndSendOtp(
                            client = phoneAuthClient,
                            onOtpSent = { verificationId ->
                                val fullNumber = "+62${uiState.phone}"
                                onNavigateToOtp(fullNumber, verificationId)
                            },
                            onAutoVerified = {
                                val fullNumber = "+62${uiState.phone}"
                                onNavigateToOtp(fullNumber, "")
                            }
                        )
                    } else {
                        // ===== EMAIL/PASSWORD AUTH (FREE - no Blaze Plan needed) =====
                        val emailAuthClient = com.example.medica.Model.EmailAuthClient()
                        val userRepository = com.example.medica.Repository.UserRepository()
                        
                        viewModel.registerWithEmail(
                            emailAuthClient = emailAuthClient,
                            userRepository = userRepository,
                            onSuccess = {
                                android.widget.Toast.makeText(
                                    context,
                                    "Registrasi Berhasil!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                onNavigateToLogin() // Navigate back to login
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(25.dp),
                enabled = !uiState.isLoading
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", color = Color.Gray, fontSize = 14.sp)
                Text(
                    "Log in",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
fun SignUpInputItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            if (passwordVisible) "👁️" else "👁‍🗨",
                            fontSize = 18.sp
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00C4B4),
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}