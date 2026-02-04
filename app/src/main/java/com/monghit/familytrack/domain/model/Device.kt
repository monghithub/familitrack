package com.monghit.familytrack.domain.model

data class Device(
    val id: Int,
    val userId: Int,
    val deviceToken: String,
    val deviceName: String,
    val platform: String = "android",
    val locationInterval: Int = 300,
    val isActive: Boolean = true,
    val lastSeen: Long? = null,
    val registeredAt: Long = System.currentTimeMillis()
)
