package kz.hashiroii.data.service

import android.service.notification.StatusBarNotification
import kz.hashiroii.data.model.SubscriptionEntity
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import java.time.LocalDate
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionDetectionService @Inject constructor(
    private val serviceRecognizer: ServiceRecognizer
) {
    
    private val datePattern = Pattern.compile("(\\d{1,2})[./-](\\d{1,2})[./-](\\d{2,4})")
    
    fun detectSubscription(notification: StatusBarNotification): SubscriptionEntity? {
        val title = notification.notification.extras.getCharSequence("android.title")?.toString()
        val text = notification.notification.extras.getCharSequence("android.text")?.toString()
        val packageName = notification.packageName

        val isBankPackage = serviceRecognizer.isBankPackage(packageName)
        if (isBankPackage) {
            val paymentData = serviceRecognizer.extractPaymentData(title, text)
            if (paymentData == null || !paymentData.isRecurring) {
                return null
            }
        }

        val serviceInfo = serviceRecognizer.recognizeService(packageName, title, text)
            ?: return null

        val paymentData = serviceRecognizer.extractPaymentData(title, text)
        val cost = formatCost(paymentData)
        val period = detectPeriod(title, text)
        val nextPaymentDate = extractDate(text ?: title ?: "") ?: LocalDate.now().plusDays(30)
        val currentPaymentDate = LocalDate.now().minusDays(15)

        return SubscriptionEntity(
            serviceName = serviceInfo.name,
            cost = cost,
            period = period.name,
            nextPaymentDate = nextPaymentDate,
            currentPaymentDate = currentPaymentDate,
            serviceType = serviceInfo.serviceType.name,
            logoUrl = serviceInfo.logoUrls.firstOrNull(),
            primaryColor = serviceInfo.primaryColor,
            secondaryColor = serviceInfo.secondaryColor
        )
    }
    
    private fun formatCost(paymentData: kz.hashiroii.domain.model.service.ExtractedPaymentData?): String {
        if (paymentData == null) return "$0.00"
        
        val currencySymbol = when (paymentData.currency) {
            "KZT" -> "₸"
            "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> paymentData.currency
        }
        return "$currencySymbol${paymentData.amount}"
    }
    
    private fun detectPeriod(title: String?, text: String?): SubscriptionPeriod {
        val detectedPeriod = serviceRecognizer.detectPeriod(title, text)
        return when (detectedPeriod.uppercase()) {
            "MONTHLY" -> SubscriptionPeriod.MONTHLY
            "YEARLY" -> SubscriptionPeriod.YEARLY
            "WEEKLY" -> SubscriptionPeriod.WEEKLY
            "DAILY" -> SubscriptionPeriod.DAILY
            "QUARTERLY" -> SubscriptionPeriod.QUARTERLY
            else -> SubscriptionPeriod.MONTHLY
        }
    }
    
    private fun extractDate(text: String): LocalDate? {
        val matcher = datePattern.matcher(text)
        return if (matcher.find()) {
            try {
                val day = matcher.group(1)?.toInt() ?: return null
                val month = matcher.group(2)?.toInt() ?: return null
                val year = matcher.group(3)?.toInt() ?: return null
                val fullYear = if (year < 100) 2000 + year else year
                LocalDate.of(fullYear, month, day)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}
