package kz.hashiroii.domain.repository

import kz.hashiroii.domain.model.service.Subscription
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getSubscriptions(): Flow<List<Subscription>>
    suspend fun refreshSubscriptions(userId: String?)
    suspend fun addSubscription(subscription: Subscription)
    suspend fun updateSubscription(oldSubscription: Subscription, newSubscription: Subscription)
    suspend fun deleteSubscription(subscription: Subscription)
    suspend fun getSubscriptionById(serviceName: String, serviceDomain: String): Subscription?
}
