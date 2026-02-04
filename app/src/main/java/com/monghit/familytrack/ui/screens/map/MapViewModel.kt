package com.monghit.familytrack.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.monghit.familytrack.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val isLoading: Boolean = true,
    val familyLocations: List<FamilyLocationMarker> = emptyList(),
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
    }

    private fun loadFamilyLocations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                locationRepository.getFamilyLocations().collect { members ->
                    val markers = members.map { member ->
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
                            familyLocations = markers
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun refreshLocations() {
        loadFamilyLocations()
    }
}
