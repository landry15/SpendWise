package com.example.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.CategoryBadge
import com.example.ui.components.CategoryIconMapper
import com.example.ui.components.DonutSlice
import com.example.ui.components.EmptyExpensesIllustration
import com.example.ui.components.SpendingDonutChart
import com.example.ui.theme.MonoAmountHeroStyle
import com.example.ui.theme.MonoAmountRowStyle
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onPeriodSelect: (DashboardPeriod) -> Unit,
    onCategorySelect: (Long?) -> Unit,
    onOpenBudgetDialog: () -> Unit,
    onDismissBudgetDialog: () -> Unit,
    onBudgetInputChange: (String) -> Unit,
    onSaveBudget: () -> Unit,
    onQuickAddExpense: () -> Unit,
    onSeeAllExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstName = uiState.userName.trim().substringBefore(" ").ifEmpty { "utilisateur" }
    val animatedBudgetProgress by animateFloatAsState(
        targetValue = uiState.budgetRatio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "budget_progress"
    )
    val isOverBudget = uiState.remainingBudget < 0.0

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onQuickAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("dashboard_add_fab")
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
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bonjour, $firstName",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Synthèse de ${FormatUtils.formatMonthYear()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Hors-ligne",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Hero Monthly Budget & Total Card
                val gradientBrush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(gradientBrush)
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Dépenses du mois",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = FormatUtils.formatMoney(uiState.monthlySpent, uiState.currency),
                                    style = MonoAmountHeroStyle,
                                    color = Color.White,
                                    modifier = Modifier.testTag("dashboard_monthly_spent")
                                )
                            }

                            IconButton(
                                onClick = onOpenBudgetDialog,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("edit_budget_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Configurer le budget mensuel",
                                    tint = Color.White
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { animatedBudgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp)
                                .clip(CircleShape),
                            color = if (isOverBudget) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                Color(0xFFF4C452)
                            },
                            trackColor = Color.White.copy(alpha = 0.24f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isOverBudget) "Dépassement" else "Budget restant",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.80f)
                                )
                                Text(
                                    text = FormatUtils.formatMoney(
                                        kotlin.math.abs(uiState.remainingBudget),
                                        uiState.currency
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverBudget) Color(0xFFFFDAD6) else Color(0xFFF4C452),
                                    modifier = Modifier.testTag("dashboard_remaining_budget")
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Budget mensuel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.80f)
                                )
                                Text(
                                    text = FormatUtils.formatMoney(uiState.monthlyBudget, uiState.currency),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Period Filter (Jour / Semaine / Mois)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    DashboardPeriod.entries.forEachIndexed { index, period ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = DashboardPeriod.entries.size
                            ),
                            selected = uiState.selectedPeriod == period,
                            onClick = { onPeriodSelect(period) },
                            modifier = Modifier.testTag("period_tab_${period.name.lowercase()}")
                        ) {
                            Text(period.label)
                        }
                    }
                }

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategoryId == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("Toutes catégories") },
                        modifier = Modifier.testTag("dashboard_cat_all")
                    )
                    uiState.categories.forEach { cat ->
                        FilterChip(
                            selected = uiState.selectedCategoryId == cat.id,
                            onClick = { onCategorySelect(cat.id) },
                            label = { Text(cat.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryIconMapper.iconForKey(cat.iconKey),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("dashboard_cat_${cat.id}")
                        )
                    }
                }

                // Category Breakdown & Donut Chart
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Répartition (${uiState.selectedPeriod.label})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = FormatUtils.formatMoney(uiState.periodTotal, uiState.currency),
                                style = MonoAmountRowStyle,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (uiState.breakdown.isEmpty()) {
                            EmptyExpensesIllustration(
                                title = "Aucune dépense sur cette période",
                                subtitle = "Ajoutez une dépense ou élargissez la période (Semaine / Mois) pour visualiser la répartition."
                            )
                        } else {
                            val slices = uiState.breakdown.map { item ->
                                DonutSlice(
                                    label = item.category.name,
                                    amount = item.totalAmount,
                                    color = CategoryIconMapper.colorFromHex(item.category.colorHex),
                                    percentage = item.percentage
                                )
                            }

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                SpendingDonutChart(
                                    slices = slices,
                                    centerTitle = FormatUtils.formatMoney(
                                        uiState.breakdown.sumOf { it.totalAmount },
                                        uiState.currency
                                    ),
                                    centerSubtitle = uiState.selectedPeriod.label
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                uiState.breakdown.forEach { item ->
                                    val barColor = CategoryIconMapper.colorFromHex(item.category.colorHex)
                                    val isSelected = uiState.selectedCategoryId == item.category.id
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .clickable { onCategorySelect(item.category.id) }
                                            .padding(vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(barColor)
                                                )
                                                Text(
                                                    text = item.category.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                            Text(
                                                text = "${FormatUtils.formatMoney(item.totalAmount, uiState.currency)} (${(item.percentage * 100).toInt()}%)",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { item.percentage.coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(7.dp)
                                                .clip(CircleShape),
                                            color = barColor,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Expenses for active filter
                if (uiState.recentExpenses.isNotEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dernières dépenses",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                TextButton(onClick = onSeeAllExpenses) {
                                    Text("Tout voir")
                                }
                            }

                            uiState.recentExpenses.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CategoryBadge(
                                        iconKey = item.category.iconKey,
                                        colorHex = item.category.colorHex,
                                        contentDescription = item.category.name,
                                        size = 40.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.expense.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.category.name} • ${FormatUtils.formatDateShort(item.expense.dateMillis)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "-${FormatUtils.formatMoney(item.expense.amount, uiState.currency)}",
                                        style = MonoAmountRowStyle,
                                        color = if (uiState.budgetRatio > 1f) {
                                            MaterialTheme.colorScheme.error
                                        } else if (uiState.budgetRatio > 0.8f) {
                                            WarningAmber
                                        } else {
                                            SuccessGreen
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isBudgetDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissBudgetDialog,
            title = { Text("Budget mensuel") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Définissez votre plafond mensuel de dépenses (${uiState.currency.symbol}).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = uiState.budgetInput,
                        onValueChange = onBudgetInputChange,
                        label = { Text("Montant du budget") },
                        isError = uiState.budgetError != null,
                        supportingText = uiState.budgetError?.let { err -> { Text(err) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onSaveBudget,
                    modifier = Modifier.testTag("budget_save_button")
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissBudgetDialog) {
                    Text("Annuler")
                }
            }
        )
    }
}
