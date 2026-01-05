package com.example.medica.ui.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.R
import com.example.medica.Model.GoogleAuthClient
import com.example.medica.Model.PhoneAuthClient
import com.example.medica.ViewModel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOtp: (String, String) -> Unit, // Callback OTP (Untuk Phone)
    onLoginSuccess: () -> Unit, // Callback Sukses (Untuk Email)
    onNavigateToSignUp: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val googleAuthClient = remember { GoogleAuthClient(context) }
    val phoneAuthClient = remember { PhoneAuthClient(context as Activity) }

    // State Pilihan Tab (0 = Phone, 1 = Email)
    var selectedTab by remember { mutableIntStateOf(0) }

    // State Input
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF00C4B4)

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            IconButton(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        containerColor = Color.White
    ) { innerPadding ->

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gambar Ilustrasi
            Image(
                painter = painterResource(id = R.drawable.img_login_illustration),
                contentDescription = null,
                modifier = Modifier.height(180.dp).fillMaxWidth(),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Login to Your Account", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF103B31))
            Spacer(modifier = Modifier.height(24.dp))

            // --- TAB SWITCHER (Phone vs Email) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                // Tab Phone
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == 0) Color.White else Color.Transparent)
                        .clickable { selectedTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Phone Number",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) primaryColor else Color.Gray,
                        fontSize = 14.sp
                    )
                }

                // Tab Email
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == 1) Color.White else Color.Transparent)
                        .clickable { selectedTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Email",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) primaryColor else Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- FORM BERDASARKAN TAB ---
            if (selectedTab == 0) {
                // === LAYOUT PHONE NUMBER ===
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.all { char -> char.isDigit() }) phoneNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    placeholder = { Text("Enter phone number") },
                    leadingIcon = {
                        Row(modifier = Modifier.padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("+62", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Check feature flag for Phone OTP
                        if (com.example.medica.Config.FeatureFlags.ENABLE_PHONE_OTP) {
                            viewModel.sendOtp(
                                phoneNumber = phoneNumber,
                                client = phoneAuthClient,
                                onCodeSent = { verificationId ->
                                    val fullNumber = "+62$phoneNumber"
                                    onNavigateToOtp(fullNumber, verificationId)
                                },
                                onAutoVerified = {
                                    onLoginSuccess()
                                }
                            )
                        } else {
                            // Phone OTP disabled - show message
                            android.widget.Toast.makeText(
                                context,
                                "Phone OTP tidak tersedia. Gunakan Email/Password atau Google Sign-In",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !state.isLoading
                ) {
                    Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

            } else {
                // === LAYOUT EMAIL & PASSWORD ===
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = Color.LightGray),
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val iconPainter = if (isPasswordVisible) painterResource(id = R.drawable.ic_visible) else painterResource(id = R.drawable.ic_invisible)
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(painter = iconPainter, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = Color.LightGray),
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val emailAuthClient = com.example.medica.Model.EmailAuthClient()
                        viewModel.signInWithEmail(
                            email = email,
                            password = password,
                            emailAuthClient = emailAuthClient,
                            onSuccess = {
                                onLoginSuccess()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !state.isLoading
                ) {
                    Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- DIVIDER & GOOGLE ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("OR", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { viewModel.signInWithGoogle(googleAuthClient) { onGoogleSignInClick() } },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                enabled = !state.isLoading
            ) {
                Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Sign in with Google", color = Color.Black)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = Color.Gray)
                Text("Sign Up", color = primaryColor, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToSignUp() })
            }
        }
    }
}