package com.example.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.CategoryEntity
import com.example.data.preferences.CurrencyOption
import com.example.data.preferences.ThemeMode
import com.example.ui.components.CategoryBadge
import com.example.ui.components.CategoryIconMapper
import com.example.util.CsvExporter

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSetCurrency: (CurrencyOption) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onOpenAddCategory: () -> Unit,
    onOpenEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onDismissCategoryForm: () -> Unit,
    onCategoryNameChange: (String) -> Unit,
    onCategoryIconSelect: (String) -> Unit,
    onCategoryColorSelect: (Long) -> Unit,
    onSaveCategory: () -> Unit,
    onExportCsv: (android.content.Context, (ExportBannerState) -> Unit) -> Unit,
    onOpenResetDialog: () -> Unit,
    onDismissResetDialog: () -> Unit,
    onConfirmReset: () -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Paramètres & Export",
                    style = MaterialTheme.typography.headlineMedium
                )

                // Export CSV Section
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Export des dépenses (CSV)",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${uiState.expenseCount} dépense(s) prête(s) à exporter localement",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onExportCsv(context) { exported ->
                                        val shareIntent = CsvExporter.createShareIntent(
                                            uri = exported.uri,
                                            fileName = exported.fileName
                                        )
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Partager l'export CSV")
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("export_and_share_csv_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Exporter & Partager")
                            }

                            OutlinedButton(
                                onClick = { onExportCsv(context) {} },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("export_local_csv_button")
                            ) {
                                Text("Sauver")
                            }
                        }

                        uiState.lastExport?.let { exportInfo ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exportInfo.fileName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${exportInfo.rowCount} lignes • Stocké hors-ligne",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val shareIntent = CsvExporter.createShareIntent(
                                                uri = exportInfo.uri,
                                                fileName = exportInfo.fileName
                                            )
                                            context.startActivity(
                                                Intent.createChooser(shareIntent, "Partager l'export CSV")
                                            )
                                        },
                                        modifier = Modifier.testTag("reshare_csv_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Share,
                                            contentDescription = "Partager le fichier CSV",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Theme & Currency Section
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Thème de l'application",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ThemeMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = ThemeMode.entries.size
                                    ),
                                    selected = uiState.themeMode == mode,
                                    onClick = { onSetThemeMode(mode) },
                                    modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}")
                                ) {
                                    Text(mode.label)
                                }
                            }
                        }

                        HorizontalDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Devise d'affichage",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CurrencyOption.entries.forEach { option ->
                                FilterChip(
                                    selected = uiState.currency == option,
                                    onClick = { onSetCurrency(option) },
                                    label = { Text(option.label) },
                                    modifier = Modifier.testTag("currency_${option.code.lowercase()}")
                                )
                            }
                        }
                    }
                }

                // Custom Categories Management Section
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
                            Column {
                                Text(
                                    text = "Catégories",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Ajoutez ou modifiez vos catégories personnalisées",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onOpenAddCategory,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("add_category_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Ajouter")
                            }
                        }

                        uiState.categories.forEachIndexed { index, category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CategoryBadge(
                                    iconKey = category.iconKey,
                                    colorHex = category.colorHex,
                                    contentDescription = category.name,
                                    size = 40.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (category.isDefault) "Par défaut" else "Personnalisée",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (!category.isDefault) {
                                    IconButton(
                                        onClick = { onOpenEditCategory(category) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("edit_category_${category.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "Modifier la catégorie",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteCategory(category) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("delete_category_${category.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Supprimer la catégorie",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            if (index < uiState.categories.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                // Reset Data Section
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Réinitialisation des données",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Supprime toutes vos dépenses enregistrées et vos catégories personnalisées tout en conservant votre compte.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        OutlinedButton(
                            onClick = onOpenResetDialog,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("reset_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Réinitialiser mes données")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (uiState.categoryForm.isVisible) {
        CategoryEditorDialog(
            form = uiState.categoryForm,
            onDismiss = onDismissCategoryForm,
            onNameChange = onCategoryNameChange,
            onIconSelect = onCategoryIconSelect,
            onColorSelect = onCategoryColorSelect,
            onSave = onSaveCategory
        )
    }

    if (uiState.isResetDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissResetDialog,
            title = { Text("Réinitialiser toutes vos données ?") },
            text = {
                Text("Cette action effacera irréversiblement toutes vos dépenses (${uiState.expenseCount}) et vos catégories personnalisées.")
            },
            confirmButton = {
                Button(
                    onClick = onConfirmReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_reset_data_button")
                ) {
                    Text("Tout effacer")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissResetDialog) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun CategoryEditorDialog(
    form: CategoryFormState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onIconSelect: (String) -> Unit,
    onColorSelect: (Long) -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (form.editingCategory == null) {
                        "Nouvelle catégorie"
                    } else {
                        "Modifier la catégorie"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text("Nom de la catégorie") },
                    placeholder = { Text("Ex. Abonnements") },
                    isError = form.nameError != null,
                    supportingText = form.nameError?.let { err -> { Text(err) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                Text(
                    text = "Icône",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIconMapper.availableIcons.forEach { option ->
                        FilterChip(
                            selected = form.iconKey == option.key,
                            onClick = { onIconSelect(option.key) },
                            label = { Text(option.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("cat_icon_${option.key}")
                        )
                    }
                }

                Text(
                    text = "Couleur",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryIconMapper.availableColors.forEach { hex ->
                        val isSelected = form.colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { onColorSelect(hex) }
                                .testTag("cat_color_$hex"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Couleur sélectionnée",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Annuler")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("category_save_button")
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}
