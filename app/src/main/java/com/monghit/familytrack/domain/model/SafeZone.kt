package com.monghit.familytrack.domain.model

data class SafeZone(
    val id: Int = 0,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Int,
    val monitoredUserId: Int,
    val createdBy: Int,
    val createdAt: Long = System.currentTimeMillis()
)
