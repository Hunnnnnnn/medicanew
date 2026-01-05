package com.example.medica.View.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.Model.ProfileMenu
import com.example.medica.R
import com.example.medica.ViewModel.ProfileViewModel
import com.example.medica.View.home.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToAppointment: () -> Unit = {},
    onNavigateToArticles: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val menus by viewModel.menuList.collectAsState()
    
    // Initialize ViewModel with context
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more),
                            contentDescription = "More",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentTab = "Profile",
                onHomeClick = onNavigateToHome,
                onAppointmentClick = onNavigateToAppointment,
                onArticlesClick = onNavigateToArticles,
                onProfileClick = {}
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Foto Profil
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(Color(0xFFFF4081), Color(0xFF7C4DFF))))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(104.dp).clip(CircleShape).background(Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = userPhone, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

            // 2. LOGIKA KLIK MENU DIPERBARUI DI SINI
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(menus) { menu ->
                    ProfileMenuItem(
                        menu = menu,
                        onClick = {
                            when (menu.title) {
                                "Edit Profile" -> onNavigateToEditProfile()
                                "Language" -> onNavigateToLanguage()
                                "Logout" -> onLogout()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    menu: ProfileMenu,
    onClick: () -> Unit // 3. parameter diubah jadi 'onClick' umum
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Panggil onClick
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = menu.iconRes),
            contentDescription = null,
            tint = if (menu.isLogout) Color.Red else Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = menu.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (menu.isLogout) Color.Red else Color.Black, modifier = Modifier.weight(1f))

        if (!menu.isLogout) {
            if (menu.endText.isNotEmpty()) {
                Text(text = menu.endText, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
            }
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
        }
    }
}