package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpendWiseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: SpendWiseDatabase? = null

        fun getInstance(context: Context): SpendWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SpendWiseDatabase::class.java,
                    "spendwise_offline.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
