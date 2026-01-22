package kz.hashiroii.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kz.hashiroii.designsystem.TiyinChip
import kz.hashiroii.designsystem.theme.TiyinTheme
import kz.hashiroii.domain.model.service.ServiceInfo
import kz.hashiroii.domain.model.service.ServiceType
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = subscription.cost,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subscription.period.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(subscription.serviceInfo.primaryColor))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TiyinChip(
                    text = getServiceTypeLabel(subscription.serviceInfo.serviceType),
                    modifier = Modifier
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            val daysLeft = subscription.daysUntilNextPayment()
            Text(
                text = if (daysLeft > 0) "$daysLeft days left" else "Due today",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            LinearProgressIndicator(
                progress = { subscription.progressPercentage() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Next payment: ${subscription.nextPaymentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getServiceTypeLabel(serviceType: ServiceType): String {
    return when (serviceType) {
        ServiceType.STREAMING -> "Streaming"
        ServiceType.AUDIOBOOK -> "Audiobook"
        ServiceType.SOFTWARE -> "Software"
        ServiceType.NEWS -> "News"
        ServiceType.FITNESS -> "Fitness"
        ServiceType.EDUCATION -> "Education"
        ServiceType.GAMING -> "Gaming"
        ServiceType.CLOUD_STORAGE -> "Cloud Storage"
        ServiceType.PRODUCTIVITY -> "Productivity"
        ServiceType.OTHER -> "Other"
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun SubscriptionCardLightPreview() {
    TiyinTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                SubscriptionCard(
                    subscription = Subscription(
                        serviceInfo = ServiceInfo(
                            name = "Spotify",
                            logoResId = 0,
                            primaryColor = 0xFF1DB954,
                            secondaryColor = 0xFF191414,
                            serviceType = ServiceType.STREAMING
                        ),
                        cost = "$9.99",
                        period = SubscriptionPeriod.MONTHLY,
                        nextPaymentDate = LocalDate.now().plusDays(5),
                        currentPaymentDate = LocalDate.now().minusDays(25)
                    )
                )
            }
        }
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun SubscriptionCardDarkPreview() {
    TiyinTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                SubscriptionCard(
                    subscription = Subscription(
                        serviceInfo = ServiceInfo(
                            name = "Netflix",
                            logoResId = 0,
                            primaryColor = 0xFFE50914,
                            secondaryColor = 0xFF000000,
                            serviceType = ServiceType.STREAMING
                        ),
                        cost = "$15.99",
                        period = SubscriptionPeriod.MONTHLY,
                        nextPaymentDate = LocalDate.now().plusDays(12),
                        currentPaymentDate = LocalDate.now().minusDays(18)
                    )
                )
            }
        }
    }
}
