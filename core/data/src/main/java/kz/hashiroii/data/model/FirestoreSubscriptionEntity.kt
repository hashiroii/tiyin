package kz.hashiroii.data.model

import java.time.Instant
import java.time.ZoneId

data class SubscriptionFirestoreEntity(
    val serviceName: String = "",
    val serviceDomain: String = "",
    val cost: String = "",
    val period: String = "",
    val nextPaymentDate: Long = 0,
    val currentPaymentDate: Long = 0,
    val serviceType: String = "",
    val primaryColor: Long = 0,
    val secondaryColor: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val logoUrl: String = ""
) {
    fun toEntity(id: String): SubscriptionEntity {
        return SubscriptionEntity(
            id = id,
            serviceName = serviceName,
            serviceDomain = serviceDomain,
            cost = cost,
            period = period,
            nextPaymentDate = Instant.ofEpochMilli(nextPaymentDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
            currentPaymentDate = Instant.ofEpochMilli(currentPaymentDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
            serviceType = serviceType,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
        )
    }

    companion object {
        fun fromEntity(entity: SubscriptionEntity): SubscriptionFirestoreEntity {
            return SubscriptionFirestoreEntity(
                serviceName = entity.serviceName,
                serviceDomain = entity.serviceDomain,
                cost = entity.cost,
                period = entity.period,
                nextPaymentDate = entity.nextPaymentDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
                currentPaymentDate = entity.currentPaymentDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
                serviceType = entity.serviceType,
                primaryColor = entity.primaryColor,
                secondaryColor = entity.secondaryColor,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}