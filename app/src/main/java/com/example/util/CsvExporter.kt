package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.ExpenseWithCategory
import com.example.data.preferences.CurrencyOption
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CsvExportResult(
    val file: File,
    val uri: Uri,
    val rowCount: Int
)

object CsvExporter {
    private const val UTF8_BOM = "\uFEFF"

    fun buildCsvContent(
        expenses: List<ExpenseWithCategory>,
        currency: CurrencyOption
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("ID,Date,Catégorie,Description,Montant,Devise\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        expenses.forEach { item ->
            val dateStr = dateFormat.format(Date(item.expense.dateMillis))
            val categoryStr = escapeCsvCell(item.category.name)
            val descStr = escapeCsvCell(item.expense.description)
            val amountStr = String.format(Locale.US, "%.2f", item.expense.amount)
            sb.append("${item.expense.id},$dateStr,$categoryStr,$descStr,$amountStr,${currency.code}\n")
        }
        return sb.toString()
    }

    fun exportToLocalCsv(
        context: Context,
        expenses: List<ExpenseWithCategory>,
        currency: CurrencyOption
    ): CsvExportResult {
        val exportDir = File(context.filesDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(exportDir, "spendwise_depenses_$timestamp.csv")
        val content = buildCsvContent(expenses, currency)
        file.writeText(content, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return CsvExportResult(file = file, uri = uri, rowCount = expenses.size)
    }

    fun createShareIntent(uri: Uri, fileName: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Export dépenses SpendWise ($fileName)")
            putExtra(Intent.EXTRA_TEXT, "Voici l'export CSV de mes dépenses personnelles SpendWise.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    internal fun escapeCsvCell(raw: String): String {
        val sanitized = if (raw.isNotEmpty() && raw[0] in charArrayOf('=', '+', '-', '@', '\t', '\r')) {
            "'$raw"
        } else {
            raw
        }
        val escaped = sanitized.replace("\"", "\"\"")
        return if (escaped.contains(',') || escaped.contains(';') || escaped.contains('"') || escaped.contains('\n')) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
