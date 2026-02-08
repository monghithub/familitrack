package com.monghit.familytrack.data.repository

import com.monghit.familytrack.data.remote.ApiService
import com.monghit.familytrack.data.remote.dto.CreateFamilyRequest
import com.monghit.familytrack.data.remote.dto.CreateSafeZoneRequest
import com.monghit.familytrack.data.remote.dto.DeleteSafeZoneRequest
import com.monghit.familytrack.data.remote.dto.JoinFamilyRequest
import com.monghit.familytrack.data.remote.dto.LocationUpdateRequest
import com.monghit.familytrack.data.remote.dto.ManualNotifyRequest
import com.monghit.familytrack.data.remote.dto.RegisterDeviceRequest
import com.monghit.familytrack.domain.model.Device
import com.monghit.familytrack.domain.model.FamilyMember
import com.monghit.familytrack.domain.model.Location
import com.monghit.familytrack.domain.model.SafeZone
import com.monghit.familytrack.domain.model.User
import com.monghit.familytrack.domain.model.UserRole
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
                settingsRepository.setDeviceId(body.deviceId)
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

    suspend fun sendLocation(
        location: Location,
        batteryLevel: Int = -1,
        isCharging: Boolean = false
    ): Result<Boolean> {
        return try {
            val deviceToken = settingsRepository.deviceToken.first()
            val userId = settingsRepository.userId.first()

            val request = LocationUpdateRequest(
                userId = userId,
                deviceToken = deviceToken,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                timestamp = location.timestamp,
                batteryLevel = if (batteryLevel >= 0) batteryLevel else null,
                isCharging = if (batteryLevel >= 0) isCharging else null
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

    data class FamilyData(
        val members: List<FamilyMember> = emptyList(),
        val safeZones: List<SafeZone> = emptyList()
    )

    fun getFamilyLocations(): Flow<FamilyData> = flow {
        try {
            val response = apiService.getFamilyLocations()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val members = body.members.map { dto ->
                    val role = when (dto.role.lowercase()) {
                        "admin" -> UserRole.ADMIN
                        "monitor" -> UserRole.MONITOR
                        else -> UserRole.MONITORED
                    }
                    FamilyMember(
                        user = User(
                            id = dto.userId,
                            name = dto.name,
                            role = role
                        ),
                        device = if (dto.deviceId != null) {
                            Device(
                                id = dto.deviceId,
                                userId = dto.userId,
                                deviceToken = "",
                                deviceName = dto.deviceName ?: "",
                                locationInterval = 300,
                                isActive = true,
                                lastSeen = null
                            )
                        } else null,
                        lastLocation = if (dto.location != null) {
                            Location(
                                deviceId = dto.deviceId ?: 0,
                                userId = dto.userId,
                                latitude = dto.location.latitude,
                                longitude = dto.location.longitude,
                                accuracy = dto.location.accuracy ?: 0f,
                                timestamp = System.currentTimeMillis()
                            )
                        } else null,
                        isOnline = dto.isOnline,
                        batteryLevel = dto.batteryLevel,
                        isCharging = dto.isCharging
                    )
                }
                val safeZones = body.safeZones.map { dto ->
                    SafeZone(
                        id = dto.zoneId,
                        name = dto.name,
                        centerLat = dto.lat,
                        centerLng = dto.lng,
                        radiusMeters = dto.radius,
                        monitoredUserId = 0,
                        createdBy = 0
                    )
                }
                emit(FamilyData(members, safeZones))
            } else {
                Timber.e("Failed to get family locations: ${response.message()}")
                emit(FamilyData())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting family locations")
            emit(FamilyData())
        }
    }

    suspend fun sendManualNotification(fromUserId: Int, toUserId: Int): Result<Boolean> {
        return try {
            val request = ManualNotifyRequest(
                fromUserId = fromUserId,
                toUserId = toUserId
            )
            val response = apiService.sendManualNotification(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to send notification: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending manual notification")
            Result.failure(e)
        }
    }

    suspend fun createSafeZone(
        name: String,
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        monitoredUserId: Int
    ): Result<SafeZone> {
        return try {
            val createdBy = settingsRepository.userId.first()
            val request = CreateSafeZoneRequest(
                name = name,
                lat = lat,
                lng = lng,
                radiusMeters = radiusMeters,
                monitoredUserId = monitoredUserId,
                createdBy = createdBy
            )
            val response = apiService.createSafeZone(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(
                    SafeZone(
                        id = body.zoneId,
                        name = body.name,
                        centerLat = body.lat,
                        centerLng = body.lng,
                        radiusMeters = body.radiusMeters,
                        monitoredUserId = monitoredUserId,
                        createdBy = createdBy
                    )
                )
            } else {
                Result.failure(Exception("Failed to create safe zone: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating safe zone")
            Result.failure(e)
        }
    }

    suspend fun deleteSafeZone(zoneId: Int): Result<Boolean> {
        return try {
            val request = DeleteSafeZoneRequest(zoneId = zoneId)
            val response = apiService.deleteSafeZone(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete safe zone: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting safe zone")
            Result.failure(e)
        }
    }

    suspend fun createFamily(familyName: String, userName: String): Result<Triple<Int, Int, String>> {
        return try {
            val request = CreateFamilyRequest(familyName = familyName, userName = userName)
            val response = apiService.createFamily(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                settingsRepository.setUserId(body.userId)
                settingsRepository.setUserName(userName)
                settingsRepository.setFamilyId(body.familyId)
                settingsRepository.setFamilyName(body.familyName)
                settingsRepository.setInviteCode(body.inviteCode)
                settingsRepository.setUserRole(body.role)
                Result.success(Triple(body.userId, body.familyId, body.inviteCode))
            } else {
                Result.failure(Exception("Failed to create family: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating family")
            Result.failure(e)
        }
    }

    suspend fun joinFamily(inviteCode: String, userName: String): Result<Pair<Int, Int>> {
        return try {
            val request = JoinFamilyRequest(inviteCode = inviteCode, userName = userName)
            val response = apiService.joinFamily(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    settingsRepository.setUserId(body.userId)
                    settingsRepository.setUserName(userName)
                    settingsRepository.setFamilyId(body.familyId)
                    settingsRepository.setFamilyName(body.familyName)
                    settingsRepository.setUserRole(body.role)
                    Result.success(Pair(body.userId, body.familyId))
                } else {
                    Result.failure(Exception("Invalid invite code"))
                }
            } else {
                Result.failure(Exception("Invalid invite code"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error joining family")
            Result.failure(e)
        }
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
