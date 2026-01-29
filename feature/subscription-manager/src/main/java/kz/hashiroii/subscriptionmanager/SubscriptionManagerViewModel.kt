package kz.hashiroii.subscriptionmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import kz.hashiroii.domain.usecase.preferences.GetCurrencyUseCase
import kz.hashiroii.domain.usecase.logo.GetLogoUrlUseCase
import kz.hashiroii.domain.usecase.subscription.AddSubscriptionUseCase
import kz.hashiroii.domain.usecase.subscription.DeleteSubscriptionUseCase
import kz.hashiroii.domain.usecase.subscription.GetSubscriptionsUseCase
import kz.hashiroii.domain.usecase.subscription.SearchServiceUseCase
import kz.hashiroii.domain.usecase.subscription.UpdateSubscriptionUseCase
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class SubscriptionManagerViewModel @Inject constructor(
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val updateSubscriptionUseCase: UpdateSubscriptionUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
    private val searchServiceUseCase: SearchServiceUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    private val getLogoUrlUseCase: GetLogoUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubscriptionManagerUiState>(
        SubscriptionManagerUiState.Editing(
            isEditMode = false,
            originalSubscription = null,
            currency = "USD"
        )
    )
    val uiState: StateFlow<SubscriptionManagerUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val debounceDelayMs = 500L

    init {
        loadInitialCurrency()
    }

    private fun loadInitialCurrency() {
        viewModelScope.launch {
            getCurrencyUseCase().collect { currency ->
                val state = _uiState.value
                if (state is SubscriptionManagerUiState.Editing && state.currency != currency) {
                    _uiState.value = state.copy(currency = currency)
                }
            }
        }
    }

    fun initializeForEdit(subscription: Subscription) {
        viewModelScope.launch {
            var logoUrl: String? = null
            getLogoUrlUseCase(subscription.serviceInfo.domain)
                .collect { url ->
                    logoUrl = url
                    _uiState.value = SubscriptionManagerUiState.Editing(
                        isEditMode = true,
                        originalSubscription = subscription,
                        selectedService = kz.hashiroii.domain.usecase.subscription.ServiceSearchResult(
                            serviceInfo = subscription.serviceInfo,
                            logoUrl = logoUrl
                        ),
                        amount = subscription.amount.toString(),
                        currency = subscription.currency,
                        startDate = subscription.currentPaymentDate,
                        endDate = subscription.nextPaymentDate,
                        period = subscription.period
                    )
                    return@collect
                }
        }
    }
    
    suspend fun getSubscriptionForEdit(serviceName: String, serviceDomain: String): Subscription? {
        return getSubscriptionsUseCase()
            .first()
            .firstOrNull { 
                it.serviceInfo.name == serviceName && it.serviceInfo.domain == serviceDomain 
            }
    }

    fun onIntent(intent: SubscriptionManagerIntent) {
        when (val state = _uiState.value) {
            is SubscriptionManagerUiState.Editing -> {
                when (intent) {
                    is SubscriptionManagerIntent.UpdateServiceSearchQuery -> {
                        updateServiceSearch(state, intent.query)
                    }
                    is SubscriptionManagerIntent.SelectService -> {
                        _uiState.value = state.copy(
                            selectedService = intent.serviceResult,
                            serviceSearchQuery = intent.serviceResult.serviceInfo.name,
                            serviceSearchResult = null
                        )
                    }
                    is SubscriptionManagerIntent.UpdateAmount -> {
                        _uiState.value = state.copy(amount = intent.amount)
                    }
                    is SubscriptionManagerIntent.UpdateCurrency -> {
                        _uiState.value = state.copy(currency = intent.currency)
                    }
                    is SubscriptionManagerIntent.UpdateStartDate -> {
                        val calculatedEndDate = calculateEndDate(intent.date, state.period)
                        _uiState.value = state.copy(
                            startDate = intent.date,
                            endDate = calculatedEndDate
                        )
                    }
                    is SubscriptionManagerIntent.UpdateEndDate -> {
                        _uiState.value = state.copy(endDate = intent.date)
                    }
                    is SubscriptionManagerIntent.UpdatePeriod -> {
                        val calculatedEndDate = state.startDate?.let { 
                            calculateEndDate(it, intent.period) 
                        }
                        _uiState.value = state.copy(
                            period = intent.period,
                            endDate = calculatedEndDate ?: state.endDate
                        )
                    }
                    is SubscriptionManagerIntent.SaveSubscription -> {
                        saveSubscription(state)
                    }
                    is SubscriptionManagerIntent.DeleteSubscription -> {
                        deleteSubscription(state)
                    }
                    is SubscriptionManagerIntent.ClearSelectedService -> {
                        _uiState.value = state.copy(
                            selectedService = null,
                            serviceSearchQuery = "",
                            serviceSearchResult = null
                        )
                    }
                    is SubscriptionManagerIntent.Cancel -> {
                        // Handled by navigation
                    }
                    is SubscriptionManagerIntent.LoadSubscription -> {
                        // Already loaded
                    }
                }
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    private fun updateServiceSearch(state: SubscriptionManagerUiState.Editing, query: String) {
        _uiState.value = state.copy(
            serviceSearchQuery = query,
            serviceSearchResult = null,
            isLoading = false
        )
        
        searchJob?.cancel()
        
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(debounceDelayMs)
                
                _uiState.value = state.copy(
                    serviceSearchQuery = query,
                    isLoading = true
                )
                
                searchServiceUseCase(query)
                    .onEach { result ->
                        _uiState.value = SubscriptionManagerUiState.Editing(
                            isEditMode = state.isEditMode,
                            originalSubscription = state.originalSubscription,
                            serviceSearchQuery = query,
                            serviceSearchResult = result,
                            selectedService = state.selectedService,
                            amount = state.amount,
                            currency = state.currency,
                            startDate = state.startDate,
                            endDate = state.endDate,
                            period = state.period,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    .catch { e ->
                        _uiState.value = state.copy(
                            serviceSearchQuery = query,
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                    .launchIn(viewModelScope)
            }
        } else {
            _uiState.value = state.copy(
                serviceSearchQuery = query,
                serviceSearchResult = null,
                isLoading = false
            )
        }
    }

    private fun saveSubscription(state: SubscriptionManagerUiState.Editing) {
        val selectedService = state.selectedService ?: run {
            _uiState.value = state.copy(errorMessage = "Please select a service")
            return
        }

        val amount = try {
            BigDecimal(state.amount)
        } catch (e: Exception) {
            _uiState.value = state.copy(errorMessage = "Invalid amount")
            return
        }

        val startDate = state.startDate ?: run {
            _uiState.value = state.copy(errorMessage = "Please select start date")
            return
        }

        val endDate = state.endDate ?: run {
            _uiState.value = state.copy(errorMessage = "Please select end date")
            return
        }

        if (endDate.isBefore(startDate)) {
            _uiState.value = state.copy(errorMessage = "End date must be after start date")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val subscription = Subscription(
                serviceInfo = selectedService.serviceInfo,
                amount = amount,
                currency = state.currency,
                period = state.period,
                nextPaymentDate = endDate,
                currentPaymentDate = startDate
            )

            val result = if (state.isEditMode && state.originalSubscription != null) {
                updateSubscriptionUseCase(state.originalSubscription, subscription)
            } else {
                addSubscriptionUseCase(subscription)
            }

            _uiState.value = if (result.isSuccess) {
                SubscriptionManagerUiState.Success
            } else {
                state.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to save subscription"
                )
            }
        }
    }
    
    private fun deleteSubscription(state: SubscriptionManagerUiState.Editing) {
        val originalSubscription = state.originalSubscription ?: run {
            _uiState.value = state.copy(errorMessage = "No subscription to delete")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            
            val result = deleteSubscriptionUseCase(originalSubscription)
            
            _uiState.value = if (result.isSuccess) {
                SubscriptionManagerUiState.Success
            } else {
                state.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete subscription"
                )
            }
        }
    }
    
    private fun calculateEndDate(startDate: LocalDate, period: SubscriptionPeriod): LocalDate {
        return when (period) {
            SubscriptionPeriod.DAILY -> startDate.plusDays(1)
            SubscriptionPeriod.WEEKLY -> startDate.plusWeeks(1)
            SubscriptionPeriod.MONTHLY -> startDate.plusMonths(1)
            SubscriptionPeriod.QUARTERLY -> startDate.plusMonths(3)
            SubscriptionPeriod.YEARLY -> startDate.plusYears(1)
        }
    }
}
