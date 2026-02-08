package com.monghit.familytrack.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.remote.ApiService
import com.monghit.familytrack.data.remote.dto.GetProfileRequest
import com.monghit.familytrack.data.remote.dto.UpdateProfileRequest
import com.monghit.familytrack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val role: String = "",
    val familyName: String = "",
    val inviteCode: String = "",
    val avatar: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val userId = settingsRepository.userId.first()
                val response = apiService.getProfile(GetProfileRequest(userId))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.update {
                        it.copy(
                            name = body.name,
                            role = body.role,
                            familyName = body.familyName ?: "",
                            inviteCode = body.inviteCode ?: "",
                            avatar = body.avatar,
                            isLoading = false
                        )
                    }
                } else {
                    // Fallback to local data
                    _uiState.update {
                        it.copy(
                            name = settingsRepository.userName.first(),
                            familyName = settingsRepository.familyName.first(),
                            inviteCode = settingsRepository.inviteCode.first(),
                            role = settingsRepository.userRole.first(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading profile")
                _uiState.update {
                    it.copy(
                        name = settingsRepository.userName.first(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val userId = settingsRepository.userId.first()
                val state = _uiState.value
                val response = apiService.updateProfile(
                    UpdateProfileRequest(
                        userId = userId,
                        name = state.name,
                        avatar = state.avatar
                    )
                )
                if (response.isSuccessful) {
                    settingsRepository.setUserName(state.name)
                    _uiState.update {
                        it.copy(isSaving = false, message = "Perfil actualizado")
                    }
                } else {
                    _uiState.update {
                        it.copy(isSaving = false, message = "Error al guardar")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving profile")
                _uiState.update {
                    it.copy(isSaving = false, message = "Error de conexión")
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
