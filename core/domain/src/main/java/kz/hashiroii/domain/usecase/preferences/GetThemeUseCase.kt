package kz.hashiroii.domain.usecase.preferences

import kotlinx.coroutines.flow.Flow
import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke(): Flow<String> = preferencesRepository.theme
}
