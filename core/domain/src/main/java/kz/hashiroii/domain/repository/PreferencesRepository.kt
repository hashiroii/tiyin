package kz.hashiroii.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val theme: Flow<String>
    val language: Flow<String>
    val currency: Flow<String>
    val cardView: Flow<String>
    val showSpendingCard: Flow<Boolean>
    val notificationsEnabled: Flow<Boolean>

    suspend fun setTheme(theme: String)
    suspend fun setLanguage(language: String)
    suspend fun setCurrency(currency: String)
    suspend fun setCardView(cardView: String)
    suspend fun setShowSpendingCard(show: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
