package kz.hashiroii.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocaleHelper {
    suspend fun applyLanguage(languageCode: String) {
        withContext(Dispatchers.Main) {
            applyLanguageSync(languageCode)
        }
    }
    
    fun applyLanguageSync(languageCode: String) {
        val localeList = if (languageCode.isEmpty() || languageCode == "System") {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        
        AppCompatDelegate.setApplicationLocales(localeList)
    }
    
    fun wrap(context: Context, languageCode: String): Context {
        if (languageCode.isEmpty() || languageCode == "System") {
            return context
        }
        
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        } else {
            config.setLocale(locale)
        }
        
        return context.createConfigurationContext(config)
    }
}
