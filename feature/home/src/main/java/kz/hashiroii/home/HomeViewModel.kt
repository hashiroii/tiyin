package kz.hashiroii.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val calculateTotalCostUseCase: CalculateTotalCostUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getCurrencyUseCase()
        .flatMapLatest { currency ->
            combine(
                getSubscriptionsUseCase(),
                calculateTotalCostUseCase(currency)
            ) { subscriptions, totalCostResult ->
                val activeCount = subscriptions.size
                
                HomeUiState.Success(
                    subscriptions = subscriptions,
                    activeSubscriptionsCount = activeCount,
                    totalCost = totalCostResult.total,
                    totalCostCurrency = totalCostResult.targetCurrency
                ) as HomeUiState
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
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadSubscriptions -> {
            }
            is HomeIntent.RefreshSubscriptions -> refreshSubscriptions()
        }
    }

    private fun refreshSubscriptions() {
        viewModelScope.launch {
            try {
                refreshSubscriptionsUseCase()
            } catch (e: Exception) {
            }
        }
    }
}
