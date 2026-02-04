package com.monghit.familytrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.monghit.familytrack.data.repository.LocationRepository
import com.monghit.familytrack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

data class HomeUiState(
    val isLocationEnabled: Boolean = false,
    val intervalMinutes: Int = 5,
    val lastUpdateFormatted: String = "Nunca",
    val isRegistered: Boolean = false,
    val deviceToken: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        ensureRegistered()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.isLocationEnabled.collect { enabled ->
                _uiState.update { it.copy(isLocationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.locationInterval.collect { interval ->
                _uiState.update { it.copy(intervalMinutes = interval / 60) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isRegistered.collect { registered ->
                _uiState.update { it.copy(isRegistered = registered) }
            }
        }
    }

    private fun ensureRegistered() {
        viewModelScope.launch {
            try {
                // Get FCM token
                val token = FirebaseMessaging.getInstance().token.await()
                Timber.d("FCM token: $token")
                settingsRepository.setDeviceToken(token)
                _uiState.update { it.copy(deviceToken = token) }

                // Set default userId if not set
                val currentUserId = settingsRepository.userId.first()
                if (currentUserId == 0) {
                    settingsRepository.setUserId(1)
                }

                // Register with backend
                val deviceName = settingsRepository.deviceName.first()
                val result = locationRepository.registerDevice(token, deviceName)
                result.onSuccess { deviceId ->
                    Timber.d("Device registered with ID: $deviceId")
                }.onFailure { error ->
                    Timber.e(error, "Failed to register device")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during registration")
            }
        }
    }

    fun toggleLocationSharing(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLocationEnabled(enabled)
        }
    }

    fun updateInterval(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setLocationInterval(minutes * 60)
            _uiState.update { it.copy(intervalMinutes = minutes) }
        }
    }
}
