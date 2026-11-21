package com.example.util

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Utilitaire de hachage sécurisé hors-ligne pour les mots de passe utilisateurs.
 * Utilise PBKDF2WithHmacSHA256 (avec repli PBKDF2WithHmacSHA1 sur API 24-25 si requis)
 * et un sel aléatoire unique de 16 octets par utilisateur.
 */
object PasswordHasher {
    private const val SALT_BYTES = 16
    private const val KEY_LENGTH_BITS = 256
    private const val ITERATIONS = 65_536
    private const val ALG_SHA256 = "PBKDF2WithHmacSHA256"
    private const val ALG_SHA1 = "PBKDF2WithHmacSHA1"

    data class HashedPassword(
        val hash: String,
        val salt: String
    )

    fun hashPassword(password: String): HashedPassword {
        val saltBytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(saltBytes)
        val saltBase64 = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val (algorithm, derived) = deriveKey(password.toCharArray(), saltBytes, ITERATIONS)
        val hashBase64 = Base64.encodeToString(derived, Base64.NO_WRAP)
        return HashedPassword(
            hash = "$algorithm$$ITERATIONS$$hashBase64",
            salt = saltBase64
        )
    }

    fun verifyPassword(password: String, storedHash: String, storedSalt: String): Boolean {
        return try {
            val parts = storedHash.split("$")
            if (parts.size != 3) return false
            val algorithm = parts[0]
            val iterations = parts[1].toIntOrNull() ?: return false
            val expectedBytes = Base64.decode(parts[2], Base64.NO_WRAP)
            val saltBytes = Base64.decode(storedSalt, Base64.NO_WRAP)

            val factory = SecretKeyFactory.getInstance(algorithm)
            val spec = PBEKeySpec(password.toCharArray(), saltBytes, iterations, expectedBytes.size * 8)
            val actualBytes = factory.generateSecret(spec).encoded
            spec.clearPassword()

            MessageDigest.isEqual(expectedBytes, actualBytes)
        } catch (_: Exception) {
            false
        }
    }

    private fun deriveKey(
        chars: CharArray,
        salt: ByteArray,
        iterations: Int
    ): Pair<String, ByteArray> {
        val algorithms = listOf(ALG_SHA256, ALG_SHA1)
        for (alg in algorithms) {
            try {
                val factory = SecretKeyFactory.getInstance(alg)
                val spec = PBEKeySpec(chars, salt, iterations, KEY_LENGTH_BITS)
                val encoded = factory.generateSecret(spec).encoded
                spec.clearPassword()
                return alg to encoded
            } catch (_: NoSuchAlgorithmException) {
                // Fallback vers SHA1 sur anciens niveaux API
            }
        }
        throw IllegalStateException("Aucun algorithme PBKDF2 disponible sur cet appareil")
    }
}
