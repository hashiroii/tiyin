package kz.hashiroii.ui.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currencyCode: String): String {
        return format(BigDecimal.valueOf(amount), currencyCode)
    }
    
    fun format(amount: BigDecimal, currencyCode: String): String {
        val locale = getLocaleForCurrency(currencyCode)
        val currency = try {
            Currency.getInstance(currencyCode)
        } catch (e: Exception) {
            return formatWithSymbol(amount, currencyCode)
        }
        
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = currency
        return formatter.format(amount)
    }
    
    private fun formatWithSymbol(amount: BigDecimal, currencyCode: String): String {
        val symbol = when (currencyCode) {
            "KZT" -> "₸"
            "USD" -> "$"
            "EUR" -> "€"
            "RUB" -> "₽"
            "GBP" -> "£"
            else -> currencyCode
        }
        val formattedAmount = String.format("%.2f", amount.toDouble()).trimEnd('0').trimEnd('.')
        return "$symbol$formattedAmount"
    }
    
    private fun getLocaleForCurrency(currencyCode: String): Locale {
        return when (currencyCode) {
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "KZT" -> Locale("kz", "KZ")
            "RUB" -> Locale("ru", "RU")
            "GBP" -> Locale.UK
            else -> Locale.getDefault()
        }
    }
}
