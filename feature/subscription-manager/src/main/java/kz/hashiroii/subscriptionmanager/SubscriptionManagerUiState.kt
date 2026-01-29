package kz.hashiroii.subscriptionmanager

import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import kz.hashiroii.domain.usecase.subscription.ServiceSearchResult
import java.math.BigDecimal
import java.time.LocalDate

sealed interface SubscriptionManagerUiState {
    data object Loading : SubscriptionManagerUiState
    
    data class Editing(
        val isEditMode: Boolean,
        val originalSubscription: Subscription?,
        val serviceSearchQuery: String = "",
        val serviceSearchResult: ServiceSearchResult? = null,
        val selectedService: ServiceSearchResult? = null,
        val amount: String = "",
        val currency: String = "USD",
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
        val period: SubscriptionPeriod = SubscriptionPeriod.MONTHLY,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : SubscriptionManagerUiState
    
    data object Success : SubscriptionManagerUiState
    
    data class Error(
        val message: String
    ) : SubscriptionManagerUiState
}
