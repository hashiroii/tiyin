package kz.hashiroii.domain.usecase.preferences

import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(language: String) {
        preferencesRepository.setLanguage(language)
    }
}
