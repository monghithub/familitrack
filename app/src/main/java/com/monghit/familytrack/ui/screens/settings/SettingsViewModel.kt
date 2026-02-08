package com.monghit.familytrack.ui.screens.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.repository.LocationRepository
import com.monghit.familytrack.data.repository.SecurityRepository
import com.monghit.familytrack.data.repository.SettingsRepository
import com.monghit.familytrack.services.LocationForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsUiState(
    val isLocationEnabled: Boolean = false,
    val intervalMinutes: Int = 5,
    val notificationsEnabled: Boolean = true,
    val userName: String = "",
    val deviceName: String = "",
    val deviceToken: String = "",
    val deviceId: Int = 0,
    val userId: Int = 0,
    val isRegistered: Boolean = false,
    val lastLocationUpdate: String = "Nunca",
    val actionMessage: String? = null,
    val isPinSet: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val darkMode: String = "system"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadSecuritySettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.isLocationEnabled.collect { enabled ->
                _uiState.update { it.copy(isLocationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.locationInterval.collect { seconds ->
                _uiState.update { it.copy(intervalMinutes = seconds / 60) }
            }
        }
        viewModelScope.launch {
            settingsRepository.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
        viewModelScope.launch {
            settingsRepository.deviceName.collect { name ->
                _uiState.update { it.copy(deviceName = name) }
            }
        }
        viewModelScope.launch {
            settingsRepository.deviceToken.collect { token ->
                _uiState.update { it.copy(deviceToken = token) }
            }
        }
        viewModelScope.launch {
            settingsRepository.deviceId.collect { id ->
                _uiState.update { it.copy(deviceId = id) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userId.collect { id ->
                _uiState.update { it.copy(userId = id) }
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
                    val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(timestamp))
                } else "Nunca"
                _uiState.update { it.copy(lastLocationUpdate = formatted) }
            }
        }
        viewModelScope.launch {
            settingsRepository.darkMode.collect { mode ->
                _uiState.update { it.copy(darkMode = mode) }
            }
        }
    }

    private fun loadSecuritySettings() {
        _uiState.update {
            it.copy(
                isPinSet = securityRepository.isPinSet(),
                isBiometricEnabled = securityRepository.isBiometricEnabled()
            )
        }
    }

    fun togglePinEnabled(enabled: Boolean) {
        if (!enabled) {
            securityRepository.clearPin()
            _uiState.update { it.copy(isPinSet = false, isBiometricEnabled = false) }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        securityRepository.setBiometricEnabled(enabled)
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun onPinCreated() {
        _uiState.update { it.copy(isPinSet = true) }
    }

    fun toggleLocationEnabled(enabled: Boolean) {
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
            locationRepository.updateLocationInterval(seconds)
            if (_uiState.value.isLocationEnabled) {
                LocationForegroundService.updateInterval(application, seconds)
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(mode)
        }
    }

    fun updateDeviceName(name: String) {
        viewModelScope.launch {
            settingsRepository.setDeviceName(name)
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            try {
                val userId = settingsRepository.userId.first()
                locationRepository.sendManualNotification(
                    fromUserId = userId,
                    toUserId = userId
                ).onSuccess {
                    _uiState.update { it.copy(actionMessage = "Notificaci\u00f3n de prueba enviada") }
                }.onFailure { e ->
                    _uiState.update { it.copy(actionMessage = "Error: ${e.message}") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error sending test notification")
                _uiState.update { it.copy(actionMessage = "Error al enviar") }
            }
        }
    }

    fun forceLocationSend() {
        viewModelScope.launch {
            if (_uiState.value.isLocationEnabled) {
                LocationForegroundService.stop(application)
                LocationForegroundService.start(application)
                _uiState.update { it.copy(actionMessage = "Enviando ubicaci\u00f3n...") }
            } else {
                _uiState.update { it.copy(actionMessage = "Activa la ubicaci\u00f3n primero") }
            }
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            LocationForegroundService.stop(application)
            settingsRepository.clearAll()
        }
    }
}
