package com.example.data.local

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.UserEntity
import com.example.util.FormatUtils
import com.example.util.PasswordHasher

object DatabaseSeeder {
    const val DEMO_EMAIL = "demo@spendwise.app"
    const val DEMO_PASSWORD = "SpendWise2026!"

    fun defaultCategoriesForUser(userId: Long): List<CategoryEntity> {
        return listOf(
            CategoryEntity(
                userId = userId,
                name = "Alimentation",
                iconKey = "food",
                colorHex = 0xFF0B5D4E,
                isDefault = true
            ),
            CategoryEntity(
                userId = userId,
                name = "Transport",
                iconKey = "transport",
                colorHex = 0xFF2563EB,
                isDefault = true
            ),
            CategoryEntity(
                userId = userId,
                name = "Logement",
                iconKey = "housing",
                colorHex = 0xFFD97706,
                isDefault = true
            ),
            CategoryEntity(
                userId = userId,
                name = "Loisirs",
                iconKey = "leisure",
                colorHex = 0xFF9333EA,
                isDefault = true
            ),
            CategoryEntity(
                userId = userId,
                name = "Santé",
                iconKey = "health",
                colorHex = 0xFFDC2626,
                isDefault = true
            ),
            CategoryEntity(
                userId = userId,
                name = "Autre",
                iconKey = "other",
                colorHex = 0xFF475569,
                isDefault = true
            )
        )
    }

    suspend fun ensureDemoAccountSeeded(
        userDao: UserDao,
        categoryDao: CategoryDao,
        expenseDao: ExpenseDao
    ) {
        if (userDao.countUsers() > 0) return

        val hashed = PasswordHasher.hashPassword(DEMO_PASSWORD)
        val demoUserId = userDao.insertUser(
            UserEntity(
                name = "Étudiant Démo",
                email = DEMO_EMAIL,
                passwordHash = hashed.hash,
                passwordSalt = hashed.salt,
                monthlyBudget = 1200.0
            )
        )

        val categoryIds = categoryDao.insertCategories(defaultCategoriesForUser(demoUserId))
        if (categoryIds.size < 6) return

        val foodId = categoryIds[0]
        val transportId = categoryIds[1]
        val housingId = categoryIds[2]
        val leisureId = categoryIds[3]
        val healthId = categoryIds[4]
        val otherId = categoryIds[5]

        val now = System.currentTimeMillis()
        val demoExpenses = listOf(
            ExpenseEntity(
                userId = demoUserId,
                categoryId = foodId,
                amount = 14.80,
                description = "Déjeuner cafétéria campus",
                dateMillis = now
            ),
            ExpenseEntity(
                userId = demoUserId,
                categoryId = transportId,
                amount = 3.20,
                description = "Ticket bus aller-retour",
                dateMillis = now
            ),
            ExpenseEntity(
                userId = demoUserId,
                categoryId = foodId,
                amount = 54.60,
                description = "Courses supermarché semaine",
                dateMillis = FormatUtils.daysAgoMillis(2, now)
            ),
            ExpenseEntity(
                userId = demoUserId,
                categoryId = leisureId,
                amount = 22.00,
                description = "Séance cinéma & pop-corn",
                dateMillis = FormatUtils.daysAgoMillis(3, now)
            ),
            ExpenseEntity(
                userId = demoUserId,
                categoryId = healthId,
                amount = 18.50,
                description = "Pharmacie vitamines",
                dateMillis = FormatUtils.daysAgoMillis(5, now)
            ),
            ExpenseEntity(
                userId = demoUserId,
                categoryId = housingId,
                amount = 420.00,
                description = "Part loyer studio étudiant",
                dateMillis = FormatUtils.daysAgoMillis(8, now)
            ),
            ExpenseEntity(
                userId = demoUserId,
                categoryId = transportId,
                amount = 38.00,
                description = "Abonnement transport mensuel",
                dateMillis = FormatUtils.daysAgoMillis(10, now)
            ),
            ExpenseEntity(
                userId = demoUserId,
                categoryId = otherId,
                amount = 29.90,
                description = "Fournitures & impression dossier",
                dateMillis = FormatUtils.daysAgoMillis(12, now)
            )
        )
        expenseDao.insertExpenses(demoExpenses)
    }
}
