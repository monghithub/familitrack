package com.monghit.familytrack.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.remote.ApiService
import com.monghit.familytrack.data.remote.dto.ChatMessagesRequest
import com.monghit.familytrack.data.remote.dto.ChatSendRequest
import com.monghit.familytrack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ChatMessage(
    val id: Int,
    val content: String,
    val createdAt: String,
    val userId: Int,
    val userName: String,
    val isOwnMessage: Boolean
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val currentUserId: Int = 0
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(currentUserId = settingsRepository.userId.first()) }
            loadMessages()
            startPolling()
        }
    }

    private suspend fun loadMessages() {
        try {
            val familyId = settingsRepository.familyId.first()
            if (familyId == 0) return
            val currentUserId = settingsRepository.userId.first()
            val response = apiService.getChatMessages(ChatMessagesRequest(familyId))
            if (response.isSuccessful && response.body() != null) {
                val messages = response.body()!!.messages.map { dto ->
                    ChatMessage(
                        id = dto.id,
                        content = dto.content,
                        createdAt = dto.createdAt,
                        userId = dto.userId,
                        userName = dto.userName,
                        isOwnMessage = dto.userId == currentUserId
                    )
                }.reversed()
                _uiState.update { it.copy(messages = messages, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading messages")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(10_000)
                loadMessages()
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, inputText = "") }
            try {
                val userId = settingsRepository.userId.first()
                val response = apiService.sendChatMessage(
                    ChatSendRequest(fromUserId = userId, content = text)
                )
                if (response.isSuccessful) {
                    loadMessages()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error sending message")
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }
}
