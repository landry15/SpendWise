package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY isDefault DESC, name ASC")
    fun observeCategoriesForUser(userId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY isDefault DESC, name ASC")
    suspend fun getCategoriesForUser(userId: Long): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    @Query("DELETE FROM categories WHERE userId = :userId AND isDefault = 0")
    suspend fun deleteCustomCategoriesForUser(userId: Long)
}
