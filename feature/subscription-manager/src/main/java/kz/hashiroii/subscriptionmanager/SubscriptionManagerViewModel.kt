package kz.hashiroii.subscriptionmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kz.hashiroii.domain.model.service.ServiceInfo
import kz.hashiroii.domain.model.service.ServiceType
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import kz.hashiroii.domain.repository.LogoRepository
import kz.hashiroii.domain.usecase.preferences.GetCurrencyUseCase
import kz.hashiroii.domain.usecase.subscription.AddSubscriptionUseCase
import kz.hashiroii.domain.usecase.subscription.DeleteSubscriptionUseCase
import kz.hashiroii.domain.usecase.subscription.GetSubscriptionsUseCase
import kz.hashiroii.domain.usecase.subscription.SearchServiceUseCase
import kz.hashiroii.domain.usecase.subscription.ServiceSearchResult
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
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    private val getLogoRepository: LogoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubscriptionManagerUiState>(
        SubscriptionManagerUiState.Editing(
            isEditMode = false,
            originalSubscription = null,
            currency = "USD"
        )
    )
    val uiState: StateFlow<SubscriptionManagerUiState> = _uiState.asStateFlow()

    private val _events = Channel<SubscriptionManagerEvent>(Channel.BUFFERED)
    val events: Flow<SubscriptionManagerEvent> = _events.receiveAsFlow()

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

    fun loadForEdit(serviceName: String, domain: String) {
        viewModelScope.launch {
            val subscription = getSubscriptionsUseCase()
                .first()
                .firstOrNull {
                    it.serviceInfo.domain == domain && it.serviceInfo.name == serviceName
                } ?: return@launch

            _uiState.value = SubscriptionManagerUiState.Editing(
                isEditMode = true,
                originalSubscription = subscription,
                selectedService = ServiceSearchResult(
                    serviceInfo = subscription.serviceInfo,
                    logoUrl = getLogoRepository.getLogoUrl(domain)
                ),
                amount = subscription.amount.toString(),
                currency = subscription.currency,
                startDate = subscription.currentPaymentDate,
                endDate = subscription.nextPaymentDate,
                period = subscription.period
            )
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

                    SubscriptionManagerIntent.DismissDialog ->
                        _uiState.value = state.copy(activeDialog = null)
                    is SubscriptionManagerIntent.ShowDialog ->
                        _uiState.value = state.copy(activeDialog = intent.dialog)
                }
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    fun getLogoUrl(domain: String): String? = getLogoRepository.getLogoUrl(domain)

    private fun updateServiceSearch(state: SubscriptionManagerUiState.Editing, query: String) {
        _uiState.value = state.copy(
            serviceSearchQuery = query,
            serviceSearchResult = null,
            isLoading = false
        )
        
        searchJob?.cancel()

        if (query.isBlank()) return
        
        searchJob = viewModelScope.launch {
            delay(debounceDelayMs)

            val currentState = _uiState.value as? SubscriptionManagerUiState.Editing ?: return@launch
            _uiState.value = currentState.copy(isLoading = true)

            val trimmed = query.trim().lowercase()
            val domain = if (trimmed.contains(".")) {
                trimmed.removePrefix("http://").removePrefix("https://")
                    .removePrefix("www.").split("/")[0]
            } else {
                "$trimmed.com"
            }
            val serviceName = if (trimmed.contains(".")) {
                domain.split(".")[0].replaceFirstChar { it.uppercaseChar() }
            } else {
                trimmed.replaceFirstChar { it.uppercaseChar() }
            }

            val result = ServiceSearchResult(
                serviceInfo = ServiceInfo(
                    name = serviceName,
                    domain = domain,
                    primaryColor = 0xFF6200EE,
                    secondaryColor = 0xFF000000,
                    serviceType = ServiceType.OTHER
                ),
                logoUrl = getLogoRepository.getLogoUrl(domain)
            )

            val s = _uiState.value as? SubscriptionManagerUiState.Editing ?: return@launch
            _uiState.value = s.copy(
                serviceSearchQuery = query,
                serviceSearchResult = result,
                isLoading = false,
                errorMessage = null
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

            if (result.isSuccess) {
                _events.send(SubscriptionManagerEvent.NavigateBack)
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
            
            if (result.isSuccess) {
                _events.send(SubscriptionManagerEvent.NavigateBack)
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
