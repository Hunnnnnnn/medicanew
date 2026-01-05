package com.example.medica.Model

data class ArticleData(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "", // Newest, Health, Lifestyle, Cancer
    val imageUrl: String = "",
    val imageRes: Int = 0, // For local drawable resources
    val date: String = "",
    val readTime: String = "5 min read",
    val isTrending: Boolean = false
)
