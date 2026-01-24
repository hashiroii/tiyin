package kz.hashiroii.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {
    private val dataStore = context.dataStore

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val CURRENCY_KEY = stringPreferencesKey("currency")
        private val CARD_VIEW_KEY = stringPreferencesKey("card_view")
        private val SHOW_SPENDING_CARD_KEY = booleanPreferencesKey("show_spending_card")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    }

    private val preferencesFlow = dataStore.data.distinctUntilChanged()

    override val theme: Flow<String> = preferencesFlow.map { it[THEME_KEY] ?: "System" }.distinctUntilChanged()
    override val language: Flow<String> = preferencesFlow.map { it[LANGUAGE_KEY] ?: "" }.distinctUntilChanged()
    override val currency: Flow<String> = preferencesFlow.map { it[CURRENCY_KEY] ?: "KZT" }.distinctUntilChanged()
    override val cardView: Flow<String> = preferencesFlow.map { it[CARD_VIEW_KEY] ?: "Default" }.distinctUntilChanged()
    override val showSpendingCard: Flow<Boolean> = preferencesFlow.map { it[SHOW_SPENDING_CARD_KEY] ?: true }.distinctUntilChanged()
    override val notificationsEnabled: Flow<Boolean> = preferencesFlow.map { it[NOTIFICATIONS_ENABLED_KEY] ?: true }.distinctUntilChanged()

    override suspend fun setTheme(theme: String) {
        dataStore.edit { it[THEME_KEY] = theme }
    }

    override suspend fun setLanguage(language: String) {
        dataStore.edit { 
            it[LANGUAGE_KEY] = language
        }
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("language", language)
            .apply()
    }

    override suspend fun setCurrency(currency: String) {
        dataStore.edit { it[CURRENCY_KEY] = currency }
    }

    override suspend fun setCardView(cardView: String) {
        dataStore.edit { it[CARD_VIEW_KEY] = cardView }
    }

    override suspend fun setShowSpendingCard(show: Boolean) {
        dataStore.edit { it[SHOW_SPENDING_CARD_KEY] = show }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED_KEY] = enabled }
    }
}
