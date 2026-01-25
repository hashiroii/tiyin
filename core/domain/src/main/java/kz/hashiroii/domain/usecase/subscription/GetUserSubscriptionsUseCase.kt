package kz.hashiroii.domain.usecase.subscription

import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import kz.hashiroii.domain.usecase.auth.GetCurrentUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class GetUserSubscriptionsUseCase @Inject constructor(
    private val firestoreSubscriptionRepository: FirestoreSubscriptionRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(): Flow<List<Subscription>> {
        val user = getCurrentUserUseCase()
        return if (user != null) {
            firestoreSubscriptionRepository.getUserSubscriptions(user.id)
        } else {
            emptyFlow()
        }
    }
}
