package com.example.model

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

data class CheckIn(
    val id: Long = 0,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val userColor: Long,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val address: String,
    val note: String = "",
    val batteryPct: Int = 100,
    val speedMph: Float = 0f,
    val avatarPhotoUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class FamilyMember(
    val id: String,
    val name: String,
    val role: String, // "Parent", "Child", "Guardian"
    val colorHex: Long,
    val avatarInitials: String,
    val isSelf: Boolean = false,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val statusText: String, // "At Home", "At School", "In Transit", "Work"
    val batteryPct: Int = 85,
    val speedMph: Float = 0f,
    val avatarPhotoUri: String? = null,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)


data class GeofenceZone(
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val category: String, // "Home", "School", "Work", "Activity", "Other"
    val colorHex: Long,
    val notifyOnEnter: Boolean = true,
    val notifyOnExit: Boolean = true
)

data class LiveLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val altitudeMeters: Double = 0.0,
    val speedMph: Float = 0f,
    val bearing: Float = 0f,
    val address: String = "Locating...",
    val placeName: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val isGpsLocked: Boolean = true,
    val batteryPct: Int = 100
)

data class CalendarEvent(
    val id: Long = 0,
    val title: String,
    val date: String, // "2026-09-12"
    val startTime: String = "", // "14:30"
    val endTime: String = "", // "15:30"
    val description: String = "",
    val memberId: String = "self_user",
    val memberName: String = "You",
    val memberColor: Long = 0xFF2563EB
)

data class TaskItem(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "medium", // "high", "medium", "low"
    val dueDate: String = "",
    val isShoppingList: Boolean = false,
    val completed: Boolean = false,
    val assignedToId: String = "self_user",
    val assignedToName: String = "You",
    val assignedColor: Long = 0xFF2563EB,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class ChoreItem(
    val id: Long = 0,
    val title: String,
    val pointValue: Int = 5,
    val recurrence: String = "Daily", // "Daily", "Weekdays", "Weekly"
    val completed: Boolean = false,
    val completedAtMillis: Long? = null,
    val completedById: String? = null,
    val streakCount: Int = 5,
    val assignedToId: String = "self_user",
    val assignedToName: String = "You",
    val assignedColor: Long = 0xFF2563EB
)

data class FeedItem(
    val id: Long = 0,
    val type: String, // "checkin", "event_added", "task_completed", "chore_completed", "family_alert"
    val message: String,
    val userName: String,
    val userAvatar: String,
    val userColor: Long,
    val avatarPhotoUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)


data class NotificationItem(
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String = "general",
    val isRead: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

object BrandPalette {
    val Teal = 0xFF0D9488L
    val Blue = 0xFF2563EBL
    val Purple = 0xFF7C3AEDL
    val Pink = 0xFFDB2777L
    val Orange = 0xFFEA580CL
    val Red = 0xFFDC2626L
    val Green = 0xFF16A34AL
    val Amber = 0xFFD97706L
    val Sky = 0xFF0284C7L
    val Rose = 0xFFE11D48L

    val MemberColorList: List<Pair<String, Long>> = listOf(
        Pair("Teal", Teal),
        Pair("Blue", Blue),
        Pair("Purple", Purple),
        Pair("Pink", Pink),
        Pair("Orange", Orange),
        Pair("Red", Red),
        Pair("Green", Green),
        Pair("Amber", Amber),
        Pair("Sky", Sky),
        Pair("Rose", Rose)
    )

    val memberColors: List<Long> = MemberColorList.map { it.second }
}


