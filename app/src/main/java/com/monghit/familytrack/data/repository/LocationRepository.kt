package com.monghit.familytrack.data.repository

import com.monghit.familytrack.data.remote.ApiService
import com.monghit.familytrack.data.remote.dto.LocationUpdateRequest
import com.monghit.familytrack.data.remote.dto.RegisterDeviceRequest
import com.monghit.familytrack.domain.model.FamilyMember
import com.monghit.familytrack.domain.model.Location
import com.monghit.familytrack.domain.model.LocationUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository
) {
    suspend fun registerDevice(deviceToken: String, deviceName: String): Result<Int> {
        return try {
            val userId = settingsRepository.userId.first()
            val request = RegisterDeviceRequest(
                deviceToken = deviceToken,
                userId = userId,
                deviceName = deviceName
            )
            val response = apiService.registerDevice(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                settingsRepository.setRegistered(true)
                settingsRepository.setLocationInterval(body.locationInterval)
                Result.success(body.deviceId)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error registering device")
            Result.failure(e)
        }
    }

    suspend fun sendLocation(location: Location): Result<Boolean> {
        return try {
            val deviceToken = settingsRepository.deviceToken.first()
            val userId = settingsRepository.userId.first()

            val request = LocationUpdateRequest(
                userId = userId,
                deviceToken = deviceToken,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                timestamp = location.timestamp
            )

            val response = apiService.updateLocation(request)
            if (response.isSuccessful) {
                Timber.d("Location sent successfully")
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to send location: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending location")
            Result.failure(e)
        }
    }

    fun getFamilyLocations(): Flow<List<FamilyMember>> = flow {
        // TODO: Implement API call to get family locations
        // For now, emit empty list
        emit(emptyList())
    }

    suspend fun updateLocationInterval(seconds: Int): Result<Boolean> {
        return try {
            val deviceToken = settingsRepository.deviceToken.first()
            val response = apiService.updateLocationInterval(
                deviceToken = deviceToken,
                intervalSeconds = seconds
            )
            if (response.isSuccessful) {
                settingsRepository.setLocationInterval(seconds)
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update interval"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating interval")
            Result.failure(e)
        }
    }
}
