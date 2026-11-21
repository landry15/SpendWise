package com.example.data.repository

import com.example.data.local.DatabaseSeeder
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.util.PasswordHasher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val preferences: UserPreferencesRepository
) {
    val loggedInUserId: Flow<Long?> = preferences.loggedInUserId

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserEntity?> = preferences.loggedInUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null) else userDao.observeUserById(userId)
    }

    suspend fun ensureDemoSeeded() {
        DatabaseSeeder.ensureDemoAccountSeeded(userDao, categoryDao, expenseDao)
    }

    suspend fun register(name: String, email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()

        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return AuthResult.Error("Un compte existe déjà avec cette adresse e-mail.")
        }

        val hashed = PasswordHasher.hashPassword(password)
        val userId = userDao.insertUser(
            UserEntity(
                name = cleanName,
                email = cleanEmail,
                passwordHash = hashed.hash,
                passwordSalt = hashed.salt,
                monthlyBudget = 1500.0
            )
        )
        categoryDao.insertCategories(DatabaseSeeder.defaultCategoriesForUser(userId))
        preferences.setLoggedInUserId(userId)
        val createdUser = userDao.getUserById(userId)
            ?: return AuthResult.Error("Impossible de charger le profil créé.")
        return AuthResult.Success(createdUser)
    }

    suspend fun login(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return AuthResult.Error("Identifiants incorrects.")

        val valid = PasswordHasher.verifyPassword(password, user.passwordHash, user.passwordSalt)
        if (!valid) {
            return AuthResult.Error("Identifiants incorrects.")
        }

        preferences.setLoggedInUserId(user.id)
        return AuthResult.Success(user)
    }

    suspend fun updateProfileName(userId: Long, newName: String) {
        userDao.updateUserName(userId, newName.trim())
    }

    suspend fun updateMonthlyBudget(userId: Long, budget: Double) {
        userDao.updateMonthlyBudget(userId, budget)
    }

    suspend fun logout() {
        preferences.setLoggedInUserId(null)
    }
}
