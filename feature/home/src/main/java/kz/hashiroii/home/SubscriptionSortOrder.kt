package kz.hashiroii.home

import kz.hashiroii.domain.model.service.Subscription
import java.time.LocalDate

enum class SubscriptionSortOrder {
    /** Default: closest to expire (end date) first */
    EXPIRY_DATE,
    /** By cost (amount) descending */
    COST,
    /** By service name A–Z */
    ALPHABET
}

fun List<Subscription>.sortedBy(order: SubscriptionSortOrder): List<Subscription> =
    when (order) {
        SubscriptionSortOrder.EXPIRY_DATE -> sortedBy { it.nextPaymentDate }
        SubscriptionSortOrder.COST -> sortedByDescending { it.amount }
        SubscriptionSortOrder.ALPHABET -> sortedBy { it.serviceInfo.name.lowercase() }
    }
