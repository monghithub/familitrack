package com.monghit.familytrack.domain.model

data class FamilyMember(
    val user: User,
    val device: Device?,
    val lastLocation: Location?,
    val isOnline: Boolean
) {
    val displayName: String get() = user.name
    val lastSeenFormatted: String get() = formatLastSeen(device?.lastSeen)

    private fun formatLastSeen(timestamp: Long?): String {
        if (timestamp == null) return "Nunca"

        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Justo ahora"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hace $hours h"
            else -> "Hace $days días"
        }
    }
}
