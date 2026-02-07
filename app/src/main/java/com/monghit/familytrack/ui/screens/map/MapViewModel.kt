package com.monghit.familytrack.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.monghit.familytrack.data.repository.LocationRepository
import com.monghit.familytrack.domain.model.SafeZone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class MapUiState(
    val isLoading: Boolean = true,
    val familyLocations: List<FamilyLocationMarker> = emptyList(),
    val safeZones: List<SafeZone> = emptyList(),
    val currentUserLocation: LatLng? = null,
    val error: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadFamilyLocations()
        startAutoRefresh()
    }

    private fun loadFamilyLocations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                locationRepository.getFamilyLocations().collect { familyData ->
                    val markers = familyData.members.map { member ->
                        FamilyLocationMarker(
                            userId = member.user.id,
                            name = member.displayName,
                            position = LatLng(
                                member.lastLocation?.latitude ?: 0.0,
                                member.lastLocation?.longitude ?: 0.0
                            ),
                            lastSeenFormatted = member.lastSeenFormatted,
                            isCurrentUser = false
                        )
                    }.filter { it.position.latitude != 0.0 && it.position.longitude != 0.0 }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            familyLocations = markers,
                            safeZones = familyData.safeZones
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading family locations")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                refreshLocations()
            }
        }
    }

    fun refreshLocations() {
        loadFamilyLocations()
    }
}
