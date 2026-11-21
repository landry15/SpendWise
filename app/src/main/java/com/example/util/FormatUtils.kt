package com.example.util

import com.example.data.preferences.CurrencyOption
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val frenchLocale = Locale.FRANCE

    fun formatMoney(amount: Double, currency: CurrencyOption): String {
        val formatter = NumberFormat.getNumberInstance(frenchLocale).apply {
            minimumFractionDigits = if (currency.decimals == 0) 0 else 2
            maximumFractionDigits = currency.decimals
        }
        return "${formatter.format(amount)} ${currency.symbol}"
    }

    fun formatDateShort(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", frenchLocale)
        return sdf.format(Date(millis))
    }

    fun formatDateNumeric(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", frenchLocale)
        return sdf.format(Date(millis))
    }

    fun formatMonthYear(millis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("MMMM yyyy", frenchLocale)
        val raw = sdf.format(Date(millis))
        return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(frenchLocale) else it.toString() }
    }

    fun startOfDayMillis(referenceMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun endOfDayMillis(referenceMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun startOfWeekMillis(referenceMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance(frenchLocale).apply {
            timeInMillis = referenceMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun startOfMonthMillis(referenceMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun daysAgoMillis(days: Int, referenceMillis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
