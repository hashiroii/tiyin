package kz.hashiroii.tiyin

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kz.hashiroii.domain.usecase.preferences.GetLanguageUseCase
import kz.hashiroii.ui.LocaleHelper
import javax.inject.Inject

@HiltAndroidApp
class TiyinApplication : Application() {
    
    @Inject
    lateinit var getLanguageUseCase: GetLanguageUseCase
    
    override fun onCreate() {
        super.onCreate()
        
        runBlocking {
            try {
                if (::getLanguageUseCase.isInitialized) {
                    val languageCode = getLanguageUseCase().first()
                    if (languageCode.isNotEmpty() && languageCode != "System") {
                        LocaleHelper.applyLanguageSync(languageCode)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}
