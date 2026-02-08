package com.monghit.familytrack.ui.screens.routehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monghit.familytrack.data.remote.ApiService
import com.monghit.familytrack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: String?
)

data class RouteHistoryUiState(
    val points: List<RoutePoint> = emptyList(),
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isLoading: Boolean = false
)

@HiltViewModel
class RouteHistoryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteHistoryUiState())
    val uiState: StateFlow<RouteHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userId = settingsRepository.userId.first()
                val date = _uiState.value.selectedDate
                val response = apiService.getLocationHistory(userId, date)
                if (response.isSuccessful && response.body() != null) {
                    val points = response.body()!!.locations.map { dto ->
                        RoutePoint(
                            latitude = dto.latitude,
                            longitude = dto.longitude,
                            accuracy = dto.accuracy,
                            timestamp = dto.timestamp
                        )
                    }
                    _uiState.update { it.copy(points = points, isLoading = false) }
                } else {
                    _uiState.update { it.copy(points = emptyList(), isLoading = false) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading route history")
                _uiState.update { it.copy(points = emptyList(), isLoading = false) }
            }
        }
    }
}
