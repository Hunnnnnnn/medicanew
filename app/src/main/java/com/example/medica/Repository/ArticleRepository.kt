package com.example.medica.Repository

import com.example.medica.Model.ArticleData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ArticleRepository {
    private val db = FirebaseFirestore.getInstance()
    private val articlesCollection = db.collection("articles")

    /**
     * Get trending articles for home screen
     */
    fun getTrendingArticles(
        onSuccess: (List<ArticleData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        articlesCollection
            .whereEqualTo("isTrending", true)
            .limit(2)
            .get()
            .addOnSuccessListener { documents ->
                val articles = documents.mapNotNull { document ->
                    document.toObject(ArticleData::class.java).copy(id = document.id)
                }
                onSuccess(articles)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load trending articles")
            }
    }

    /**
     * Get all articles or filter by category
     */
    fun getArticlesByCategory(
        category: String = "Newest",
        onSuccess: (List<ArticleData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val query = if (category == "Newest") {
            // Get all articles sorted by date
            articlesCollection.orderBy("date", Query.Direction.DESCENDING)
        } else {
            // Filter by specific category
            articlesCollection.whereEqualTo("category", category)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val articles = documents.mapNotNull { document ->
                    document.toObject(ArticleData::class.java).copy(id = document.id)
                }
                onSuccess(articles)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load articles")
            }
    }

    /**
     * Get single article by ID
     */
    fun getArticleById(
        articleId: String,
        onSuccess: (ArticleData) -> Unit,
        onFailure: (String) -> Unit
    ) {
        articlesCollection.document(articleId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val article = document.toObject(ArticleData::class.java)?.copy(id = document.id)
                    if (article != null) {
                        onSuccess(article)
                    } else {
                        onFailure("Failed to parse article")
                    }
                } else {
                    onFailure("Article not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load article")
            }
    }

    /**
     * Get default hardcoded articles (fallback if Firestore empty)
     */
    fun getDefaultArticles(): List<ArticleData> {
        return listOf(
            ArticleData(
                id = "1",
                title = "What are the symptoms of prostate cancer?",
                content = "Prostate cancer symptoms include...",
                category = "Health",
                date = "Dec 22, 2025",
                isTrending = true
            ),
            ArticleData(
                id = "2",
                title = "Salmonella is sneaky: Watch out",
                content = "Salmonella bacteria can cause food poisoning...",
                category = "Health",
                date = "Nov 9, 2025",
                isTrending = true
            ),
            ArticleData(
                id = "3",
                title = "COVID-19 Was a Top Cause of Death in 2020 and 2021, Even For Younger People",
                content = "COVID-19 pandemic impact analysis...",
                category = "Health",
                date = "Dec 22, 2025"
            ),
            ArticleData(
                id = "4",
                title = "Study Finds Being 'Hangry' is a Real Thing",
                content = "Research shows hunger affects mood...",
                category = "Lifestyle",
                date = "Dec 22, 2025"
            ),
            ArticleData(
                id = "5",
                title = "Why Childhood Obesity Rates Are Rising and What We Can Do",
                content = "Childhood obesity trends and solutions...",
                category = "Lifestyle",
                date = "Oct 21, 2025"
            )
        )
    }
}
