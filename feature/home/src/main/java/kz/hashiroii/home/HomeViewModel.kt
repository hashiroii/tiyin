package kz.hashiroii.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kz.hashiroii.domain.usecase.auth.GetCurrentUserUseCase
import kz.hashiroii.domain.usecase.logo.GetLogoUrlUseCase
import kz.hashiroii.domain.usecase.logo.PrefetchLogosUseCase
import kz.hashiroii.domain.usecase.preferences.GetCurrencyUseCase
import kz.hashiroii.domain.usecase.subscription.CalculateTotalCostUseCase
import kz.hashiroii.domain.usecase.subscription.GetSubscriptionsUseCase
import kz.hashiroii.domain.usecase.subscription.RefreshSubscriptionsUseCase
import kz.hashiroii.ui.UiText
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    private val refreshSubscriptionsUseCase: RefreshSubscriptionsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val calculateTotalCostUseCase: CalculateTotalCostUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getLogoUrlUseCase: GetLogoUrlUseCase,
    private val prefetchLogosUseCase: PrefetchLogosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _sortOrder = MutableStateFlow(SubscriptionSortOrder.EXPIRY_DATE)

    init {
        // When local cache is empty and user is signed in, fetch from Firestore once (e.g. after clear data).
        viewModelScope.launch {
            val subscriptions = getSubscriptionsUseCase().first()
            val user = getCurrentUserUseCase()
            if (subscriptions.isEmpty() && user != null) {
                refreshSubscriptionsUseCase()
            }
        }

        getCurrencyUseCase()
            .flatMapLatest { currency ->
                combine(
                    getSubscriptionsUseCase(),
                    calculateTotalCostUseCase(currency)
                ) { subscriptions, totalCostResult ->
                    subscriptions to totalCostResult
                }
            }
            .flatMapLatest { (subscriptions, totalCostResult) ->
                val activeCount = subscriptions.size
                val domains = subscriptions.map { it.serviceInfo.domain }.distinct()
                
                viewModelScope.launch {
                    prefetchLogosUseCase(domains)
                }
                
                if (domains.isEmpty()) {
                    kotlinx.coroutines.flow.flowOf(
                        HomeUiState.Success(
                            subscriptions = subscriptions,
                            activeSubscriptionsCount = activeCount,
                            totalCost = totalCostResult.total,
                            totalCostCurrency = totalCostResult.targetCurrency,
                            logoUrls = emptyMap()
                        ) as HomeUiState
                    )
                } else {
                    val logoUrlFlows: List<Flow<Pair<String, String?>>> = domains.map { domain: String ->
                        getLogoUrlUseCase(domain).map { logoUrl: String? ->
                            domain to logoUrl
                        }
                    }
                    
                    combine(logoUrlFlows) { logoUrlPairs: Array<Pair<String, String?>> ->
                        val logoUrlsMap = logoUrlPairs.toMap()
                        
                        HomeUiState.Success(
                            subscriptions = subscriptions,
                            activeSubscriptionsCount = activeCount,
                            totalCost = totalCostResult.total,
                            totalCostCurrency = totalCostResult.targetCurrency,
                            logoUrls = logoUrlsMap
                        ) as HomeUiState
                    }
                }
            }
            .combine(_sortOrder) { state, order ->
                when (state) {
                    is HomeUiState.Success -> state.copy(
                        sortOrder = order,
                        subscriptions = state.subscriptions.sortedBy(order)
                    )
                    else -> state
                }
            }
            .catch { e ->
                emit(
                    HomeUiState.Error(
                        message = UiText.DynamicString(
                            e.message ?: "Unknown error"
                        )
                    )
                )
            }
            .flowOn(Dispatchers.Default)
            .onEach { state ->
                withContext(Dispatchers.Main.immediate) {
                    _uiState.value = state
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadSubscriptions -> {
                // Already handled by the flow in init
            }
            is HomeIntent.RefreshSubscriptions -> refreshSubscriptions()
            is HomeIntent.SetSortOrder -> _sortOrder.value = intent.order
        }
    }

    private fun refreshSubscriptions() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                refreshSubscriptionsUseCase()
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = UiText.DynamicString(
                        e.message ?: "Failed to refresh subscriptions"
                    )
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
