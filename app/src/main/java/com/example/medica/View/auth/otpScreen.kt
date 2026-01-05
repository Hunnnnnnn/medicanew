package com.example.medica.View.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.Model.PhoneAuthClient
import com.example.medica.ViewModel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phoneNumber: String,
    verificationId: String,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val phoneAuthClient = remember { PhoneAuthClient(context as Activity) }
    var otpCode by remember { mutableStateOf("") }
    val otpLength = 6
    val primaryColor = Color(0xFF00C4B4)

    LaunchedEffect(Unit) {
        viewModel.startResendTimer(60)
    }

    Scaffold(
        topBar = {
            IconButton(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        
        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Verification Code",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please enter code we just send to\n$phoneNumber",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- INPUT KODE OTP ---
            BasicTextField(
                value = otpCode,
                onValueChange = {
                    if (it.length <= otpLength && it.all { char -> char.isDigit() }) {
                        otpCode = it
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(otpLength) { index ->
                            val char = when {
                                index >= otpCode.length -> ""
                                else -> otpCode[index].toString()
                            }

                            Box(
                                modifier = Modifier
                                    .width(45.dp)
                                    .height(55.dp)
                                    .padding(4.dp)
                                    .border(
                                        width = if (index == otpCode.length) 2.dp else 1.dp,
                                        color = if (index == otpCode.length) primaryColor else Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(text = "Didn't receive OTP?", color = Color.Gray, fontSize = 14.sp)

            // --- LOGIKA TAMPILAN TIMER ---
            if (state.timer > 0) {
                // TAMPILKAN WAKTU BERJALAN
                val minutes = state.timer / 60
                val seconds = state.timer % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)

                Text(
                    text = "Resend Code in $timeString",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                // TAMPILKAN TOMBOL RESEND
                Text(
                    text = "Resend Code",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        val cleanPhone = phoneNumber.replace("+62", "")
                        // Saat diklik, sendOtp akan otomatis mereset timer lagi (lihat LoginViewModel)
                        viewModel.sendOtp(
                            phoneNumber = cleanPhone,
                            client = phoneAuthClient,
                            onCodeSent = { verificationId ->
                                Toast.makeText(context, "Kode dikirim ulang!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (otpCode.length == otpLength) {
                        // --- Verifikasi OTP dan Save ke Firestore ---
                        phoneAuthClient.verifyOtp(
                            verificationId = verificationId,
                            code = otpCode,
                            onSuccess = {
                                // Setelah OTP berhasil, cek dan simpan data user ke Firestore
                                viewModel.checkAndSaveUser(
                                    onSuccess = {
                                        Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    },
                                    onFailure = { errorMsg ->
                                        Toast.makeText(context, "Gagal menyimpan data: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, "Kode OTP salah: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Masukkan kode lengkap", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(25.dp),
                enabled = !state.isLoading && otpCode.length == otpLength
            ) {
                Text("Verify", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}