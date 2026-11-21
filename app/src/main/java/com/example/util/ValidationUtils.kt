package com.example.util

import java.util.regex.Pattern

object ValidationUtils {
    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    fun validateName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Le nom est obligatoire"
            trimmed.length < 2 -> "Le nom doit contenir au moins 2 caractères"
            trimmed.length > 50 -> "Le nom ne doit pas dépasser 50 caractères"
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "L'adresse e-mail est obligatoire"
            !EMAIL_PATTERN.matcher(trimmed).matches() -> "Adresse e-mail invalide"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Le mot de passe est obligatoire"
            password.length < 6 -> "Au moins 6 caractères requis"
            else -> null
        }
    }

    fun validateExpenseAmount(rawAmount: String): Pair<Double?, String?> {
        val normalized = rawAmount.trim().replace(',', '.')
        if (normalized.isEmpty()) {
            return null to "Le montant est obligatoire"
        }
        val parsed = normalized.toDoubleOrNull()
        return when {
            parsed == null -> null to "Montant numérique invalide"
            parsed <= 0.0 -> null to "Le montant doit être supérieur à 0"
            parsed > 10_000_000.0 -> null to "Montant trop élevé"
            else -> parsed to null
        }
    }

    fun validateExpenseDescription(description: String): String? {
        val trimmed = description.trim()
        return when {
            trimmed.isEmpty() -> "La description est obligatoire"
            trimmed.length < 2 -> "Description trop courte"
            trimmed.length > 120 -> "Maximum 120 caractères"
            else -> null
        }
    }

    fun validateBudget(rawBudget: String): Pair<Double?, String?> {
        val normalized = rawBudget.trim().replace(',', '.')
        if (normalized.isEmpty()) return null to "Le budget est obligatoire"
        val parsed = normalized.toDoubleOrNull()
        return when {
            parsed == null -> null to "Montant numérique invalide"
            parsed <= 0.0 -> null to "Le budget doit être supérieur à 0"
            else -> parsed to null
        }
    }

    fun validateCategoryName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Le nom de catégorie est obligatoire"
            trimmed.length < 2 -> "Au moins 2 caractères requis"
            trimmed.length > 28 -> "Maximum 28 caractères"
            else -> null
        }
    }
}
