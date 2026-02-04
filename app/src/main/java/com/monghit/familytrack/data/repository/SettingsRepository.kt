package com.monghit.familytrack.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val LOCATION_ENABLED = booleanPreferencesKey("location_enabled")
        val LOCATION_INTERVAL = intPreferencesKey("location_interval")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val DEVICE_TOKEN = stringPreferencesKey("device_token")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val IS_REGISTERED = booleanPreferencesKey("is_registered")
    }

    val isLocationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LOCATION_ENABLED] ?: false
        }

    val locationInterval: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LOCATION_INTERVAL] ?: 300
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    val userId: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID] ?: 0
        }

    val userName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: ""
        }

    val deviceToken: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEVICE_TOKEN] ?: ""
        }

    val deviceName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEVICE_NAME] ?: android.os.Build.MODEL
        }

    val isRegistered: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_REGISTERED] ?: false
        }

    suspend fun setLocationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_ENABLED] = enabled
        }
    }

    suspend fun setLocationInterval(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_INTERVAL] = seconds
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setUserId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = id
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setDeviceToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_TOKEN] = token
        }
    }

    suspend fun setDeviceName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_NAME] = name
        }
    }

    suspend fun setRegistered(registered: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_REGISTERED] = registered
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
