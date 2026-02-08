package com.monghit.familytrack.ui.screens.familysetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SetupStep {
    CHOOSE,
    CREATE_FAMILY,
    JOIN_FAMILY,
    SUCCESS
}

data class FamilySetupUiState(
    val step: SetupStep = SetupStep.CHOOSE,
    val userName: String = "",
    val familyName: String = "",
    val inviteCode: String = "",
    val resultInviteCode: String = "",
    val resultFamilyName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FamilySetupViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilySetupUiState())
    val uiState: StateFlow<FamilySetupUiState> = _uiState.asStateFlow()

    fun goToCreate() {
        _uiState.update { it.copy(step = SetupStep.CREATE_FAMILY, error = null) }
    }

    fun goToJoin() {
        _uiState.update { it.copy(step = SetupStep.JOIN_FAMILY, error = null) }
    }

    fun goBack() {
        _uiState.update { it.copy(step = SetupStep.CHOOSE, error = null) }
    }

    fun updateUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun updateFamilyName(name: String) {
        _uiState.update { it.copy(familyName = name) }
    }

    fun updateInviteCode(code: String) {
        _uiState.update { it.copy(inviteCode = code.uppercase().take(6)) }
    }

    fun createFamily() {
        val state = _uiState.value
        if (state.userName.isBlank() || state.familyName.isBlank()) {
            _uiState.update { it.copy(error = "Completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = locationRepository.createFamily(state.familyName, state.userName)
            result.onSuccess { (_, _, inviteCode) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        step = SetupStep.SUCCESS,
                        resultInviteCode = inviteCode,
                        resultFamilyName = state.familyName
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message ?: "Error al crear familia")
                }
            }
        }
    }

    fun joinFamily() {
        val state = _uiState.value
        if (state.userName.isBlank() || state.inviteCode.length != 6) {
            _uiState.update { it.copy(error = "Introduce tu nombre y el código de 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = locationRepository.joinFamily(state.inviteCode, state.userName)
            result.onSuccess { (_, _) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        step = SetupStep.SUCCESS,
                        resultFamilyName = ""
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Código de invitación inválido")
                }
            }
        }
    }
}
