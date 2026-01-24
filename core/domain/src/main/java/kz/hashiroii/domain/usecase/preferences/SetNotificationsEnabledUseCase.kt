package kz.hashiroii.domain.usecase.preferences

import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetNotificationsEnabledUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        preferencesRepository.setNotificationsEnabled(enabled)
    }
}
