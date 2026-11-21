package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SpendWiseDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ExpenseWithCategory
import com.example.data.preferences.CurrencyOption
import com.example.util.CsvExporter
import com.example.util.PasswordHasher
import com.example.util.ValidationUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SpendWise", appName)
    }

    @Test
    fun `password hasher produces verifiable salted hash`() {
        val hashed = PasswordHasher.hashPassword("Secret123!")
        assertTrue(PasswordHasher.verifyPassword("Secret123!", hashed.hash, hashed.salt))
        assertFalse(PasswordHasher.verifyPassword("WrongPass", hashed.hash, hashed.salt))
    }

    @Test
    fun `validation utils enforce expense and auth constraints`() {
        assertNotNull(ValidationUtils.validateEmail("invalid-email"))
        assertNull(ValidationUtils.validateEmail("etudiant@bac3.edu"))

        val (badAmount, badAmountErr) = ValidationUtils.validateExpenseAmount("-12.5")
        assertNull(badAmount)
        assertNotNull(badAmountErr)

        val (goodAmount, goodAmountErr) = ValidationUtils.validateExpenseAmount("45,90")
        assertEquals(45.90, goodAmount!!, 0.001)
        assertNull(goodAmountErr)
    }

    @Test
    fun `csv exporter builds valid utf8 bom csv and neutralizes formula injection`() {
        val cat = CategoryEntity(id = 1L, userId = 1L, name = "Alimentation", iconKey = "food", colorHex = 0xFF0B5D4E)
        val exp = ExpenseEntity(id = 10L, userId = 1L, categoryId = 1L, amount = 18.5, description = "=SUM(A1), café", dateMillis = 1700000000000L)
        val csv = CsvExporter.buildCsvContent(
            expenses = listOf(ExpenseWithCategory(expense = exp, category = cat)),
            currency = CurrencyOption.EUR
        )
        assertTrue(csv.startsWith("\uFEFFID,Date,Catégorie,Description,Montant,Devise"))
        assertTrue(csv.contains("\"'=SUM(A1), café\""))
        assertTrue(csv.contains("18.50,EUR"))
    }

    @Test
    fun `room database inserts and reads user expenses`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, SpendWiseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val hashed = PasswordHasher.hashPassword("Demo2026")
            val userId = db.userDao().insertUser(
                com.example.data.local.entity.UserEntity(
                    name = "Alice",
                    email = "alice@spendwise.app",
                    passwordHash = hashed.hash,
                    passwordSalt = hashed.salt
                )
            )
            val catId = db.categoryDao().insertCategory(
                CategoryEntity(userId = userId, name = "Transport", iconKey = "transport", colorHex = 0xFF2563EB, isDefault = true)
            )
            db.expenseDao().insertExpense(
                ExpenseEntity(userId = userId, categoryId = catId, amount = 12.0, description = "Bus", dateMillis = System.currentTimeMillis())
            )
            val list = db.expenseDao().getExpensesWithCategory(userId)
            assertEquals(1, list.size)
            assertEquals("Transport", list.first().category.name)
        } finally {
            db.close()
        }
    }
}
