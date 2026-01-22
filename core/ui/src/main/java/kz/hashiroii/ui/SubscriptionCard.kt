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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
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
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                if (!subscription.serviceInfo.logoUrl.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = subscription.serviceInfo.logoUrl,
                        contentDescription = subscription.serviceInfo.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = subscription.serviceInfo.name,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = subscription.serviceInfo.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        softWrap = true
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.widthIn(min = 80.dp)
                    ) {
                        Text(
                            text = subscription.cost,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subscription.period.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val daysLeft = subscription.daysUntilNextPayment()
                    Text(
                        text = if (daysLeft > 0) "$daysLeft days left" else "Due today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { subscription.progressPercentage() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                TiyinChip(
                    text = getServiceTypeLabel(subscription.serviceInfo.serviceType)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
