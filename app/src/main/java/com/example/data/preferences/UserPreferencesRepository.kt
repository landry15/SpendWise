package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.spendWiseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spendwise_preferences"
)

enum class ThemeMode(val label: String) {
    SYSTEM("Système"),
    LIGHT("Clair"),
    DARK("Sombre")
}

enum class CurrencyOption(
    val code: String,
    val symbol: String,
    val label: String,
    val decimals: Int = 2
) {
    EUR("EUR", "€", "Euro (€)"),
    USD("USD", "$", "Dollar US ($)"),
    CDF("CDF", "FC", "Franc congolais (FC)", decimals = 0),
    XAF("XAF", "FCFA", "Franc CFA (FCFA)", decimals = 0),
    MAD("MAD", "DH", "Dirham marocain (DH)"),
    CAD("CAD", "\$CA", "Dollar canadien (\$CA)"),
    CHF("CHF", "CHF", "Franc suisse (CHF)");

    companion object {
        fun fromCode(code: String?): CurrencyOption {
            return entries.firstOrNull { it.code == code } ?: EUR
        }
    }
}

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val loggedInUserId: Flow<Long?> = context.spendWiseDataStore.data.map { prefs ->
        val id = prefs[Keys.LOGGED_IN_USER_ID]
        if (id == null || id <= 0L) null else id
    }

    val currency: Flow<CurrencyOption> = context.spendWiseDataStore.data.map { prefs ->
        CurrencyOption.fromCode(prefs[Keys.CURRENCY_CODE])
    }

    val themeMode: Flow<ThemeMode> = context.spendWiseDataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME_MODE]
        ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }

    suspend fun setLoggedInUserId(userId: Long?) {
        context.spendWiseDataStore.edit { prefs ->
            if (userId == null || userId <= 0L) {
                prefs.remove(Keys.LOGGED_IN_USER_ID)
            } else {
                prefs[Keys.LOGGED_IN_USER_ID] = userId
            }
        }
    }

    suspend fun setCurrency(option: CurrencyOption) {
        context.spendWiseDataStore.edit { prefs ->
            prefs[Keys.CURRENCY_CODE] = option.code
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.spendWiseDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }
}
