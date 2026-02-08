package com.monghit.familytrack.ui.screens.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.monghit.familytrack.data.remote.ApiService
import com.monghit.familytrack.data.remote.dto.EmergencyRequest
import com.monghit.familytrack.data.repository.LocationRepository
import com.monghit.familytrack.data.repository.SettingsRepository
import com.monghit.familytrack.services.LocationForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val isLocationEnabled: Boolean = false,
    val intervalMinutes: Int = 5,
    val lastUpdateFormatted: String = "Nunca",
    val isRegistered: Boolean = false,
    val deviceToken: String = "",
    val isSendingSos: Boolean = false,
    val sosMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

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
        viewModelScope.launch {
            settingsRepository.lastLocationUpdate.collect { timestamp ->
                val formatted = if (timestamp > 0) {
                    dateFormat.format(Date(timestamp))
                } else {
                    "Nunca"
                }
                _uiState.update { it.copy(lastUpdateFormatted = formatted) }
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
            if (enabled) {
                LocationForegroundService.start(application)
            } else {
                LocationForegroundService.stop(application)
            }
        }
    }

    fun updateInterval(minutes: Int) {
        viewModelScope.launch {
            val seconds = minutes * 60
            settingsRepository.setLocationInterval(seconds)
            _uiState.update { it.copy(intervalMinutes = minutes) }

            if (_uiState.value.isLocationEnabled) {
                LocationForegroundService.updateInterval(application, seconds)
            }
        }
    }

    fun sendSos(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingSos = true) }
            try {
                val userId = settingsRepository.userId.first()
                val response = apiService.sendEmergency(
                    EmergencyRequest(userId = userId, latitude = latitude, longitude = longitude)
                )
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSendingSos = false, sosMessage = "SOS enviado a tu familia") }
                } else {
                    _uiState.update { it.copy(isSendingSos = false, sosMessage = "Error al enviar SOS") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error sending SOS")
                _uiState.update { it.copy(isSendingSos = false, sosMessage = "Error de conexion") }
            }
        }
    }

    fun clearSosMessage() {
        _uiState.update { it.copy(sosMessage = null) }
    }
}
