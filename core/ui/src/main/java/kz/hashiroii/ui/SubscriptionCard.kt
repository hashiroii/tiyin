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
import androidx.compose.ui.platform.LocalContext
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
import kz.hashiroii.ui.CachedStringProvider
import kz.hashiroii.ui.ServiceLogo
import kz.hashiroii.ui.rememberCachedStringProvider
import kz.hashiroii.ui.util.CurrencyFormatter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val packageName = context.packageName
    val stringProvider = rememberCachedStringProvider()
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
                ServiceLogo(
                    serviceInfo = subscription.serviceInfo,
                    modifier = Modifier,
                    size = 40.dp
                )
                
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
                            text = CurrencyFormatter.format(
                                subscription.amount.toDouble(),
                                subscription.currency
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getPeriodLabel(stringProvider, subscription.period),
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                ) {
                    val today = java.time.LocalDate.now()
                    val daysLeft = subscription.daysUntilNextPayment(today)
                    Text(
                        text = if (daysLeft > 0) {
                            java.lang.String.format(
                                stringProvider.getString(
                                    resourceName = "subscription_days_left",
                                    packageName = packageName,
                                    default = "%1\$d days left"
                                ),
                                daysLeft
                            )
                        } else {
                            stringProvider.getString(
                                resourceName = "subscription_due_today",
                                packageName = packageName,
                                default = "Due today"
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { subscription.progressPercentage(today) },
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
                    text = getServiceTypeLabel(stringProvider, subscription.serviceInfo.serviceType)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = java.lang.String.format(
                    stringProvider.getString(
                        resourceName = "subscription_next_payment",
                        packageName = packageName,
                        default = "Next payment: %1\$s"
                    ),
                    subscription.nextPaymentDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getServiceTypeLabel(stringProvider: CachedStringProvider, serviceType: ServiceType): String {
    return when (serviceType) {
        ServiceType.STREAMING -> stringProvider.getString("service_type_streaming", "kz.hashiroii.tiyin", "Streaming")
        ServiceType.AUDIOBOOK -> stringProvider.getString("service_type_audiobook", "kz.hashiroii.tiyin", "Audiobook")
        ServiceType.SOFTWARE -> stringProvider.getString("service_type_software", "kz.hashiroii.tiyin", "Software")
        ServiceType.NEWS -> stringProvider.getString("service_type_news", "kz.hashiroii.tiyin", "News")
        ServiceType.FITNESS -> stringProvider.getString("service_type_fitness", "kz.hashiroii.tiyin", "Fitness")
        ServiceType.EDUCATION -> stringProvider.getString("service_type_education", "kz.hashiroii.tiyin", "Education")
        ServiceType.GAMING -> stringProvider.getString("service_type_gaming", "kz.hashiroii.tiyin", "Gaming")
        ServiceType.CLOUD_STORAGE -> stringProvider.getString("service_type_cloud_storage", "kz.hashiroii.tiyin", "Cloud Storage")
        ServiceType.PRODUCTIVITY -> stringProvider.getString("service_type_productivity", "kz.hashiroii.tiyin", "Productivity")
        ServiceType.OTHER -> stringProvider.getString("service_type_other", "kz.hashiroii.tiyin", "Other")
    }
}

private fun getPeriodLabel(stringProvider: CachedStringProvider, period: SubscriptionPeriod): String {
    return when (period) {
        SubscriptionPeriod.MONTHLY -> stringProvider.getString("period_monthly", "kz.hashiroii.tiyin", "Monthly")
        SubscriptionPeriod.YEARLY -> stringProvider.getString("period_yearly", "kz.hashiroii.tiyin", "Yearly")
        SubscriptionPeriod.WEEKLY -> stringProvider.getString("period_weekly", "kz.hashiroii.tiyin", "Weekly")
        SubscriptionPeriod.DAILY -> stringProvider.getString("period_daily", "kz.hashiroii.tiyin", "Daily")
        SubscriptionPeriod.QUARTERLY -> stringProvider.getString("period_quarterly", "kz.hashiroii.tiyin", "Quarterly")
    }
}

@Preview(name = "Light Theme", showBackground = true)
@Composable
private fun SubscriptionCardLightPreview() {
    TiyinTheme(themePreference = "Light") {
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
                        amount = BigDecimal("9.99"),
                        currency = "USD",
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
    TiyinTheme(themePreference = "Dark") {
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
                        amount = BigDecimal("15.99"),
                        currency = "USD",
                        period = SubscriptionPeriod.MONTHLY,
                        nextPaymentDate = LocalDate.now().plusDays(12),
                        currentPaymentDate = LocalDate.now().minusDays(18)
                    )
                )
            }
        }
    }
}
