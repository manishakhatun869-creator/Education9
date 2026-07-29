package com.example.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "towfik_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val KEY_USER_TYPE = stringPreferencesKey("user_type") // "ADMIN", "STUDENT", or ""
        val KEY_STUDENT_CODE = stringPreferencesKey("student_code")
        val KEY_ADMIN_EMAIL = stringPreferencesKey("admin_email")
        val KEY_REMEMBER_ADMIN = booleanPreferencesKey("remember_admin")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_DARK_MODE] ?: false
    }

    val userType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_TYPE] ?: ""
    }

    val studentCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_STUDENT_CODE] ?: ""
    }

    val adminEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_ADMIN_EMAIL] ?: "admin@gmail.com"
    }

    val rememberAdmin: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_REMEMBER_ADMIN] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_DARK_MODE] = enabled
        }
    }

    suspend fun setLoggedInAdmin(email: String, remember: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_TYPE] = "ADMIN"
            preferences[KEY_ADMIN_EMAIL] = email
            preferences[KEY_REMEMBER_ADMIN] = remember
        }
    }

    suspend fun setLoggedInStudent(code: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_TYPE] = "STUDENT"
            preferences[KEY_STUDENT_CODE] = code
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_TYPE] = ""
            preferences[KEY_STUDENT_CODE] = ""
        }
    }
}
