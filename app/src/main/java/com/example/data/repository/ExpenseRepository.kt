package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val userDao: UserDao
) {
    fun observeExpenses(userId: Long): Flow<List<ExpenseWithCategory>> =
        expenseDao.observeExpensesWithCategory(userId)

    suspend fun getExpensesSnapshot(userId: Long): List<ExpenseWithCategory> =
        expenseDao.getExpensesWithCategory(userId)

    suspend fun addExpense(
        userId: Long,
        categoryId: Long,
        amount: Double,
        description: String,
        dateMillis: Long
    ): Long {
        return expenseDao.insertExpense(
            ExpenseEntity(
                userId = userId,
                categoryId = categoryId,
                amount = amount,
                description = description.trim(),
                dateMillis = dateMillis
            )
        )
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense.copy(description = expense.description.trim()))
    }

    suspend fun deleteExpense(expenseId: Long) {
        expenseDao.deleteExpenseById(expenseId)
    }

    suspend fun resetUserData(userId: Long) {
        expenseDao.deleteAllExpensesForUser(userId)
        categoryDao.deleteCustomCategoriesForUser(userId)
        userDao.updateMonthlyBudget(userId, 1500.0)
    }
}
