package kz.hashiroii.subscriptionmanager

import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import java.math.BigDecimal
import java.time.LocalDate

sealed interface SubscriptionManagerIntent {
    data object LoadSubscription : SubscriptionManagerIntent
    data class UpdateServiceSearchQuery(val query: String) : SubscriptionManagerIntent
    data class SelectService(val serviceResult: kz.hashiroii.domain.usecase.subscription.ServiceSearchResult) : SubscriptionManagerIntent
    data class UpdateAmount(val amount: String) : SubscriptionManagerIntent
    data class UpdateCurrency(val currency: String) : SubscriptionManagerIntent
    data class UpdateStartDate(val date: LocalDate) : SubscriptionManagerIntent
    data class UpdateEndDate(val date: LocalDate) : SubscriptionManagerIntent
    data class UpdatePeriod(val period: SubscriptionPeriod) : SubscriptionManagerIntent
    data object SaveSubscription : SubscriptionManagerIntent
    data object DeleteSubscription : SubscriptionManagerIntent
    data object ClearSelectedService : SubscriptionManagerIntent
    data object Cancel : SubscriptionManagerIntent
}
