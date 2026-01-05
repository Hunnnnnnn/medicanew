package com.example.medica.View.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument // <- PENTING: Import ini
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.View.auth.OnboardingScreen
import com.example.medica.View.auth.OtpScreen
import com.example.medica.View.doctor.DoctorListScreen
import com.example.medica.View.home.AppointmentScreen
import com.example.medica.View.home.FavoriteDoctorScreen
import com.example.medica.View.home.HomeScreen
import com.example.medica.View.home.NotificationScreen
import com.example.medica.View.home.PoliScreen
import com.example.medica.View.profile.ProfileScreen
import com.example.medica.View.profile.EditProfileScreen
import com.example.medica.View.profile.LanguageScreen
import com.example.medica.ui.auth.LoginScreen
import com.example.medica.ui.auth.SignUpScreen
import com.example.medica.View.home.BookAppointmentScreen
import com.example.medica.View.home.PatientDetailsScreen
import com.example.medica.View.auth.ConfirmationScreen
import com.example.medica.View.auth.DoctorDetailScreen
import com.example.medica.View.article.ArticleMainScreen
import com.example.medica.View.article.ArticleDetailScreen
import com.example.medica.ViewModel.ProfileViewModel
import com.example.medica.ViewModel.PatientViewModel
import com.example.medica.ViewModel.AppointmentViewModel

// Definisikan kunci argumen
const val DOCTOR_ID_KEY = "doctorId"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sharedProfileViewModel: ProfileViewModel = viewModel()
    val patientViewModel: PatientViewModel = viewModel()
    val appointmentViewModel: AppointmentViewModel = viewModel()

    NavHost(navController = navController, startDestination = "onboarding") {

        composable("onboarding") {
            OnboardingScreen(onNavigateToLogin = { navController.navigate("login") { popUpTo("onboarding") { inclusive = true } } })
        }

        composable("login") {
            LoginScreen(
                onNavigateBack = {},
                onNavigateToOtp = { phone, vId ->
                    navController.navigate("otp/${phone.replace("+", "")}/$vId")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onGoogleSignInClick = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            com.example.medica.ui.auth.SignUpScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOtp = { phone, vId ->
                    navController.navigate("otp/${phone.replace("+", "")}/$vId")
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("otp/{phone}/{vId}") {
            val phone = it.arguments?.getString("phone") ?: ""
            val vId = it.arguments?.getString("vId") ?: ""
            OtpScreen(
                phoneNumber = "+$phone",
                verificationId = vId,
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToPoliDetails = { navController.navigate("poli_details") },
                onNavigateToNotification = { navController.navigate("notification") },
                onNavigateToFavorite = { navController.navigate("favorite") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToDoctorDetail = { doctorId ->
                    navController.navigate("doctor_detail/$doctorId")
                },
                onNavigateToPoliBySpecialty = { specialty ->
                    navController.navigate("doctor_list/$specialty")
                },
                onNavigateToAppointment = { navController.navigate("appointment") },
                onNavigateToArticles = { navController.navigate("articles") }
            )
        }

        // Doctor List filtered by specialty
        composable(
            route = "doctor_list/{specialty}",
            arguments = listOf(navArgument("specialty") { type = NavType.StringType })
        ) { backStackEntry ->
            val specialty = backStackEntry.arguments?.getString("specialty") ?: ""
            
            DoctorListScreen(
                specialty = specialty,
                onNavigateBack = { navController.popBackStack() },
                onDoctorSelected = { doctor ->
                    // Navigate to doctor detail screen to show full info
                    navController.navigate("doctor_detail/${doctor.id}")
                }
            )
        }

        // Doctor Detail Screen
        composable(
            route = "doctor_detail/{doctorId}",
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            android.util.Log.d("AppNavigation", "DoctorDetail route - doctorId from arguments: '$doctorId'")

            DoctorDetailScreen(
                doctorId = doctorId,
                patientViewModel = patientViewModel, // Pass PatientViewModel
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBook = {
                    // DoctorDetailScreen already saved doctor details
                    navController.navigate("book_appointment")
                }
            )
        }

        composable("poli_details") { PoliScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("notification") { NotificationScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("favorite") { FavoriteDoctorScreen(onNavigateBack = { navController.popBackStack() }) }

        // Profile Routes
        composable("profile") {
            ProfileScreen(
                onNavigateToHome = { navController.navigate("home") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToEditProfile = { navController.navigate("edit_profile") },
                onNavigateToLanguage = { navController.navigate("language") },
                onNavigateToAppointment = { navController.navigate("appointment") },
                onNavigateToArticles = { navController.navigate("articles") }
            )
        }

        composable("edit_profile") {
            val profileViewModel: ProfileViewModel = viewModel()
            EditProfileScreen(
                onNavigateBack = { 
                    profileViewModel.reloadUserData()
                    navController.popBackStack() 
                },
                viewModel = profileViewModel
            )
        }

        composable("language") {
            LanguageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Article Routes
        composable("articles") {
            ArticleMainScreen(
                onNavigateToDetail = { articleId ->
                    navController.navigate("article_detail/$articleId")
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToAppointment = { navController.navigate("appointment") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable(
            route = "article_detail/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            ArticleDetailScreen(
                articleId = articleId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("appointment") {
            AppointmentScreen(
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToReschedule = { appointmentId ->
                    navController.navigate("reschedule_appointment/$appointmentId")
                },
                viewModel = appointmentViewModel
            )
        }

        composable("book_appointment") {
            BookAppointmentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = { date, time ->
                    patientViewModel.setBookingDetails(date, time)
                    navController.navigate("patient_details")
                },
                isReschedule = false
            )
        }

        composable(
            route = "reschedule_appointment/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            
            BookAppointmentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = { newDate, newTime ->
                    if (appointmentId.isEmpty()) {
                        android.widget.Toast.makeText(context, "Invalid appointment ID", android.widget.Toast.LENGTH_SHORT).show()
                        return@BookAppointmentScreen
                    }
                    
                    android.widget.Toast.makeText(context, "Rescheduling...", android.widget.Toast.LENGTH_SHORT).show()
                    
                    appointmentViewModel.rescheduleAppointment(
                        appointmentId = appointmentId,
                        newDate = newDate,
                        newTime = newTime,
                        onSuccess = {
                            android.widget.Toast.makeText(context, "Appointment rescheduled!", android.widget.Toast.LENGTH_SHORT).show()
                            appointmentViewModel.onTabSelected("upcoming")
                            navController.navigate("appointment") {
                                popUpTo("appointment") { inclusive = true }
                            }
                        }
                    )
                },
                isReschedule = true
            )
        }

        composable("patient_details") {
            PatientDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = {
                    navController.navigate("confirmation_screen")
                },
                viewModel = patientViewModel
            )
        }

        composable("confirmation_screen") {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            ConfirmationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    val date = patientViewModel.bookingDate.value
                    val time = patientViewModel.bookingTime.value
                    
                    // Check if user is logged in
                    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (currentUser == null) {
                        android.widget.Toast.makeText(context, "Please login first", android.widget.Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                        return@ConfirmationScreen
                    }
                    
                    // Create AppointmentData object with REAL data from PatientViewModel
                    val appointment = com.example.medica.Model.AppointmentData(
                        userId = currentUser.uid,  // Real user ID
                        patientName = patientViewModel.name.value, // Patient name from Patient Details
                        doctorId = patientViewModel.doctorId.value,
                        doctorName = patientViewModel.doctorName.value,
                        specialty = patientViewModel.specialty.value,
                        date = date,
                        time = time,
                        // Use specialty as poli/location (e.g., "Poli Oftalmologi")
                        location = if (patientViewModel.specialty.value.isNotEmpty()) {
                            "Poli ${patientViewModel.specialty.value}"
                        } else {
                            patientViewModel.location.value.ifEmpty { "Hospital" }
                        },
                        imageRes = com.example.medica.R.drawable.ic_doctor,
                        status = "upcoming"
                    )
                    
                    android.widget.Toast.makeText(context, "Creating appointment...", android.widget.Toast.LENGTH_SHORT).show()
                    
                    appointmentViewModel.createAppointment(
                        appointment = appointment,
                        onSuccess = {
                            android.widget.Toast.makeText(context, "Appointment created!", android.widget.Toast.LENGTH_SHORT).show()
                            appointmentViewModel.onTabSelected("upcoming")
                            navController.navigate("appointment") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    )
                },
                viewModel = patientViewModel
            )
        }
    }
}
