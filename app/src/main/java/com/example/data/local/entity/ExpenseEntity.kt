package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["categoryId"]),
        Index(value = ["dateMillis"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val categoryId: Long,
    val amount: Double,
    val description: String,
    val dateMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
)
