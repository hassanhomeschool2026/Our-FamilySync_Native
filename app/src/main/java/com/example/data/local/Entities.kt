package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CheckIn
import com.example.model.FamilyMember
import com.example.model.GeofenceZone

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val userColor: Long,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val address: String,
    val note: String,
    val batteryPct: Int,
    val speedMph: Float,
    val avatarPhotoUri: String? = null,
    val createdAtMillis: Long
) {
    fun toModel(): CheckIn = CheckIn(
        id = id,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        userColor = userColor,
        latitude = latitude,
        longitude = longitude,
        locationName = locationName,
        address = address,
        note = note,
        batteryPct = batteryPct,
        speedMph = speedMph,
        avatarPhotoUri = avatarPhotoUri,
        createdAtMillis = createdAtMillis
    )

    companion object {
        fun fromModel(model: CheckIn): CheckInEntity = CheckInEntity(
            id = model.id,
            userId = model.userId,
            userName = model.userName,
            userAvatar = model.userAvatar,
            userColor = model.userColor,
            latitude = model.latitude,
            longitude = model.longitude,
            locationName = model.locationName,
            address = model.address,
            note = model.note,
            batteryPct = model.batteryPct,
            speedMph = model.speedMph,
            avatarPhotoUri = model.avatarPhotoUri,
            createdAtMillis = model.createdAtMillis
        )
    }
}

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val colorHex: Long,
    val avatarInitials: String,
    val isSelf: Boolean,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val statusText: String,
    val batteryPct: Int,
    val speedMph: Float,
    val avatarPhotoUri: String? = null,
    val lastUpdatedMillis: Long
) {
    fun toModel(): FamilyMember = FamilyMember(
        id = id,
        name = name,
        role = role,
        colorHex = colorHex,
        avatarInitials = avatarInitials,
        isSelf = isSelf,
        latitude = latitude,
        longitude = longitude,
        address = address,
        statusText = statusText,
        batteryPct = batteryPct,
        speedMph = speedMph,
        avatarPhotoUri = avatarPhotoUri,
        lastUpdatedMillis = lastUpdatedMillis
    )

    companion object {
        fun fromModel(model: FamilyMember): FamilyMemberEntity = FamilyMemberEntity(
            id = model.id,
            name = model.name,
            role = model.role,
            colorHex = model.colorHex,
            avatarInitials = model.avatarInitials,
            isSelf = model.isSelf,
            latitude = model.latitude,
            longitude = model.longitude,
            address = model.address,
            statusText = model.statusText,
            batteryPct = model.batteryPct,
            speedMph = model.speedMph,
            avatarPhotoUri = model.avatarPhotoUri,
            lastUpdatedMillis = model.lastUpdatedMillis
        )
    }
}


@Entity(tableName = "geofence_zones")
data class GeofenceZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val category: String,
    val colorHex: Long,
    val notifyOnEnter: Boolean,
    val notifyOnExit: Boolean
) {
    fun toModel(): GeofenceZone = GeofenceZone(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        category = category,
        colorHex = colorHex,
        notifyOnEnter = notifyOnEnter,
        notifyOnExit = notifyOnExit
    )

    companion object {
        fun fromModel(model: GeofenceZone): GeofenceZoneEntity = GeofenceZoneEntity(
            id = model.id,
            name = model.name,
            latitude = model.latitude,
            longitude = model.longitude,
            radiusMeters = model.radiusMeters,
            category = model.category,
            colorHex = model.colorHex,
            notifyOnEnter = model.notifyOnEnter,
            notifyOnExit = model.notifyOnExit
        )
    }
}

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val memberId: String,
    val memberName: String,
    val memberColor: Long
) {
    fun toModel(): com.example.model.CalendarEvent = com.example.model.CalendarEvent(
        id = id,
        title = title,
        date = date,
        startTime = startTime,
        endTime = endTime,
        description = description,
        memberId = memberId,
        memberName = memberName,
        memberColor = memberColor
    )

    companion object {
        fun fromModel(model: com.example.model.CalendarEvent): CalendarEventEntity = CalendarEventEntity(
            id = model.id,
            title = model.title,
            date = model.date,
            startTime = model.startTime,
            endTime = model.endTime,
            description = model.description,
            memberId = model.memberId,
            memberName = model.memberName,
            memberColor = model.memberColor
        )
    }
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val priority: String,
    val dueDate: String,
    val isShoppingList: Boolean,
    val completed: Boolean,
    val assignedToId: String,
    val assignedToName: String,
    val assignedColor: Long,
    val createdAtMillis: Long
) {
    fun toModel(): com.example.model.TaskItem = com.example.model.TaskItem(
        id = id,
        title = title,
        description = description,
        priority = priority,
        dueDate = dueDate,
        isShoppingList = isShoppingList,
        completed = completed,
        assignedToId = assignedToId,
        assignedToName = assignedToName,
        assignedColor = assignedColor,
        createdAtMillis = createdAtMillis
    )

    companion object {
        fun fromModel(model: com.example.model.TaskItem): TaskEntity = TaskEntity(
            id = model.id,
            title = model.title,
            description = model.description,
            priority = model.priority,
            dueDate = model.dueDate,
            isShoppingList = model.isShoppingList,
            completed = model.completed,
            assignedToId = model.assignedToId,
            assignedToName = model.assignedToName,
            assignedColor = model.assignedColor,
            createdAtMillis = model.createdAtMillis
        )
    }
}

@Entity(tableName = "chores")
data class ChoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val pointValue: Int,
    val recurrence: String,
    val completed: Boolean,
    val completedAtMillis: Long?,
    val completedById: String?,
    val streakCount: Int,
    val assignedToId: String,
    val assignedToName: String,
    val assignedColor: Long
) {
    fun toModel(): com.example.model.ChoreItem = com.example.model.ChoreItem(
        id = id,
        title = title,
        pointValue = pointValue,
        recurrence = recurrence,
        completed = completed,
        completedAtMillis = completedAtMillis,
        completedById = completedById,
        streakCount = streakCount,
        assignedToId = assignedToId,
        assignedToName = assignedToName,
        assignedColor = assignedColor
    )

    companion object {
        fun fromModel(model: com.example.model.ChoreItem): ChoreEntity = ChoreEntity(
            id = model.id,
            title = model.title,
            pointValue = model.pointValue,
            recurrence = model.recurrence,
            completed = model.completed,
            completedAtMillis = model.completedAtMillis,
            completedById = model.completedById,
            streakCount = model.streakCount,
            assignedToId = model.assignedToId,
            assignedToName = model.assignedToName,
            assignedColor = model.assignedColor
        )
    }
}

@Entity(tableName = "feed_items")
data class FeedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val message: String,
    val userName: String,
    val userAvatar: String,
    val userColor: Long,
    val avatarPhotoUri: String? = null,
    val createdAtMillis: Long
) {
    fun toModel(): com.example.model.FeedItem = com.example.model.FeedItem(
        id = id,
        type = type,
        message = message,
        userName = userName,
        userAvatar = userAvatar,
        userColor = userColor,
        avatarPhotoUri = avatarPhotoUri,
        createdAtMillis = createdAtMillis
    )

    companion object {
        fun fromModel(model: com.example.model.FeedItem): FeedItemEntity = FeedItemEntity(
            id = model.id,
            type = model.type,
            message = model.message,
            userName = model.userName,
            userAvatar = model.userAvatar,
            userColor = model.userColor,
            avatarPhotoUri = model.avatarPhotoUri,
            createdAtMillis = model.createdAtMillis
        )
    }
}


@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAtMillis: Long
) {
    fun toModel(): com.example.model.NotificationItem = com.example.model.NotificationItem(
        id = id,
        title = title,
        message = message,
        type = type,
        isRead = isRead,
        createdAtMillis = createdAtMillis
    )

    companion object {
        fun fromModel(model: com.example.model.NotificationItem): NotificationEntity = NotificationEntity(
            id = model.id,
            title = model.title,
            message = model.message,
            type = model.type,
            isRead = model.isRead,
            createdAtMillis = model.createdAtMillis
        )
    }
}


