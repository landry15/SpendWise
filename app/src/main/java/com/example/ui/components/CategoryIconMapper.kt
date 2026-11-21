package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object CategoryIconMapper {
    val availableIcons: List<CategoryIconOption> = listOf(
        CategoryIconOption("food", "Alimentation", Icons.Rounded.Restaurant),
        CategoryIconOption("transport", "Transport", Icons.Rounded.DirectionsCar),
        CategoryIconOption("housing", "Logement", Icons.Rounded.Home),
        CategoryIconOption("leisure", "Loisirs", Icons.Rounded.SportsEsports),
        CategoryIconOption("health", "Santé", Icons.Rounded.Favorite),
        CategoryIconOption("shopping", "Achats", Icons.Rounded.ShoppingBag),
        CategoryIconOption("education", "Études", Icons.Rounded.School),
        CategoryIconOption("bills", "Factures", Icons.Rounded.ReceiptLong),
        CategoryIconOption("other", "Autre", Icons.Rounded.Category)
    )

    val availableColors: List<Long> = listOf(
        0xFF0B5D4E,
        0xFF2563EB,
        0xFFD97706,
        0xFF9333EA,
        0xFFDC2626,
        0xFF0891B2,
        0xFFDB2777,
        0xFF4F46E5,
        0xFF475569
    )

    fun iconForKey(key: String): ImageVector {
        return availableIcons.firstOrNull { it.key == key }?.icon ?: Icons.Rounded.Category
    }

    fun colorFromHex(colorHex: Long): Color = Color(colorHex)
}
