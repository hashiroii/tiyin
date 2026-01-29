package kz.hashiroii.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kz.hashiroii.designsystem.TiyinChip
import kz.hashiroii.designsystem.theme.TiyinTheme
import kz.hashiroii.domain.model.service.ServiceInfo
import kz.hashiroii.domain.model.service.ServiceType
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import kz.hashiroii.ui.R
import kz.hashiroii.ui.ServiceLogo
import kz.hashiroii.ui.util.CurrencyFormatter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    logoUrl: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
                    logoUrl = logoUrl,
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
                            text = getPeriodLabel(subscription.period),
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
                            stringResource(R.string.subscription_days_left, daysLeft)
                        } else {
                            stringResource(R.string.subscription_due_today)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ContinuousProgressIndicator(
                        progress = subscription.progressPercentage(today),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.subscription_next_payment,
                        subscription.nextPaymentDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TiyinChip(
                    text = getServiceTypeLabel(subscription.serviceInfo.serviceType)
                )
            }
        }
    }
}

@Composable
private fun getServiceTypeLabel(serviceType: ServiceType): String {
    return when (serviceType) {
        ServiceType.STREAMING -> stringResource(R.string.service_type_streaming)
        ServiceType.AUDIOBOOK -> stringResource(R.string.service_type_audiobook)
        ServiceType.SOFTWARE -> stringResource(R.string.service_type_software)
        ServiceType.NEWS -> stringResource(R.string.service_type_news)
        ServiceType.FITNESS -> stringResource(R.string.service_type_fitness)
        ServiceType.EDUCATION -> stringResource(R.string.service_type_education)
        ServiceType.GAMING -> stringResource(R.string.service_type_gaming)
        ServiceType.CLOUD_STORAGE -> stringResource(R.string.service_type_cloud_storage)
        ServiceType.PRODUCTIVITY -> stringResource(R.string.service_type_productivity)
        ServiceType.OTHER -> stringResource(R.string.service_type_other)
    }
}

@Composable
private fun getPeriodLabel(period: SubscriptionPeriod): String {
    return when (period) {
        SubscriptionPeriod.MONTHLY -> stringResource(R.string.period_monthly)
        SubscriptionPeriod.YEARLY -> stringResource(R.string.period_yearly)
        SubscriptionPeriod.WEEKLY -> stringResource(R.string.period_weekly)
        SubscriptionPeriod.DAILY -> stringResource(R.string.period_daily)
        SubscriptionPeriod.QUARTERLY -> stringResource(R.string.period_quarterly)
    }
}

@Composable
private fun ContinuousProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4CAF50),
    trackColor: Color
) {
    Box(
        modifier = modifier
            .background(trackColor, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(color, RoundedCornerShape(4.dp))
        )
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
                            domain = "spotify.com",
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
                    ),
                    logoUrl = null
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
                            domain = "netflix.com",
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
                    ),
                    logoUrl = null
                )
            }
        }
    }
}
