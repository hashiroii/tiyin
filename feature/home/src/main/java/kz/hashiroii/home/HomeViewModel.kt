package kz.hashiroii.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.LogoRepository
import kz.hashiroii.domain.usecase.auth.GetCurrentUserUseCase
import kz.hashiroii.domain.usecase.preferences.GetCurrencyUseCase
import kz.hashiroii.domain.usecase.subscription.CalculateTotalCostUseCase
import kz.hashiroii.domain.usecase.subscription.GetSubscriptionsUseCase
import kz.hashiroii.domain.usecase.subscription.RefreshSubscriptionsUseCase
import kz.hashiroii.ui.UiText
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    private val refreshSubscriptionsUseCase: RefreshSubscriptionsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val calculateTotalCostUseCase: CalculateTotalCostUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val logoRepository: LogoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow(SubscriptionSortOrder.EXPIRY_DATE)

    init {
        fetchOnFirstLaunch()
        observeSubscriptions()
    }

    private fun fetchOnFirstLaunch() {
        viewModelScope.launch {
            val cached = getSubscriptionsUseCase().first()
            if (cached.isEmpty() && getCurrentUserUseCase() != null) {
                refreshSubscriptionsUseCase()
            }
        }
    }

    private fun observeSubscriptions() {
        getCurrencyUseCase()
            .flatMapLatest { currency ->
                combine(
                    getSubscriptionsUseCase(),
                    calculateTotalCostUseCase(currency)
                ) { subscriptions, totalCostResult ->
                    subscriptions to totalCostResult
                }
            }
            .combine(_sortOrder) { (subscriptions, totalCostResult), order ->
                HomeUiState.Success(
                    subscriptions = subscriptions.sortedBy(order),
                    activeSubscriptionsCount = subscriptions.size,
                    totalCost = totalCostResult.total,
                    totalCostCurrency = totalCostResult.targetCurrency
                ) as HomeUiState
            }
            .catch { e ->
                emit(HomeUiState.Error(UiText.DynamicString(e.message ?: "Unknown error")))
            }
            .flowOn(Dispatchers.Default)
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.RefreshSubscriptions -> refreshSubscriptions()
            is HomeIntent.SetSortOrder -> _sortOrder.value = intent.order
        }
    }

    private fun refreshSubscriptions() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is HomeUiState.Success) {
                _uiState.value = current.copy(isRefreshing = true)
            }
            try {
                refreshSubscriptionsUseCase()
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    UiText.DynamicString(e.message ?: "Failed to refresh")
                )
            } finally {
                val updated = _uiState.value
                if (updated is HomeUiState.Success) {
                    _uiState.value = updated.copy(isRefreshing = false)
                }
            }
        }
    }

    fun getLogoUrl(domain: String): String? {
        return logoRepository.getLogoUrl(domain)
    }
}