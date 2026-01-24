package kz.hashiroii.domain.usecase.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kz.hashiroii.domain.model.PreferencesState
import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Inject

class GetPreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke(): Flow<PreferencesState> {
        return combine(
            combine(
                preferencesRepository.theme,
                preferencesRepository.language,
                preferencesRepository.currency
            ) { theme, language, currency ->
                Triple(theme, language, currency)
            },
            combine(
                preferencesRepository.cardView,
                preferencesRepository.showSpendingCard,
                preferencesRepository.notificationsEnabled
            ) { cardView, showSpendingCard, notificationsEnabled ->
                Triple(cardView, showSpendingCard, notificationsEnabled)
            }
        ) { (theme, language, currency), (cardView, showSpendingCard, notificationsEnabled) ->
            PreferencesState(
                theme = theme,
                language = language,
                currency = currency,
                cardView = cardView,
                showSpendingCard = showSpendingCard,
                notificationsEnabled = notificationsEnabled
            )
        }
    }
}
