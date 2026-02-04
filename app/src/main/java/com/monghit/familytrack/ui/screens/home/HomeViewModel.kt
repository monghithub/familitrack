package com.monghit.familytrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLocationEnabled: Boolean = false,
    val intervalMinutes: Int = 5,
    val lastUpdateFormatted: String = "Nunca"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
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
