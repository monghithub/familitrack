package com.monghit.familytrack.domain.model

data class Alert(
    val id: Int = 0,
    val deviceId: Int,
    val userId: Int,
    val alertType: AlertType,
    val message: String,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val isAcknowledged: Boolean = false,
    val acknowledgedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AlertType {
    ZONE_EXIT,
    ZONE_ENTRY,
    OFFLINE,
    MANUAL
}
