package com.oxygen.finance_buddy.core.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_prefs")

class SecurityPreferences(private val context: Context) {

    companion object {
        val PIN_KEY = stringPreferencesKey("user_pin")
        val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        val FIRST_ACCESS = booleanPreferencesKey("first_access")
    }

    val userPin: Flow<String?> = context.dataStore.data.map { prefs -> prefs[PIN_KEY] }
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[BIOMETRIC_ENABLED_KEY] ?: false }
    val isFirstAccess: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[FIRST_ACCESS] ?: true }

    suspend fun savePin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[PIN_KEY] = pin
            prefs[FIRST_ACCESS] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
}

