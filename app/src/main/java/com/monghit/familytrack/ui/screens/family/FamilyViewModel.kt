package com.monghit.familytrack.ui.screens.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.repository.LocationRepository
import com.monghit.familytrack.domain.model.FamilyMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyUiState(
    val isLoading: Boolean = true,
    val familyMembers: List<FamilyMember> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    init {
        loadFamilyMembers()
    }

    private fun loadFamilyMembers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                locationRepository.getFamilyLocations().collect { familyData ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            familyMembers = familyData.members
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

    fun refresh() {
        loadFamilyMembers()
    }
}
