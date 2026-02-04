package com.monghit.familytrack.domain.model

data class User(
    val id: Int,
    val name: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    ADMIN,
    MONITOR,
    MONITORED
}
