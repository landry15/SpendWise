package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val name: String,
    val iconKey: String,
    val colorHex: Long,
    val isDefault: Boolean = false
)
