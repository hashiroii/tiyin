package kz.hashiroii.domain.usecase.subscription

import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import kz.hashiroii.domain.usecase.auth.GetCurrentUserUseCase
import javax.inject.Inject

class SyncSubscriptionsUseCase @Inject constructor(
    private val firestoreSubscriptionRepository: FirestoreSubscriptionRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(subscriptions: List<Subscription>): Result<Unit> {
        val user = getCurrentUserUseCase() ?: return Result.failure(Exception("User not authenticated"))
        return firestoreSubscriptionRepository.syncSubscriptions(user.id, subscriptions)
    }
}
