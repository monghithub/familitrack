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

// Notification
data class NotificationData(
    @SerializedName("alertType") val alertType: String,
    @SerializedName("alertId") val alertId: Int? = null,
    @SerializedName("userId") val userId: Int? = null,
    @SerializedName("zoneName") val zoneName: String? = null
)
