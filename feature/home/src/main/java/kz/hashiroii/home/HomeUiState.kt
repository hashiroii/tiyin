package kz.hashiroii.home

import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.ui.UiText

sealed interface HomeUiState {
    data object Loading : HomeUiState
    
    data class Success(
        val subscriptions: List<Subscription>,
        val activeSubscriptionsCount: Int,
        val totalCost: Double,
        val totalCostCurrency: String
    ) : HomeUiState
    
    data class Error(
        val message: UiText
    ) : HomeUiState
}
