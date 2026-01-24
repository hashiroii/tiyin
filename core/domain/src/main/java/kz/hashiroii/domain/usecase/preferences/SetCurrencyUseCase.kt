package kz.hashiroii.domain.usecase.preferences

import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetCurrencyUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(currency: String) {
        preferencesRepository.setCurrency(currency)
    }
}
