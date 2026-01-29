package kz.hashiroii.domain.usecase.subscription

import kz.hashiroii.domain.repository.NotificationRepository
import kz.hashiroii.domain.usecase.auth.GetCurrentUserUseCase
import javax.inject.Inject

class RefreshSubscriptionsUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke() {
        val user = getCurrentUserUseCase()
        repository.refreshSubscriptions(user?.id)
    }
}
