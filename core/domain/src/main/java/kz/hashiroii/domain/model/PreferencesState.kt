package kz.hashiroii.domain.model

data class PreferencesState(
    val theme: String,
    val language: String,
    val currency: String,
    val cardView: String,
    val showSpendingCard: Boolean,
    val notificationsEnabled: Boolean
)
