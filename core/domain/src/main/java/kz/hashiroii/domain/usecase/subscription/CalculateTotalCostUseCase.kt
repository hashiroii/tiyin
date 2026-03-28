package kz.hashiroii.domain.usecase.subscription

import kz.hashiroii.domain.repository.CurrencyRepository
import kz.hashiroii.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kz.hashiroii.domain.repository.RoomSubscriptionRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class TotalCostResult(
    val total: Double,
    val targetCurrency: String
)

class CalculateTotalCostUseCase @Inject constructor(
    private val roomSubscriptionRepository: RoomSubscriptionRepository,
    private val currencyRepository: CurrencyRepository
) {
    operator fun invoke(
        targetCurrency: String = "KZT"
    ): Flow<TotalCostResult> {
        return combine(
            roomSubscriptionRepository.getSubscriptions(),
            currencyRepository.getExchangeRates()
        ) { subscriptions, rates ->
            val targetRate = rates[targetCurrency] ?: 1.0
            
            val total = subscriptions.fold(BigDecimal.ZERO) { acc, subscription ->
                val sourceRate = rates[subscription.currency] ?: 480.0
                
                val convertedAmount = if (subscription.currency == targetCurrency) {
                    subscription.amount
                } else {
                    subscription.amount
                        .multiply(BigDecimal.valueOf(sourceRate))
                        .divide(BigDecimal.valueOf(targetRate), 4, RoundingMode.HALF_UP)
                }
                
                acc.add(convertedAmount)
            }
            
            TotalCostResult(
                total = total.setScale(2, RoundingMode.HALF_UP).toDouble(),
                targetCurrency = targetCurrency
            )
        }
    }
}
