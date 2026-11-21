package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao
) {
    fun observeCategories(userId: Long): Flow<List<CategoryEntity>> =
        categoryDao.observeCategoriesForUser(userId)

    suspend fun getCategories(userId: Long): List<CategoryEntity> =
        categoryDao.getCategoriesForUser(userId)

    suspend fun addCategory(
        userId: Long,
        name: String,
        iconKey: String,
        colorHex: Long
    ): Long {
        return categoryDao.insertCategory(
            CategoryEntity(
                userId = userId,
                name = name.trim(),
                iconKey = iconKey,
                colorHex = colorHex,
                isDefault = false
            )
        )
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category.copy(name = category.name.trim()))
    }

    /**
     * Supprime une catégorie personnalisée en réaffectant ses éventuelles dépenses
     * vers la catégorie de repli (« Autre » ou première catégorie par défaut).
     */
    suspend fun deleteCustomCategory(userId: Long, category: CategoryEntity): Boolean {
        if (category.isDefault) return false
        val allCategories = categoryDao.getCategoriesForUser(userId)
        val fallback = allCategories.firstOrNull { it.id != category.id && it.name.equals("Autre", ignoreCase = true) }
            ?: allCategories.firstOrNull { it.id != category.id }
            ?: return false

        expenseDao.reassignCategory(
            userId = userId,
            oldCategoryId = category.id,
            newCategoryId = fallback.id
        )
        categoryDao.deleteCategoryById(category.id)
        return true
    }
}
