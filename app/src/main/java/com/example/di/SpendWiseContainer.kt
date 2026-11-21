package com.example.di

import android.content.Context
import com.example.data.local.SpendWiseDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ExpenseRepository

class SpendWiseContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: SpendWiseDatabase by lazy { SpendWiseDatabase.getInstance(appContext) }
    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }
    val authRepository: AuthRepository by lazy {
        AuthRepository(
            userDao = database.userDao(),
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao(),
            preferences = preferencesRepository
        )
    }
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao()
        )
    }
    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(
            expenseDao = database.expenseDao(),
            categoryDao = database.categoryDao(),
            userDao = database.userDao()
        )
    }
}
