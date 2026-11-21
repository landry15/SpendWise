package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.preferences.ThemeMode
import com.example.di.SpendWiseContainer
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.ProfileScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.expenses.ExpensesScreen
import com.example.ui.expenses.ExpensesViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.SpendWiseTheme

object SpendWiseRoutes {
    const val ROUTE_LOGIN = "login"
    const val ROUTE_REGISTER = "register"
    const val ROUTE_DASHBOARD = "dashboard"
    const val ROUTE_EXPENSES = "expenses"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_PROFILE = "profile"
}

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val mainDestinations = listOf(
    NavDestinationItem(
        route = SpendWiseRoutes.ROUTE_DASHBOARD,
        label = "Accueil",
        selectedIcon = Icons.Rounded.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_dashboard"
    ),
    NavDestinationItem(
        route = SpendWiseRoutes.ROUTE_EXPENSES,
        label = "Dépenses",
        selectedIcon = Icons.Rounded.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong,
        testTag = "nav_expenses"
    ),
    NavDestinationItem(
        route = SpendWiseRoutes.ROUTE_SETTINGS,
        label = "Paramètres",
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    ),
    NavDestinationItem(
        route = SpendWiseRoutes.ROUTE_PROFILE,
        label = "Profil",
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    )
)

@Composable
fun SpendWiseApp(container: SpendWiseContainer) {
    val themeMode by container.preferencesRepository.themeMode
        .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(container.authRepository)
    )
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    SpendWiseTheme(themeMode = themeMode) {
        if (!authUiState.isReady) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (currentUser == null) {
            val authNavController = rememberNavController()
            NavHost(
                navController = authNavController,
                startDestination = SpendWiseRoutes.ROUTE_LOGIN
            ) {
                composable(SpendWiseRoutes.ROUTE_LOGIN) {
                    LoginScreen(
                        state = authUiState,
                        onEmailChange = authViewModel::onEmailChange,
                        onPasswordChange = authViewModel::onPasswordChange,
                        onFillDemo = authViewModel::fillDemoCredentials,
                        onLoginClick = authViewModel::login,
                        onNavigateToRegister = {
                            authViewModel.clearErrors()
                            authNavController.navigate(SpendWiseRoutes.ROUTE_REGISTER)
                        }
                    )
                }
                composable(SpendWiseRoutes.ROUTE_REGISTER) {
                    RegisterScreen(
                        state = authUiState,
                        onNameChange = authViewModel::onNameChange,
                        onEmailChange = authViewModel::onEmailChange,
                        onPasswordChange = authViewModel::onPasswordChange,
                        onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
                        onRegisterClick = authViewModel::register,
                        onBackToLogin = {
                            authViewModel.clearErrors()
                            authNavController.popBackStack()
                        }
                    )
                }
            }
        } else {
            val activeUser = currentUser!!
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.factory(
                    authRepository = container.authRepository,
                    expenseRepository = container.expenseRepository,
                    categoryRepository = container.categoryRepository,
                    preferencesRepository = container.preferencesRepository
                )
            )
            val expensesViewModel: ExpensesViewModel = viewModel(
                factory = ExpensesViewModel.factory(
                    authRepository = container.authRepository,
                    expenseRepository = container.expenseRepository,
                    categoryRepository = container.categoryRepository,
                    preferencesRepository = container.preferencesRepository
                )
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(
                    authRepository = container.authRepository,
                    categoryRepository = container.categoryRepository,
                    expenseRepository = container.expenseRepository,
                    preferencesRepository = container.preferencesRepository
                )
            )

            val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
            val expensesState by expensesViewModel.uiState.collectAsStateWithLifecycle()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            val mainNavController = rememberNavController()
            val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            fun navigateToTab(route: String) {
                mainNavController.navigate(route) {
                    popUpTo(mainNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (!useNavigationRail) {
                            NavigationBar {
                                mainDestinations.forEach { dest ->
                                    val selected = currentDestination?.hierarchy?.any {
                                        it.route == dest.route
                                    } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateToTab(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) {
                                                    dest.selectedIcon
                                                } else {
                                                    dest.unselectedIcon
                                                },
                                                contentDescription = dest.label
                                            )
                                        },
                                        label = { Text(dest.label) },
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail) {
                            NavigationRail(
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                mainDestinations.forEach { dest ->
                                    val selected = currentDestination?.hierarchy?.any {
                                        it.route == dest.route
                                    } == true
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateToTab(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) {
                                                    dest.selectedIcon
                                                } else {
                                                    dest.unselectedIcon
                                                },
                                                contentDescription = dest.label
                                            )
                                        },
                                        label = { Text(dest.label) },
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }

                        NavHost(
                            navController = mainNavController,
                            startDestination = SpendWiseRoutes.ROUTE_DASHBOARD,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(SpendWiseRoutes.ROUTE_DASHBOARD) {
                                DashboardScreen(
                                    uiState = dashboardState,
                                    onPeriodSelect = dashboardViewModel::onPeriodSelect,
                                    onCategorySelect = dashboardViewModel::onCategorySelect,
                                    onOpenBudgetDialog = dashboardViewModel::openBudgetDialog,
                                    onDismissBudgetDialog = dashboardViewModel::dismissBudgetDialog,
                                    onBudgetInputChange = dashboardViewModel::onBudgetInputChange,
                                    onSaveBudget = dashboardViewModel::saveMonthlyBudget,
                                    onQuickAddExpense = {
                                        expensesViewModel.openAddExpense()
                                        navigateToTab(SpendWiseRoutes.ROUTE_EXPENSES)
                                    },
                                    onSeeAllExpenses = {
                                        navigateToTab(SpendWiseRoutes.ROUTE_EXPENSES)
                                    }
                                )
                            }

                            composable(SpendWiseRoutes.ROUTE_EXPENSES) {
                                ExpensesScreen(
                                    uiState = expensesState,
                                    onSearchQueryChange = expensesViewModel::onSearchQueryChange,
                                    onCategoryFilterSelect = expensesViewModel::onCategoryFilterSelect,
                                    onOpenAddExpense = expensesViewModel::openAddExpense,
                                    onOpenEditExpense = expensesViewModel::openEditExpense,
                                    onRequestDelete = expensesViewModel::requestDeleteExpense,
                                    onConfirmDelete = expensesViewModel::confirmDeleteExpense,
                                    onCancelDelete = expensesViewModel::cancelDeleteExpense,
                                    onDismissForm = expensesViewModel::dismissForm,
                                    onFormAmountChange = expensesViewModel::onFormAmountChange,
                                    onFormDescriptionChange = expensesViewModel::onFormDescriptionChange,
                                    onFormCategorySelect = expensesViewModel::onFormCategorySelect,
                                    onFormDateChange = expensesViewModel::onFormDateChange,
                                    onSaveExpense = expensesViewModel::saveExpense,
                                    onClearStatusMessage = expensesViewModel::clearStatusMessage
                                )
                            }

                            composable(SpendWiseRoutes.ROUTE_SETTINGS) {
                                SettingsScreen(
                                    uiState = settingsState,
                                    onSetCurrency = settingsViewModel::setCurrency,
                                    onSetThemeMode = settingsViewModel::setThemeMode,
                                    onOpenAddCategory = settingsViewModel::openAddCategory,
                                    onOpenEditCategory = settingsViewModel::openEditCategory,
                                    onDeleteCategory = settingsViewModel::deleteCategory,
                                    onDismissCategoryForm = settingsViewModel::dismissCategoryForm,
                                    onCategoryNameChange = settingsViewModel::onCategoryNameChange,
                                    onCategoryIconSelect = settingsViewModel::onCategoryIconSelect,
                                    onCategoryColorSelect = settingsViewModel::onCategoryColorSelect,
                                    onSaveCategory = settingsViewModel::saveCategory,
                                    onExportCsv = settingsViewModel::exportCsv,
                                    onOpenResetDialog = settingsViewModel::openResetDialog,
                                    onDismissResetDialog = settingsViewModel::dismissResetDialog,
                                    onConfirmReset = settingsViewModel::confirmResetAllData,
                                    onClearStatusMessage = settingsViewModel::clearStatusMessage
                                )
                            }

                            composable(SpendWiseRoutes.ROUTE_PROFILE) {
                                ProfileScreen(
                                    user = activeUser,
                                    currency = settingsState.currency,
                                    onSaveName = { newName, cb ->
                                        authViewModel.updateProfileName(activeUser.id, newName, cb)
                                    },
                                    onLogout = authViewModel::logout
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
