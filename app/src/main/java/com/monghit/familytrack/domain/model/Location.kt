package com.monghit.familytrack.domain.model

data class Location(
    val id: Int = 0,
    val deviceId: Int,
    val userId: Int,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val receivedAt: Long = System.currentTimeMillis()
)

data class LocationUpdate(
    val userId: Int,
    val deviceToken: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)
