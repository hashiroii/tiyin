package kz.hashiroii.domain.repository

import kz.hashiroii.domain.model.service.Subscription
import kotlinx.coroutines.flow.Flow

interface FirestoreSubscriptionRepository {
    fun getUserSubscriptions(userId: String): Flow<List<Subscription>>
    suspend fun saveSubscription(userId: String, subscription: Subscription): Result<Unit>
    suspend fun deleteSubscription(userId: String, subscriptionId: String): Result<Unit>
    suspend fun syncSubscriptions(userId: String, subscriptions: List<Subscription>): Result<Unit>
}
