package com.monghit.familytrack.ui.screens.pin

import androidx.lifecycle.ViewModel
import com.monghit.familytrack.data.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class PinMode {
    CREATE,
    CONFIRM,
    VERIFY
}

data class PinUiState(
    val mode: PinMode = PinMode.VERIFY,
    val pin: String = "",
    val firstPin: String = "",
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val biometricAvailable: Boolean = false
)

@HiltViewModel
class PinViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    init {
        val isPinSet = securityRepository.isPinSet()
        _uiState.update {
            it.copy(
                mode = if (isPinSet) PinMode.VERIFY else PinMode.CREATE,
                biometricAvailable = securityRepository.isBiometricEnabled()
            )
        }
    }

    fun onDigit(digit: Char) {
        val current = _uiState.value
        if (current.pin.length >= 4) return

        val newPin = current.pin + digit
        _uiState.update { it.copy(pin = newPin, error = null) }

        if (newPin.length == 4) {
            when (current.mode) {
                PinMode.CREATE -> {
                    _uiState.update {
                        it.copy(
                            mode = PinMode.CONFIRM,
                            firstPin = newPin,
                            pin = ""
                        )
                    }
                }
                PinMode.CONFIRM -> {
                    if (newPin == current.firstPin) {
                        securityRepository.setPin(newPin)
                        _uiState.update { it.copy(isAuthenticated = true) }
                    } else {
                        _uiState.update {
                            it.copy(
                                mode = PinMode.CREATE,
                                pin = "",
                                firstPin = "",
                                error = "Los PINs no coinciden. Inténtalo de nuevo."
                            )
                        }
                    }
                }
                PinMode.VERIFY -> {
                    if (securityRepository.verifyPin(newPin)) {
                        _uiState.update { it.copy(isAuthenticated = true) }
                    } else {
                        _uiState.update {
                            it.copy(pin = "", error = "PIN incorrecto")
                        }
                    }
                }
            }
        }
    }

    fun onDelete() {
        _uiState.update {
            it.copy(pin = it.pin.dropLast(1), error = null)
        }
    }

    fun onBiometricSuccess() {
        _uiState.update { it.copy(isAuthenticated = true) }
    }
}
