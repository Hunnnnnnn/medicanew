package com.example.medica.View.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.Model.DoctorDetailData // <-- MODEL YANG ANDA GUNAKAN
import com.example.medica.R
import com.example.medica.Repository.DoctorRepository
import com.example.medica.ViewModel.HomeViewModel
import com.example.medica.View.ui.DoctorItem

val PrimaryColor = Color(0xFF00C4B4)
val LightPrimaryColor = Color(0xFFE0F7FA)

@Composable
fun HomeScreen(
    onNavigateToPoliDetails: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToFavorite: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDoctorDetail: (String) -> Unit,
    onNavigateToPoliBySpecialty: (String) -> Unit, // NEW: Navigate to filtered doctor list
    onNavigateToAppointment: () -> Unit = {},
    onNavigateToArticles: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    // Use ViewModel's state
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentTab = "Home",
                onProfileClick = onNavigateToProfile,
                onAppointmentClick = onNavigateToAppointment,
                onArticlesClick = onNavigateToArticles,
                onHomeClick = {}
            )

        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HomeHeader(
                userName = uiState.userName,
                onNotificationClick = onNavigateToNotification,
                onFavoriteClick = onNavigateToFavorite
            )

            Spacer(modifier = Modifier.height(24.dp))
            SearchBarSection()
            Spacer(modifier = Modifier.height(24.dp))
            BannerSection()
            Spacer(modifier = Modifier.height(24.dp))

            SpecialistSection(
                onMenuClick = { menuName ->
                    if (menuName == "Others") {
                        // Navigate to Poli Details screen for all specialties
                        onNavigateToPoliDetails()
                    } else {
                        // Navigate to filtered doctor list by specialty
                        onNavigateToPoliBySpecialty(menuName)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            TopDoctorsFilterSection()

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "Terjadi kesalahan",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (uiState.doctorList.isEmpty()) {
                Text(
                    text = "Belum ada dokter tersedia",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = uiState.doctorList) { doctor ->
                        Box(modifier = Modifier.clickable {
                            onNavigateToDoctorDetail(doctor.id)
                        }) {
                            DoctorItem(
                                doctor = doctor
                            )
                        }
                    }
                }
            }
            // ------------------------------------

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
@Composable
fun HomeHeader(
    userName: String,
    onNotificationClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Color(0xFF103B31)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Good Morning", fontSize = 12.sp, color = Color.Gray)
            Text(text = userName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onNotificationClick) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notif", modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onFavoriteClick) {
            Icon(Icons.Filled.Favorite, contentDescription = "Favorite", tint = Color(0xFF103B31), modifier = Modifier.size(28.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSection() {
    TextField(
        value = "", onValueChange = {},
        placeholder = { Text("Search", color = Color.Gray) },
        leadingIcon = { Icon(painterResource(id = R.drawable.ic_search), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp)) },
        trailingIcon = { Icon(painterResource(id = R.drawable.ic_filter), contentDescription = "Filter", tint = Color.Black, modifier = Modifier.size(24.dp)) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun BannerSection() {
    Card(colors = CardDefaults.cardColors(containerColor = PrimaryColor), shape = RoundedCornerShape(16.dp), modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)) {
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Get more info", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("about Diabetes", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("learn more", color = Color.White, fontSize = 12.sp)
            }
            Image(painter = painterResource(id = R.drawable.ic_home), contentDescription = null, modifier = Modifier.size(80.dp), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
fun SpecialistSection(onMenuClick: (String) -> Unit) {
    val menus = listOf(
        "Dentist" to R.drawable.ic_gigi, "Ophthalmology" to R.drawable.ic_mata,
        "Neurology" to R.drawable.ic_otak, "Orthopaedi" to R.drawable.ic_tulang,
        "Radiology" to R.drawable.ic_radiologi, "Nutrition" to R.drawable.ic_nutrisi,
        "THT" to R.drawable.ic_tht, "Others" to R.drawable.ic_other
    )
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Poli Details  ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Details", color = PrimaryColor, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        val rows = menus.chunked(4)
        rows.forEach { rowItems ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                rowItems.forEach { (name, iconRes) -> MenuItem(name, iconRes, onClick = { onMenuClick(name) }) }
            }
        }
    }
}

@Composable
fun MenuItem(name: String, iconRes: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(LightPrimaryColor), contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(id = iconRes), contentDescription = name, tint = PrimaryColor, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TopDoctorsFilterSection() {
    val filters = listOf("All", "General", "Dentist", "Neurologist", "Cardiologist")
    var selectedFilter by remember { mutableStateOf("All") }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Top Doctor", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("See All", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) PrimaryColor else Color(0xFFF5F5F5))
                    .clickable { selectedFilter = filter }
                    .padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(text = filter, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentTab: String = "Home",
    onProfileClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAppointmentClick: () -> Unit = {},
    onArticlesClick: () -> Unit = {}
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.vector__1_), contentDescription = "Home", modifier = Modifier.size(24.dp)) },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = currentTab == "Home",
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, indicatorColor = Color.White)
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.solar_calendar_linear), contentDescription = "Appt", modifier = Modifier.size(24.dp)) },
            label = { Text(stringResource(R.string.nav_appointment)) },
            selected = currentTab == "Appointment",
            onClick = onAppointmentClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, indicatorColor = Color.White)
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.hugeicons_google), contentDescription = "Articles", modifier = Modifier.size(24.dp)) },
            label = { Text(stringResource(R.string.nav_articles)) },
            selected = currentTab == "Articles",
            onClick = onArticlesClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, indicatorColor = Color.White)
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.iconamoon_profile_light), contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
            label = { Text(stringResource(R.string.nav_profile)) },
            selected = currentTab == "Profile",
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, indicatorColor = Color.White)
        )
    }
}