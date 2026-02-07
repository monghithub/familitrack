package com.monghit.familytrack.ui.screens.safezones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.repository.LocationRepository
import com.monghit.familytrack.data.repository.SettingsRepository
import com.monghit.familytrack.domain.model.SafeZone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SafeZonesUiState(
    val safeZones: List<SafeZone> = emptyList(),
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val showCreateDialog: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class SafeZonesViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafeZonesUiState())
    val uiState: StateFlow<SafeZonesUiState> = _uiState.asStateFlow()

    init {
        loadSafeZones()
    }

    fun loadSafeZones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                locationRepository.getFamilyLocations().collect { familyData ->
                    _uiState.update {
                        it.copy(
                            safeZones = familyData.safeZones,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading safe zones")
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al cargar zonas seguras")
                }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createSafeZone(name: String, lat: Double, lng: Double, radiusMeters: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            try {
                val userId = settingsRepository.userId.first()
                locationRepository.createSafeZone(
                    name = name,
                    lat = lat,
                    lng = lng,
                    radiusMeters = radiusMeters,
                    monitoredUserId = userId
                ).onSuccess { zone ->
                    _uiState.update {
                        it.copy(
                            safeZones = it.safeZones + zone,
                            isCreating = false,
                            showCreateDialog = false,
                            message = "Zona \"${zone.name}\" creada"
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(isCreating = false, message = "Error: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error creating safe zone")
                _uiState.update {
                    it.copy(isCreating = false, message = "Error al crear zona")
                }
            }
        }
    }

    fun deleteSafeZone(zoneId: Int) {
        viewModelScope.launch {
            try {
                locationRepository.deleteSafeZone(zoneId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                safeZones = it.safeZones.filter { z -> z.id != zoneId },
                                message = "Zona eliminada"
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(message = "Error: ${e.message}") }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error deleting safe zone")
                _uiState.update { it.copy(message = "Error al eliminar zona") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
