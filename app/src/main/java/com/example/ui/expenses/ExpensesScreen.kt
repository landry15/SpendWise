package com.example.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseWithCategory
import com.example.data.preferences.CurrencyOption
import com.example.ui.components.CategoryBadge
import com.example.ui.components.CategoryIconMapper
import com.example.ui.components.EmptyExpensesIllustration
import com.example.ui.theme.MonoAmountRowStyle
import com.example.util.FormatUtils

@Composable
fun ExpensesScreen(
    uiState: ExpensesUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterSelect: (Long?) -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenEditExpense: (ExpenseWithCategory) -> Unit,
    onRequestDelete: (ExpenseWithCategory) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onDismissForm: () -> Unit,
    onFormAmountChange: (String) -> Unit,
    onFormDescriptionChange: (String) -> Unit,
    onFormCategorySelect: (Long) -> Unit,
    onFormDateChange: (Long) -> Unit,
    onSaveExpense: () -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            onClearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Ajouter une dépense"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Mes Dépenses",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            val totalFiltered = uiState.filteredExpenses.sumOf { it.expense.amount }
                            Text(
                                text = "${uiState.filteredExpenses.size} opération(s) • ${FormatUtils.formatMoney(totalFiltered, uiState.currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Rechercher une dépense ou catégorie...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Effacer la recherche")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expenses_search_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedCategoryFilterId == null,
                            onClick = { onCategoryFilterSelect(null) },
                            label = { Text("Toutes") },
                            modifier = Modifier.testTag("filter_category_all")
                        )
                        uiState.categories.forEach { category ->
                            FilterChip(
                                selected = uiState.selectedCategoryFilterId == category.id,
                                onClick = { onCategoryFilterSelect(category.id) },
                                label = { Text(category.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = CategoryIconMapper.iconForKey(category.iconKey),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("filter_category_${category.id}")
                            )
                        }
                    }
                }

                if (uiState.filteredExpenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            EmptyExpensesIllustration(
                                title = if (uiState.expenses.isEmpty()) {
                                    "Aucune dépense enregistrée"
                                } else {
                                    "Aucun résultat pour ce filtre"
                                },
                                subtitle = if (uiState.expenses.isEmpty()) {
                                    "Appuyez sur le bouton + pour enregistrer votre première dépense hors-ligne."
                                } else {
                                    "Essayez de modifier votre recherche ou de réinitialiser le filtre de catégorie."
                                }
                            )
                            if (uiState.expenses.isEmpty()) {
                                Button(
                                    onClick = onOpenAddExpense,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("empty_add_expense_button")
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text("Ajouter une dépense")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 4.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.filteredExpenses,
                            key = { it.expense.id }
                        ) { item ->
                            ExpenseListCard(
                                item = item,
                                currency = uiState.currency,
                                onEdit = { onOpenEditExpense(item) },
                                onDelete = { onRequestDelete(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.formState.isVisible) {
        AddEditExpenseDialog(
            formState = uiState.formState,
            categories = uiState.categories,
            currency = uiState.currency,
            onDismiss = onDismissForm,
            onAmountChange = onFormAmountChange,
            onDescriptionChange = onFormDescriptionChange,
            onCategorySelect = onFormCategorySelect,
            onDateChange = onFormDateChange,
            onSave = onSaveExpense
        )
    }

    uiState.pendingDeleteExpense?.let { target ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Supprimer cette dépense ?") },
            text = {
                Text(
                    "« ${target.expense.description} » (${FormatUtils.formatMoney(target.expense.amount, uiState.currency)}) sera définitivement supprimée."
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    modifier = Modifier.testTag("confirm_delete_expense_button")
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun ExpenseListCard(
    item: ExpenseWithCategory,
    currency: CurrencyOption,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("expense_card_${item.expense.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryBadge(
                iconKey = item.category.iconKey,
                colorHex = item.category.colorHex,
                contentDescription = item.category.name
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.expense.description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.category.name} • ${FormatUtils.formatDateShort(item.expense.dateMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "-${FormatUtils.formatMoney(item.expense.amount, currency)}",
                    style = MonoAmountRowStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("edit_expense_${item.expense.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Modifier la dépense",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_expense_${item.expense.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Supprimer la dépense",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    formState: ExpenseFormState,
    categories: List<CategoryEntity>,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelect: (Long) -> Unit,
    onDateChange: (Long) -> Unit,
    onSave: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (formState.editingExpenseId == null) {
                        "Nouvelle dépense"
                    } else {
                        "Modifier la dépense"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = formState.amountText,
                    onValueChange = onAmountChange,
                    label = { Text("Montant (${currency.symbol})") },
                    placeholder = { Text("Ex. 24.50") },
                    isError = formState.amountError != null,
                    supportingText = formState.amountError?.let { err -> { Text(err) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                OutlinedTextField(
                    value = formState.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    placeholder = { Text("Ex. Courses supermarché") },
                    isError = formState.descriptionError != null,
                    supportingText = formState.descriptionError?.let { err -> { Text(err) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_description_input")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Date de la dépense",
                        style = MaterialTheme.typography.labelLarge
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("expense_date_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(FormatUtils.formatDateNumeric(formState.dateMillis))
                    }
                    formState.dateError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Catégorie",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = formState.selectedCategoryId == cat.id,
                                onClick = { onCategorySelect(cat.id) },
                                label = { Text(cat.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = CategoryIconMapper.iconForKey(cat.iconKey),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("form_category_${cat.id}")
                            )
                        }
                    }
                    formState.categoryError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("expense_form_cancel")
                    ) {
                        Text("Annuler")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("expense_form_save")
                    ) {
                        Text(if (formState.editingExpenseId == null) "Enregistrer" else "Mettre à jour")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = formState.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selected ->
                            onDateChange(selected)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
