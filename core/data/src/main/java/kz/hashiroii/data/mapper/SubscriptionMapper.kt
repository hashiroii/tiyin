package kz.hashiroii.data.mapper

import kz.hashiroii.data.model.SubscriptionEntity
import kz.hashiroii.domain.model.service.ServiceInfo
import kz.hashiroii.domain.model.service.ServiceType
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import java.math.BigDecimal

object SubscriptionMapper {
    fun toDomain(entity: SubscriptionEntity): Subscription {
        val (amount, currency) = parseCost(entity.cost)
        
        return Subscription(
            serviceInfo = ServiceInfo(
                name = entity.serviceName,
                logoUrls = if (entity.logoUrl != null) listOf(entity.logoUrl) else emptyList(),
                logoUrl = entity.logoUrl,
                logoResId = 0,
                primaryColor = entity.primaryColor,
                secondaryColor = entity.secondaryColor,
                serviceType = parseServiceType(entity.serviceType)
            ),
            amount = amount,
            currency = currency,
            period = parsePeriod(entity.period),
            nextPaymentDate = entity.nextPaymentDate,
            currentPaymentDate = entity.currentPaymentDate
        )
    }
    
    fun toEntity(domain: Subscription): SubscriptionEntity {
        return SubscriptionEntity(
            serviceName = domain.serviceInfo.name,
            cost = formatCost(domain.amount, domain.currency),
            period = domain.period.name,
            nextPaymentDate = domain.nextPaymentDate,
            currentPaymentDate = domain.currentPaymentDate,
            serviceType = domain.serviceInfo.serviceType.name,
            logoUrl = domain.serviceInfo.effectiveLogoUrl,
            primaryColor = domain.serviceInfo.primaryColor,
            secondaryColor = domain.serviceInfo.secondaryColor
        )
    }
    
    private fun parseCost(cost: String): Pair<BigDecimal, String> {
        val currencySymbols = mapOf(
            "$" to "USD",
            "€" to "EUR",
            "₸" to "KZT",
            "₽" to "RUB",
            "£" to "GBP"
        )
        
        val currencyText = mapOf(
            "dollar" to "USD",
            "euro" to "EUR",
            "тенге" to "KZT",
            "рубль" to "RUB",
            "pound" to "GBP"
        )
        
        var currencyCode = "USD"
        for ((symbol, code) in currencySymbols) {
            if (cost.contains(symbol)) {
                currencyCode = code
                break
            }
        }
        
        if (currencyCode == "USD") {
            val lowerCost = cost.lowercase()
            for ((text, code) in currencyText) {
                if (lowerCost.contains(text)) {
                    currencyCode = code
                    break
                }
            }
        }
        
        val cleaned = cost.replace(Regex("[^0-9.,]"), "").replace(",", ".")
        val amount = cleaned.toBigDecimalOrNull() ?: BigDecimal.ZERO
        
        return Pair(amount, currencyCode)
    }
    
    private fun formatCost(amount: BigDecimal, currency: String): String {
        val symbol = when (currency) {
            "KZT" -> "₸"
            "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> currency
        }
        return "$symbol${amount.toPlainString()}"
    }
    
    private fun parsePeriod(period: String): SubscriptionPeriod {
        return when (period.uppercase()) {
            "MONTHLY" -> SubscriptionPeriod.MONTHLY
            "YEARLY" -> SubscriptionPeriod.YEARLY
            "WEEKLY" -> SubscriptionPeriod.WEEKLY
            "DAILY" -> SubscriptionPeriod.DAILY
            "QUARTERLY" -> SubscriptionPeriod.QUARTERLY
            else -> SubscriptionPeriod.MONTHLY
        }
    }
    
    private fun parseServiceType(serviceType: String): ServiceType {
        return when (serviceType.uppercase()) {
            "STREAMING" -> ServiceType.STREAMING
            "SOFTWARE" -> ServiceType.SOFTWARE
            "AUDIOBOOK" -> ServiceType.AUDIOBOOK
            "NEWS" -> ServiceType.NEWS
            "GAMING" -> ServiceType.GAMING
            "FITNESS" -> ServiceType.FITNESS
            "EDUCATION" -> ServiceType.EDUCATION
            "OTHER" -> ServiceType.OTHER
            else -> ServiceType.OTHER
        }
    }
}
