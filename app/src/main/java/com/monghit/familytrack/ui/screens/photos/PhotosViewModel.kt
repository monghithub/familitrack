package com.monghit.familytrack.ui.screens.photos

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.remote.ApiService
import com.monghit.familytrack.data.remote.dto.PhotoListRequest
import com.monghit.familytrack.data.remote.dto.PhotoSendRequest
import com.monghit.familytrack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class PhotoItem(
    val id: Int,
    val fromUserId: Int,
    val fromName: String,
    val toUserId: Int,
    val caption: String?,
    val createdAt: String
)

data class PhotosUiState(
    val photos: List<PhotoItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotosUiState())
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            try {
                val userId = settingsRepository.userId.first()
                val response = apiService.getPhotoList(PhotoListRequest(userId))
                if (response.isSuccessful && response.body() != null) {
                    val photos = response.body()!!.photos.map { dto ->
                        PhotoItem(
                            id = dto.id,
                            fromUserId = dto.fromUserId,
                            fromName = dto.fromName,
                            toUserId = dto.toUserId,
                            caption = dto.caption,
                            createdAt = dto.createdAt
                        )
                    }
                    _uiState.update { it.copy(photos = photos, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading photos")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendPhoto(bitmap: Bitmap, toUserId: Int, caption: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            try {
                val userId = settingsRepository.userId.first()
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                val imageData = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

                val response = apiService.sendPhoto(
                    PhotoSendRequest(
                        fromUserId = userId,
                        toUserId = toUserId,
                        imageData = imageData,
                        caption = caption
                    )
                )
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSending = false, message = "Foto enviada") }
                    loadPhotos()
                } else {
                    _uiState.update { it.copy(isSending = false, message = "Error al enviar foto") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error sending photo")
                _uiState.update { it.copy(isSending = false, message = "Error de conexion") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
