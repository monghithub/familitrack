package com.monghit.familytrack.ui.screens.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.repository.LocationRepository
import com.monghit.familytrack.data.repository.SettingsRepository
import com.monghit.familytrack.domain.model.FamilyMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class FamilyUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val familyMembers: List<FamilyMember> = emptyList(),
    val error: String? = null,
    val notifyMessage: String? = null
)

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
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
                            isRefreshing = false,
                            familyMembers = familyData.members,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading family members")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadFamilyMembers()
    }

    fun sendNotification(member: FamilyMember) {
        viewModelScope.launch {
            try {
                val senderUserId = settingsRepository.userId.first()
                locationRepository.sendManualNotification(
                    fromUserId = senderUserId,
                    toUserId = member.user.id
                ).onSuccess {
                    _uiState.update {
                        it.copy(notifyMessage = "Notificaci\u00f3n enviada a ${member.displayName}")
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(notifyMessage = "Error al notificar: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error sending notification")
                _uiState.update {
                    it.copy(notifyMessage = "Error al notificar")
                }
            }
        }
    }

    fun clearNotifyMessage() {
        _uiState.update { it.copy(notifyMessage = null) }
    }
}
