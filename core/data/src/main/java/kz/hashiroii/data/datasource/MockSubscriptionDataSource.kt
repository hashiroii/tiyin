package kz.hashiroii.data.datasource

import kz.hashiroii.data.model.SubscriptionEntity
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockSubscriptionDataSource @Inject constructor() {
    
    fun getMockSubscriptions(): List<SubscriptionEntity> {
        val today = LocalDate.now()
        return listOf(
            SubscriptionEntity(
                serviceName = "Spotify",
                cost = "$9.99",
                period = "MONTHLY",
                nextPaymentDate = today.plusDays(5),
                currentPaymentDate = today.minusDays(25),
                serviceType = "STREAMING",
                logoUrl = "https://www.google.com/s2/favicons?domain=spotify.com&sz=256",
                primaryColor = 0xFF1DB954,
                secondaryColor = 0xFF191414
            ),
            SubscriptionEntity(
                serviceName = "Netflix",
                cost = "$15.99",
                period = "MONTHLY",
                nextPaymentDate = today.plusDays(12),
                currentPaymentDate = today.minusDays(18),
                serviceType = "STREAMING",
                logoUrl = "https://www.google.com/s2/favicons?domain=netflix.com&sz=256",
                primaryColor = 0xFFE50914,
                secondaryColor = 0xFF000000
            ),
            SubscriptionEntity(
                serviceName = "Apple Music",
                cost = "₸2990",
                period = "MONTHLY",
                nextPaymentDate = today.plusDays(8),
                currentPaymentDate = today.minusDays(22),
                serviceType = "STREAMING",
                logoUrl = "https://www.google.com/s2/favicons?domain=apple.com&sz=256",
                primaryColor = 0xFFFA243C,
                secondaryColor = 0xFF000000
            ),
            SubscriptionEntity(
                serviceName = "YouTube Premium",
                cost = "₸1990",
                period = "MONTHLY",
                nextPaymentDate = today.plusDays(15),
                currentPaymentDate = today.minusDays(15),
                serviceType = "STREAMING",
                logoUrl = "https://www.google.com/s2/favicons?domain=youtube.com&sz=256",
                primaryColor = 0xFFFF0000,
                secondaryColor = 0xFF000000
            ),
            SubscriptionEntity(
                serviceName = "Adobe Creative Cloud",
                cost = "$52.99",
                period = "MONTHLY",
                nextPaymentDate = today.plusDays(20),
                currentPaymentDate = today.minusDays(10),
                serviceType = "SOFTWARE",
                logoUrl = "https://www.google.com/s2/favicons?domain=adobe.com&sz=256",
                primaryColor = 0xFFFF0000,
                secondaryColor = 0xFF000000
            )
        )
    }
}
