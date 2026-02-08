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
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("batteryLevel") val batteryLevel: Int? = null,
    @SerializedName("isCharging") val isCharging: Boolean? = null
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
    @SerializedName("location") val location: LocationDto?,
    @SerializedName("batteryLevel") val batteryLevel: Int? = null,
    @SerializedName("isCharging") val isCharging: Boolean? = null
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

// Profile
data class UpdateProfileRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: String? = null
)

data class UpdateProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String
)

data class GetProfileRequest(
    @SerializedName("userId") val userId: Int
)

data class GetProfileResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("familyName") val familyName: String? = null,
    @SerializedName("inviteCode") val inviteCode: String? = null
)

// Quick Message
data class QuickMessageRequest(
    @SerializedName("fromUserId") val fromUserId: Int,
    @SerializedName("message") val message: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

// Emergency SOS
data class EmergencyRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class EmergencyResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("alertId") val alertId: Int? = null
)

// Location History
data class LocationHistoryResponse(
    @SerializedName("locations") val locations: List<LocationHistoryDto>
)

data class LocationHistoryDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

// Chat
data class ChatSendRequest(
    @SerializedName("fromUserId") val fromUserId: Int,
    @SerializedName("content") val content: String
)

data class ChatSendResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("messageId") val messageId: Int? = null
)

data class ChatMessagesRequest(
    @SerializedName("familyId") val familyId: Int,
    @SerializedName("limit") val limit: Int = 50
)

data class ChatMessagesResponse(
    @SerializedName("messages") val messages: List<ChatMessageDto>
)

data class ChatMessageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val userName: String,
    @SerializedName("avatar") val avatar: String? = null
)

// Photos
data class PhotoSendRequest(
    @SerializedName("fromUserId") val fromUserId: Int,
    @SerializedName("toUserId") val toUserId: Int,
    @SerializedName("imageData") val imageData: String,
    @SerializedName("caption") val caption: String? = null
)

data class PhotoSendResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("photoId") val photoId: Int? = null
)

data class PhotoListRequest(
    @SerializedName("userId") val userId: Int
)

data class PhotoListResponse(
    @SerializedName("photos") val photos: List<PhotoDto>
)

data class PhotoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fromUserId") val fromUserId: Int,
    @SerializedName("fromName") val fromName: String,
    @SerializedName("toUserId") val toUserId: Int,
    @SerializedName("caption") val caption: String? = null,
    @SerializedName("createdAt") val createdAt: String
)
