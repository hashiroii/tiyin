package kz.hashiroii.domain.model.service

import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class SubscriptionPeriod {
    MONTHLY,
    YEARLY,
    WEEKLY,
    DAILY,
    QUARTERLY
}

data class Subscription(
    val id: String? = null,
    val serviceInfo: ServiceInfo,
    val amount: BigDecimal,
    val currency: String,
    val period: SubscriptionPeriod,
    val nextPaymentDate: LocalDate,
    val currentPaymentDate: LocalDate
) {
    fun daysUntilNextPayment(today: LocalDate = LocalDate.now()): Int {
        return if (nextPaymentDate.isAfter(today) || nextPaymentDate.isEqual(today)) {
            ChronoUnit.DAYS.between(today, nextPaymentDate).toInt()
        } else {
            0
        }
    }

    fun progressPercentage(today: LocalDate = LocalDate.now()): Float {
        val totalDays = when (period) {
            SubscriptionPeriod.MONTHLY -> 30
            SubscriptionPeriod.YEARLY -> 365
            SubscriptionPeriod.WEEKLY -> 7
            SubscriptionPeriod.DAILY -> 1
            SubscriptionPeriod.QUARTERLY -> 90
        }
        val daysPassed = ChronoUnit.DAYS.between(currentPaymentDate, today).toInt()
        return (daysPassed.toFloat() / totalDays).coerceIn(0f, 1f)
    }
}
