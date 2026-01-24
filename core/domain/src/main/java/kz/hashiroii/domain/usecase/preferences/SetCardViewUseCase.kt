package kz.hashiroii.domain.usecase.preferences

import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetCardViewUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(cardView: String) {
        preferencesRepository.setCardView(cardView)
    }
}
