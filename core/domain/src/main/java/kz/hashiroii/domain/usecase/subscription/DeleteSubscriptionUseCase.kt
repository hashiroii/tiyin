package kz.hashiroii.domain.usecase.subscription

import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import kz.hashiroii.domain.repository.NotificationRepository
import kz.hashiroii.domain.usecase.auth.GetCurrentUserUseCase
import javax.inject.Inject

class DeleteSubscriptionUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val firestoreSubscriptionRepository: FirestoreSubscriptionRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(subscription: Subscription): Result<Unit> {
        return try {
            val subscriptionId = subscription.id ?: return Result.failure(IllegalStateException("Subscription has no id"))

            notificationRepository.deleteSubscription(subscription)
            getCurrentUserUseCase()?.let { user ->
                firestoreSubscriptionRepository.deleteSubscription(user.id, subscriptionId)
                    .onFailure { /* sync failed; local delete already applied */ }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
