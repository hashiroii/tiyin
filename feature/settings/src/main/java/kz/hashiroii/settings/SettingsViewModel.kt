package kz.hashiroii.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kz.hashiroii.domain.usecase.preferences.GetPreferencesUseCase
import kz.hashiroii.domain.usecase.preferences.SetCardViewUseCase
import kz.hashiroii.domain.usecase.preferences.SetCurrencyUseCase
import kz.hashiroii.domain.usecase.preferences.SetLanguageUseCase
import kz.hashiroii.domain.usecase.preferences.SetNotificationsEnabledUseCase
import kz.hashiroii.domain.usecase.preferences.SetShowSpendingCardUseCase
import kz.hashiroii.domain.usecase.preferences.SetThemeUseCase
import kz.hashiroii.ui.LocaleHelper
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getPreferencesUseCase: GetPreferencesUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val setCardViewUseCase: SetCardViewUseCase,
    private val setShowSpendingCardUseCase: SetShowSpendingCardUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = getPreferencesUseCase()
        .map { preferences ->
            SettingsUiState.Success(
                theme = ThemePreference.valueOf(preferences.theme),
                language = LanguagePreference.fromCode(preferences.language),
                currency = preferences.currency,
                cardView = preferences.cardView,
                showSpendingCard = preferences.showSpendingCard,
                notificationsEnabled = preferences.notificationsEnabled
            ) as SettingsUiState
        }
        .catch { e ->
            emit(
                SettingsUiState.Error(
                    message = e.message ?: "Unknown error"
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = SettingsUiState.Loading
        )

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateTheme -> {
                viewModelScope.launch {
                    setThemeUseCase(intent.theme.name)
                }
            }
            is SettingsIntent.UpdateLanguage -> {
                viewModelScope.launch {
                    setLanguageUseCase(intent.language.code)
                }
            }
            is SettingsIntent.UpdateCurrency -> {
                viewModelScope.launch {
                    setCurrencyUseCase(intent.currency)
                }
            }
            is SettingsIntent.UpdateCardView -> {
                viewModelScope.launch {
                    setCardViewUseCase(intent.cardView)
                }
            }
            is SettingsIntent.UpdateShowSpendingCard -> {
                viewModelScope.launch {
                    setShowSpendingCardUseCase(intent.show)
                }
            }
            is SettingsIntent.UpdateNotifications -> {
                viewModelScope.launch {
                    setNotificationsEnabledUseCase(intent.enabled)
                }
            }
            is SettingsIntent.OpenFeedback -> {
            }
            is SettingsIntent.OpenAbout -> {
            }
            is SettingsIntent.OpenLicenses -> {
            }
            is SettingsIntent.DeleteAccount -> {
            }
        }
    }
}
