package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.ArticleData
import com.example.medica.Repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArticleUiState(
    val trendingArticles: List<ArticleData> = emptyList(),
    val articles: List<ArticleData> = emptyList(),
    val selectedCategory: String = "Newest",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ArticleViewModel : ViewModel() {
    
    private val repository = ArticleRepository()
    
    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()
    
    init {
        loadTrendingArticles()
        loadArticles()
    }
    
    /**
     * Load trending articles
     */
    private fun loadTrendingArticles() {
        viewModelScope.launch {
            repository.getTrendingArticles(
                onSuccess = { articles ->
                    _uiState.update { it.copy(trendingArticles = articles) }
                },
                onFailure = { error ->
                    android.util.Log.e("ArticleVM", "Error loading trending: $error")
                    // Use default articles as fallback
                    val defaultArticles = repository.getDefaultArticles().filter { it.isTrending }
                    _uiState.update { it.copy(trendingArticles = defaultArticles) }
                }
            )
        }
    }
    
    /**
     * Load articles by selected category
     */
    fun loadArticles(category: String = _uiState.value.selectedCategory) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            repository.getArticlesByCategory(
                category = category,
                onSuccess = { articles ->
                    _uiState.update {
                        it.copy(
                            articles = articles,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("ArticleVM", "Error loading articles: $error")
                    // Use default articles as fallback
                    val defaultArticles = repository.getDefaultArticles()
                    _uiState.update {
                        it.copy(
                            articles = defaultArticles,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Select category and reload articles
     */
    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadArticles(category)
    }
}
