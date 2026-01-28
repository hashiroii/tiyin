package kz.hashiroii.subscriptionmanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.hashiroii.designsystem.theme.TiyinTheme
import kz.hashiroii.domain.model.service.ServiceInfo
import kz.hashiroii.domain.model.service.ServiceType
import kz.hashiroii.domain.model.service.SubscriptionPeriod
import kz.hashiroii.domain.usecase.subscription.ServiceSearchResult
import kz.hashiroii.ui.R as UiR
import kz.hashiroii.ui.ServiceLogo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionManagerScreenRoute(
    subscriptionId: String? = null,
    serviceName: String? = null,
    serviceDomain: String? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionManagerViewModel = hiltViewModel(),
    onDeleteSubscriptionReady: ((() -> Unit) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is SubscriptionManagerUiState.Editing && currentState.isEditMode) {
            onDeleteSubscriptionReady?.invoke {
                viewModel.onIntent(SubscriptionManagerIntent.DeleteSubscription)
            }
        }
    }
    
    LaunchedEffect(subscriptionId, serviceName, serviceDomain) {
        if (serviceName != null && serviceDomain != null) {
            // Load subscription for editing
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val subscription = viewModel.getSubscriptionForEdit(serviceName, serviceDomain)
                subscription?.let { viewModel.initializeForEdit(it) }
            }
        }
    }
    
    LaunchedEffect(uiState) {
        if (uiState is SubscriptionManagerUiState.Success) {
            onBackClick()
        }
    }
    
    SubscriptionManagerScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun SubscriptionManagerScreen(
    uiState: SubscriptionManagerUiState,
    onIntent: (SubscriptionManagerIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when (val state = uiState) {
            is SubscriptionManagerUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is SubscriptionManagerUiState.Editing -> {
                SubscriptionManagerContent(
                    state = state,
                    onIntent = onIntent,
                    onBackClick = onBackClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            is SubscriptionManagerUiState.Success -> {
                // Navigation handled by LaunchedEffect
            }
            
            is SubscriptionManagerUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionManagerContent(
    state: SubscriptionManagerUiState.Editing,
    onIntent: (SubscriptionManagerIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showPeriodDialog by remember { mutableStateOf(false) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    val isAmountValid = remember(state.amount) {
        if (state.amount.isBlank()) return@remember false
        val trimmed = state.amount.trim()
        val regex = Regex("^\\d+(\\.\\d+)?$")
        if (!trimmed.matches(regex)) return@remember false
        try {
            val value = trimmed.toDouble()
            value > 0
        } catch (e: Exception) {
            false
        }
    }
    
    val isAmountInvalid = remember(state.amount) {
        state.amount.isNotBlank() && !isAmountValid
    }
    
    val isCurrencyValid = remember(state.currency) {
        state.currency.isNotBlank()
    }
    
    val isStartDateValid = remember(state.startDate) {
        state.startDate != null
    }
    
    val isEndDateValid = remember(state.endDate, state.startDate) {
        state.endDate != null && (state.startDate == null || !state.endDate.isBefore(state.startDate))
    }
    
    val isPeriodValid = remember(state.period) {
        true
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Service Search or Selected Service
        if (state.selectedService == null) {
            OutlinedTextField(
                value = state.serviceSearchQuery,
                onValueChange = { onIntent(SubscriptionManagerIntent.UpdateServiceSearchQuery(it)) },
                label = { Text(stringResource(R.string.subscription_manager_service_search)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )
            
            // Service Search Result
            state.serviceSearchResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onIntent(SubscriptionManagerIntent.SelectService(result)) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (result.logoUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(result.logoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = result.serviceInfo.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result.serviceInfo.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = result.serviceInfo.domain,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // Selected Service - replaces search field
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceLogo(
                        serviceInfo = state.selectedService.serviceInfo,
                        logoUrl = state.selectedService.logoUrl,
                        size = 48.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.selectedService.serviceInfo.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = state.selectedService.serviceInfo.domain,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onIntent(SubscriptionManagerIntent.ClearSelectedService) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.subscription_manager_clear_service)
                        )
                    }
                }
            }
        }
        
        // Amount and Currency Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.amount,
                onValueChange = { onIntent(SubscriptionManagerIntent.UpdateAmount(it)) },
                label = { Text(stringResource(R.string.subscription_manager_amount)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null
                    )
                },
                supportingText = if (isAmountInvalid) {
                    { Text(context.getString(R.string.please_enter_a_valid_positive_number), color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = when {
                        isAmountInvalid -> MaterialTheme.colorScheme.error
                        isAmountValid -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.outline
                    },
                    unfocusedBorderColor = when {
                        isAmountInvalid -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        isAmountValid -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.outline
                    },
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    errorSupportingTextColor = MaterialTheme.colorScheme.error
                ),
                isError = isAmountInvalid,
                modifier = Modifier.weight(2f),
                enabled = !state.isLoading
            )
            
            OutlinedTextField(
                value = state.currency,
                onValueChange = { },
                label = { Text(stringResource(R.string.subscription_manager_currency)) },
                leadingIcon = {
                    Icon(   
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline, // Correct way
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !state.isLoading) {
                        showCurrencyDialog = true
                        focusManager.clearFocus(true) },
                readOnly = true,
                enabled = false
            )
        }
        
        // Period
        val periodDisplayName = when (state.period) {
            SubscriptionPeriod.MONTHLY -> stringResource(UiR.string.period_monthly)
            SubscriptionPeriod.YEARLY -> stringResource(UiR.string.period_yearly)
            SubscriptionPeriod.WEEKLY -> stringResource(UiR.string.period_weekly)
            SubscriptionPeriod.DAILY -> stringResource(UiR.string.period_daily)
            SubscriptionPeriod.QUARTERLY -> stringResource(UiR.string.period_quarterly)
        }
        OutlinedTextField(
            value = if (state.isEditMode || periodDisplayName.isNotEmpty()) periodDisplayName else "",
            onValueChange = { },
            label = { Text(stringResource(R.string.subscription_manager_period)) },
            placeholder = if (!state.isEditMode) {
                { Text(stringResource(R.string.subscription_manager_period), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline, // Use theme outline
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !state.isLoading) {
                    showPeriodDialog = true
                    focusManager.clearFocus(true) },
            readOnly = true,
            enabled = false
        )
        
        // Start Date
        OutlinedTextField(
            value = state.startDate?.format(dateFormatter) ?: "",
            onValueChange = { },
            label = { Text(stringResource(R.string.subscription_manager_start_date)) },
            placeholder = if (state.startDate == null && !state.isEditMode) {
                { Text(stringResource(R.string.subscription_manager_start_date), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline, 
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !state.isLoading
                ) {
                    focusManager.clearFocus(true)
                    showStartDatePicker = true
                  },
            readOnly = true,
            enabled = false
        )
        
        // End Date (read-only, auto-calculated) - Info Card
        if (state.endDate != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.subscription_manager_end_date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.endDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // Error Message
        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Save Button
        Button(
            onClick = { onIntent(SubscriptionManagerIntent.SaveSubscription) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && state.selectedService != null
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
            }
            Text(
                text = if (state.isEditMode) {
                    stringResource(R.string.subscription_manager_save)
                } else {
                    stringResource(R.string.subscription_manager_add)
                }
            )
        }
    }
    
    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = state.startDate ?: LocalDate.now(),
            onDateSelected = { date ->
                onIntent(SubscriptionManagerIntent.UpdateStartDate(date))
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    
    // Currency Dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = state.currency,
            onCurrencySelected = { currency ->
                onIntent(SubscriptionManagerIntent.UpdateCurrency(currency))
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
    
    // Period Dialog
    if (showPeriodDialog) {
        PeriodSelectionDialog(
            currentPeriod = state.period,
            onPeriodSelected = { period ->
                onIntent(SubscriptionManagerIntent.UpdatePeriod(period))
                showPeriodDialog = false
            },
            onDismiss = { showPeriodDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text(stringResource(R.string.subscription_manager_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.subscription_manager_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun CurrencySelectionDialog(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currencies = listOf("USD", "KZT", "RUB", "EUR", "GBP")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.subscription_manager_select_currency)) },
        text = {
            Column {
                currencies.forEach { currency ->
                    TextButton(
                        onClick = { onCurrencySelected(currency) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(currency)
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.subscription_manager_cancel))
            }
        }
    )
}

@Composable
private fun PeriodSelectionDialog(
    currentPeriod: SubscriptionPeriod,
    onPeriodSelected: (SubscriptionPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    val periods = SubscriptionPeriod.values()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.subscription_manager_select_period)) },
        text = {
            Column {
                periods.forEach { period ->
                    val periodName = when (period) {
                        SubscriptionPeriod.MONTHLY -> stringResource(UiR.string.period_monthly)
                        SubscriptionPeriod.YEARLY -> stringResource(UiR.string.period_yearly)
                        SubscriptionPeriod.WEEKLY -> stringResource(UiR.string.period_weekly)
                        SubscriptionPeriod.DAILY -> stringResource(UiR.string.period_daily)
                        SubscriptionPeriod.QUARTERLY -> stringResource(UiR.string.period_quarterly)
                    }
                    TextButton(
                        onClick = { onPeriodSelected(period) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(periodName)
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.subscription_manager_cancel))
            }
        }
    )
}

@Preview(name = "Adding Mode - Light", showBackground = true)
@Composable
private fun SubscriptionManagerScreenAddingLightPreview() {
    TiyinTheme(themePreference = "Light") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            SubscriptionManagerScreen(
                uiState = SubscriptionManagerUiState.Editing(
                    isEditMode = false,
                    originalSubscription = null,
                    serviceSearchQuery = "",
                    selectedService = null,
                    amount = "",
                    currency = "USD",
                    startDate = null,
                    endDate = null,
                    period = SubscriptionPeriod.MONTHLY
                ),
                onIntent = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "Adding Mode with Selected Service - Light", showBackground = true)
@Composable
private fun SubscriptionManagerScreenAddingWithServiceLightPreview() {
    TiyinTheme(themePreference = "Light") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            SubscriptionManagerScreen(
                uiState = SubscriptionManagerUiState.Editing(
                    isEditMode = false,
                    originalSubscription = null,
                    serviceSearchQuery = "",
                    selectedService = ServiceSearchResult(
                        serviceInfo = ServiceInfo(
                            name = "Spotify",
                            domain = "spotify.com",
                            logoResId = 0,
                            primaryColor = 0xFF1DB954,
                            secondaryColor = 0xFF191414,
                            serviceType = ServiceType.STREAMING
                        ),
                        logoUrl = null
                    ),
                    amount = "9.99",
                    currency = "USD",
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusMonths(1),
                    period = SubscriptionPeriod.MONTHLY
                ),
                onIntent = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "Editing Mode - Light", showBackground = true)
@Composable
private fun SubscriptionManagerScreenEditingLightPreview() {
    TiyinTheme(themePreference = "Light") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            SubscriptionManagerScreen(
                uiState = SubscriptionManagerUiState.Editing(
                    isEditMode = true,
                    originalSubscription = null,
                    serviceSearchQuery = "",
                    selectedService = ServiceSearchResult(
                        serviceInfo = ServiceInfo(
                            name = "Netflix",
                            domain = "netflix.com",
                            logoResId = 0,
                            primaryColor = 0xFFE50914,
                            secondaryColor = 0xFF000000,
                            serviceType = ServiceType.STREAMING
                        ),
                        logoUrl = null
                    ),
                    amount = "15.99",
                    currency = "USD",
                    startDate = LocalDate.now().minusMonths(2),
                    endDate = LocalDate.now().plusMonths(1),
                    period = SubscriptionPeriod.MONTHLY
                ),
                onIntent = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "Adding Mode - Dark", showBackground = true)
@Composable
private fun SubscriptionManagerScreenAddingDarkPreview() {
    TiyinTheme(themePreference = "Dark") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            SubscriptionManagerScreen(
                uiState = SubscriptionManagerUiState.Editing(
                    isEditMode = false,
                    originalSubscription = null,
                    serviceSearchQuery = "",
                    selectedService = ServiceSearchResult(
                        serviceInfo = ServiceInfo(
                            name = "Apple Music",
                            domain = "apple.com",
                            logoResId = 0,
                            primaryColor = 0xFFFA243C,
                            secondaryColor = 0xFFFFFFFF,
                            serviceType = ServiceType.STREAMING
                        ),
                        logoUrl = null
                    ),
                    amount = "10.99",
                    currency = "USD",
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusMonths(1),
                    period = SubscriptionPeriod.MONTHLY
                ),
                onIntent = {},
                onBackClick = {}
            )
        }
    }
}

