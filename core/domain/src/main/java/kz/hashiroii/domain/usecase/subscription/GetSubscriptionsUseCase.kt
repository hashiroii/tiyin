package kz.hashiroii.domain.usecase.subscription

import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kz.hashiroii.domain.repository.RoomSubscriptionRepository
import javax.inject.Inject

class GetSubscriptionsUseCase @Inject constructor(
    private val roomSubscriptionRepository: RoomSubscriptionRepository
) {
    operator fun invoke(): Flow<List<Subscription>> = roomSubscriptionRepository.getSubscriptions()
}
