package kz.hashiroii.domain.usecase.preferences

import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(theme: String) {
        preferencesRepository.setTheme(theme)
    }
}
