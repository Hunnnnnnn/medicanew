package com.example.medica.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    
    /**
     * Set app locale based on language preference
     * @param context Application context
     * @param languageCode "English" or "Indonesia"
     * @return Updated context with new locale
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "Indonesia" -> Locale("id", "ID")
            else -> Locale("en", "US")
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    /**
     * Get locale code from language name
     */
    fun getLocaleCode(languageName: String): String {
        return when (languageName) {
            "Indonesia" -> "id"
            else -> "en"
        }
    }
    
    /**
     * Get language name from locale code
     */
    fun getLanguageName(localeCode: String): String {
        return when (localeCode) {
            "id" -> "Indonesia"
            else -> "English"
        }
    }
}
