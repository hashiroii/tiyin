package kz.hashiroii.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kz.hashiroii.designsystem.theme.TiyinTheme
import kz.hashiroii.domain.model.service.ServiceInfo
import kz.hashiroii.domain.model.service.ServiceType
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import kz.hashiroii.home.R
import kz.hashiroii.ui.ActiveSubscriptionsRow
import kz.hashiroii.ui.SubscriptionCard
import kz.hashiroii.ui.TotalSpendingCard
import kz.hashiroii.ui.UiText
import kz.hashiroii.ui.util.CurrencyFormatter
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun HomeScreenRoute(
    onAddSubscriptionClick: () -> Unit,
    onEditSubscriptionClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    HomeScreen(
        uiState = uiState,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onIntent(HomeIntent.RefreshSubscriptions) },
        onIntent = viewModel::onIntent,
        onAddSubscriptionClick = onAddSubscriptionClick,
        onEditSubscriptionClick = onEditSubscriptionClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onIntent: (HomeIntent) -> Unit,
    onAddSubscriptionClick: () -> Unit,
    onEditSubscriptionClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp) // Standard size
                    )
                }
            }
            
            is HomeUiState.Success -> {
                val spendingAmount = CurrencyFormatter.format(
                    uiState.totalCost,
                    uiState.totalCostCurrency
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
                        ) {
                        item {
                            TotalSpendingCard(
                                title = stringResource(R.string.total_spending),
                                amount = spendingAmount,
                                currency = uiState.totalCostCurrency
                            )
                        }
                        
                        item {
                            ActiveSubscriptionsRow(
                                count = uiState.activeSubscriptionsCount,
                                label = stringResource(R.string.active_subscriptions),
                                onSortClick = {},
                                trailingContent = {
                                    SortOrderDropdown(
                                        currentOrder = uiState.sortOrder,
                                        onOrderSelected = { onIntent(HomeIntent.SetSortOrder(it)) }
                                    )
                                }
                            )
                        }
                        
                        itemsIndexed(
                            items = uiState.subscriptions,
                            key = { index, sub -> sub.id ?: "sub-$index-${sub.serviceInfo.domain}-${sub.serviceInfo.name}" }
                        ) { _, subscription ->
                            SubscriptionCard(
                                subscription = subscription,
                                logoUrl = uiState.logoUrls[subscription.serviceInfo.domain],
                                onClick = {
                                    onEditSubscriptionClick(
                                        subscription.serviceInfo.name,
                                        subscription.serviceInfo.domain
                                    )
                                }
                            )
                        }
                        }
                    }

                    // Floating Action Button
                    FloatingActionButton(
                        onClick = onAddSubscriptionClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Subscription"
                        )
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

@Composable
private fun SortOrderDropdown(
    currentOrder: SubscriptionSortOrder,
    onOrderSelected: (SubscriptionSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = stringResource(R.string.sort_subscriptions),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_by_expiry_date)) },
                onClick = {
                    onOrderSelected(SubscriptionSortOrder.EXPIRY_DATE)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_by_cost)) },
                onClick = {
                    onOrderSelected(SubscriptionSortOrder.COST)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_by_alphabet)) },
                onClick = {
                    onOrderSelected(SubscriptionSortOrder.ALPHABET)
                    expanded = false
                }
            )
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
                onIntent = {},
                onAddSubscriptionClick = {},
                onEditSubscriptionClick = { _, _ -> }
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
                onIntent = {},
                onAddSubscriptionClick = {},
                onEditSubscriptionClick = { _, _ -> }
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
                        Subscription(
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
                        )
                    ),
                    activeSubscriptionsCount = 2,
                    totalCost = 12470.40,
                    totalCostCurrency = "KZT",
                    sortOrder = SubscriptionSortOrder.EXPIRY_DATE
                ),
                onIntent = {},
                onAddSubscriptionClick = {},
                onEditSubscriptionClick = { _, _ -> }
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
                        )
                    ),
                    activeSubscriptionsCount = 1,
                    totalCost = 4795.20,
                    totalCostCurrency = "KZT"
                ),
                onIntent = {},
                onAddSubscriptionClick = {},
                onEditSubscriptionClick = { _, _ -> }
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
                onIntent = {},
                onAddSubscriptionClick = {},
                onEditSubscriptionClick = { _, _ -> }
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
                onIntent = {},
                onAddSubscriptionClick = {},
                onEditSubscriptionClick = { _, _ -> }
            )
        }
    }
}
