package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY dateMillis DESC, createdAt DESC")
    fun observeExpensesWithCategory(userId: Long): Flow<List<ExpenseWithCategory>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY dateMillis DESC, createdAt DESC")
    suspend fun getExpensesWithCategory(userId: Long): List<ExpenseWithCategory>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Long): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)

    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun deleteAllExpensesForUser(userId: Long)

    @Query("UPDATE expenses SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId AND userId = :userId")
    suspend fun reassignCategory(userId: Long, oldCategoryId: Long, newCategoryId: Long)
}
