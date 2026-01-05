package com.example.medica.View.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medica.R
import com.example.medica.ViewModel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val languages = listOf(
        LanguageOption("English", "English(UK)"),
        LanguageOption("Indonesia", "Indonesia")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Suggested",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Language options
            languages.forEach { language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage == language.name,
                    onClick = {
                        viewModel.setLanguage(context, language.name)
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.restart_required),
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Language",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional languages (you can add more)
            val additionalLanguages = listOf(
                LanguageOption("Mandarin", "Mandarin"),
                LanguageOption("Hindi", "Hindi"),
                LanguageOption("Spanish", "Spanish"),
                LanguageOption("Bengali", "Bengali"),
                LanguageOption("Arabic", "Arabic")
            )
            
            additionalLanguages.forEach { language ->
                LanguageItem(
                    language = language,
                    isSelected = selectedLanguage == language.name,
                    onClick = {
                        viewModel.setLanguage(context, language.name)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language.displayName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF00C4B4),
                unselectedColor = Color.Gray
            )
        )
    }
}

data class LanguageOption(
    val name: String,
    val displayName: String
)
