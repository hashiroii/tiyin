package kz.hashiroii.domain.usecase.subscription

import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.NotificationRepository
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import kz.hashiroii.domain.usecase.auth.GetCurrentUserUseCase
import javax.inject.Inject

class UpdateSubscriptionUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val firestoreSubscriptionRepository: FirestoreSubscriptionRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(oldSubscription: Subscription, newSubscription: Subscription): Result<Unit> {
        return try {
            val subscriptionId = oldSubscription.id ?: return Result.failure(IllegalStateException("Subscription has no id"))
            val updated = newSubscription.copy(id = subscriptionId)

            notificationRepository.updateSubscription(oldSubscription, updated)
            getCurrentUserUseCase()?.let { user ->
                firestoreSubscriptionRepository.updateSubscription(user.id, subscriptionId, updated)
                    .onFailure { /* sync failed; local update already applied */ }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
