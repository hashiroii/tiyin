package kz.hashiroii.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kz.hashiroii.designsystem.theme.TiyinTheme
import kz.hashiroii.domain.model.service.ServiceInfo
import kz.hashiroii.domain.model.service.ServiceType
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import kz.hashiroii.ui.ActiveSubscriptionsRow
import kz.hashiroii.ui.CachedStringProvider
import kz.hashiroii.ui.rememberCachedStringProvider
import kz.hashiroii.ui.SubscriptionCard
import kz.hashiroii.ui.TotalSpendingCard
import kz.hashiroii.ui.UiText
import kz.hashiroii.ui.util.CurrencyFormatter
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun HomeScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    HomeScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
            
            is HomeUiState.Success -> {
                val stringProvider = rememberCachedStringProvider()
                
                val context = LocalContext.current
                val packageName = context.packageName
                
                val totalSpendingTitle = remember {
                    stringProvider.getString(
                        resourceName = "total_spending",
                        packageName = packageName,
                        default = "Total Spending"
                    )
                }
                val activeSubscriptionsLabel = remember {
                    stringProvider.getString(
                        resourceName = "active_subscriptions",
                        packageName = packageName,
                        default = "Active Subscriptions"
                    )
                }
                
                val spendingAmount = CurrencyFormatter.format(
                    uiState.totalCost,
                    uiState.totalCostCurrency
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        TotalSpendingCard(
                            title = totalSpendingTitle,
                            amount = spendingAmount,
                            currency = uiState.totalCostCurrency
                        )
                    }
                    
                    item {
                        ActiveSubscriptionsRow(
                            count = uiState.activeSubscriptionsCount,
                            label = activeSubscriptionsLabel,
                            onSortClick = {
                                // TODO: Implement sort functionality
                            }
                        )
                    }
                    
                    items(
                        items = uiState.subscriptions,
                        key = { it.serviceInfo.name }
                    ) { subscription ->
                        SubscriptionCard(subscription = subscription)
                    }
                }
            }
            
            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.message.asString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Preview(name = "Loading - Light", showBackground = true)
@Composable
private fun HomeScreenLoadingLightPreview() {
    TiyinTheme(themePreference = "Light") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                uiState = HomeUiState.Loading,
                onIntent = {}
            )
        }
    }
}

@Preview(name = "Loading - Dark", showBackground = true)
@Composable
private fun HomeScreenLoadingDarkPreview() {
    TiyinTheme(themePreference = "Dark") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                uiState = HomeUiState.Loading,
                onIntent = {}
            )
        }
    }
}

@Preview(name = "Success - Light", showBackground = true)
@Composable
private fun HomeScreenSuccessLightPreview() {
    TiyinTheme(themePreference = "Light") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                uiState = HomeUiState.Success(
                    subscriptions = listOf(
                        Subscription(
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
                        ),
                        Subscription(
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
                    ),
                    activeSubscriptionsCount = 2,
                    totalCost = 12470.40,
                    totalCostCurrency = "KZT"
                ),
                onIntent = {}
            )
        }
    }
}

@Preview(name = "Success - Dark", showBackground = true)
@Composable
private fun HomeScreenSuccessDarkPreview() {
    TiyinTheme(themePreference = "Dark") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                uiState = HomeUiState.Success(
                    subscriptions = listOf(
                        Subscription(
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
                    ),
                    activeSubscriptionsCount = 1,
                    totalCost = 4795.20,
                    totalCostCurrency = "KZT"
                ),
                onIntent = {}
            )
        }
    }
}

@Preview(name = "Error - Light", showBackground = true)
@Composable
private fun HomeScreenErrorLightPreview() {
    TiyinTheme(themePreference = "Light") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                uiState = HomeUiState.Error(
                    message = UiText.DynamicString("Failed to load subscriptions")
                ),
                onIntent = {}
            )
        }
    }
}

@Preview(name = "Error - Dark", showBackground = true)
@Composable
private fun HomeScreenErrorDarkPreview() {
    TiyinTheme(themePreference = "Dark") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                uiState = HomeUiState.Error(
                    message = UiText.DynamicString("Failed to load subscriptions")
                ),
                onIntent = {}
            )
        }
    }
}
