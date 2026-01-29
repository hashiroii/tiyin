package kz.hashiroii.domain.usecase.subscription

import java.util.UUID
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.NotificationRepository
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import kz.hashiroii.domain.usecase.auth.GetCurrentUserUseCase
import javax.inject.Inject

class AddSubscriptionUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val firestoreSubscriptionRepository: FirestoreSubscriptionRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(subscription: Subscription): Result<Unit> {
        return try {
            val docId = UUID.nameUUIDFromBytes(
                "${subscription.serviceInfo.domain}-${subscription.serviceInfo.name}".toByteArray()
            ).toString()
            val subscriptionWithId = subscription.copy(id = docId)

            // Save locally first so UI updates immediately; sync to Firestore best-effort.
            notificationRepository.addSubscription(subscriptionWithId)
            getCurrentUserUseCase()?.let { user ->
                firestoreSubscriptionRepository.saveSubscription(user.id, subscriptionWithId, docId)
                    .onFailure { /* sync failed; subscription is still saved locally */ }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
