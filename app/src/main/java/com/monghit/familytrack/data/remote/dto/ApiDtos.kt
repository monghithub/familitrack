package com.monghit.familytrack.data.remote.dto

import com.google.gson.annotations.SerializedName

// Register Device
data class RegisterDeviceRequest(
    @SerializedName("deviceToken") val deviceToken: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("deviceName") val deviceName: String
)

data class RegisterDeviceResponse(
    @SerializedName("status") val status: String,
    @SerializedName("deviceId") val deviceId: Int,
    @SerializedName("locationInterval") val locationInterval: Int,
    @SerializedName("message") val message: String
)

// Location Update
data class LocationUpdateRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("deviceToken") val deviceToken: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float,
    @SerializedName("timestamp") val timestamp: Long
)

data class LocationUpdateResponse(
    @SerializedName("status") val status: String,
    @SerializedName("alert") val alert: Boolean = false
)

// Config Update
data class ConfigUpdateRequest(
    @SerializedName("deviceToken") val deviceToken: String,
    @SerializedName("intervalSeconds") val intervalSeconds: Int
)

data class ConfigUpdateResponse(
    @SerializedName("status") val status: String,
    @SerializedName("newInterval") val newInterval: Int
)

// Manual Notification
data class ManualNotifyRequest(
    @SerializedName("fromUserId") val fromUserId: Int,
    @SerializedName("toUserId") val toUserId: Int,
    @SerializedName("type") val type: String = "manual"
)

// Notification
data class NotificationData(
    @SerializedName("alertType") val alertType: String,
    @SerializedName("alertId") val alertId: Int? = null,
    @SerializedName("userId") val userId: Int? = null,
    @SerializedName("zoneName") val zoneName: String? = null
)

// Family Locations
data class FamilyLocationsResponse(
    @SerializedName("members") val members: List<FamilyMemberDto>,
    @SerializedName("safeZones") val safeZones: List<SafeZoneDto> = emptyList()
)

data class SafeZoneDto(
    @SerializedName("zoneId") val zoneId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("radius") val radius: Int
)

data class FamilyMemberDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String,
    @SerializedName("deviceId") val deviceId: Int?,
    @SerializedName("deviceName") val deviceName: String?,
    @SerializedName("isOnline") val isOnline: Boolean,
    @SerializedName("lastSeenFormatted") val lastSeenFormatted: String,
    @SerializedName("location") val location: LocationDto?
)

data class LocationDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float?,
    @SerializedName("timestamp") val timestamp: String?
)

// Safe Zones CRUD
data class CreateSafeZoneRequest(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("radiusMeters") val radiusMeters: Int,
    @SerializedName("monitoredUserId") val monitoredUserId: Int,
    @SerializedName("createdBy") val createdBy: Int
)

data class CreateSafeZoneResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("zoneId") val zoneId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("radiusMeters") val radiusMeters: Int
)

data class DeleteSafeZoneRequest(
    @SerializedName("zoneId") val zoneId: Int
)

data class DeleteSafeZoneResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("zoneId") val zoneId: Int,
    @SerializedName("name") val name: String
)

// Family Registration
data class CreateFamilyRequest(
    @SerializedName("familyName") val familyName: String,
    @SerializedName("userName") val userName: String
)

data class CreateFamilyResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("userId") val userId: Int,
    @SerializedName("familyId") val familyId: Int,
    @SerializedName("familyName") val familyName: String,
    @SerializedName("inviteCode") val inviteCode: String,
    @SerializedName("role") val role: String
)

data class JoinFamilyRequest(
    @SerializedName("inviteCode") val inviteCode: String,
    @SerializedName("userName") val userName: String
)

data class JoinFamilyResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("userId") val userId: Int,
    @SerializedName("familyId") val familyId: Int,
    @SerializedName("familyName") val familyName: String,
    @SerializedName("role") val role: String
)
