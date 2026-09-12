package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilySyncDao {
    // Check-ins
    @Query("SELECT * FROM check_ins ORDER BY createdAtMillis DESC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Query("DELETE FROM check_ins WHERE id = :id")
    suspend fun deleteCheckIn(id: Long)

    @Query("DELETE FROM check_ins")
    suspend fun clearCheckIns()

    // Family Members
    @Query("SELECT * FROM family_members ORDER BY isSelf DESC, name ASC")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMembers(members: List<FamilyMemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMember(member: FamilyMemberEntity)

    @Query("UPDATE family_members SET latitude = :lat, longitude = :lng, address = :addr, speedMph = :speed, batteryPct = :battery, lastUpdatedMillis = :time WHERE isSelf = 1")
    suspend fun updateSelfLocation(lat: Double, lng: Double, addr: String, speed: Float, battery: Int, time: Long)

    @Query("UPDATE family_members SET name = :name, colorHex = :colorHex, avatarPhotoUri = :avatarUri WHERE isSelf = 1")
    suspend fun updateSelfProfile(name: String, colorHex: Long, avatarUri: String?)

    // Geofence Zones
    @Query("SELECT * FROM geofence_zones ORDER BY id ASC")
    fun getAllZones(): Flow<List<GeofenceZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZone(zone: GeofenceZoneEntity): Long

    @Update
    suspend fun updateZone(zone: GeofenceZoneEntity)

    @Query("DELETE FROM geofence_zones WHERE id = :id")
    suspend fun deleteZone(id: Long)

    // Calendar Events
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, startTime ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    // Tasks & Shopping
    @Query("SELECT * FROM tasks ORDER BY completed ASC, createdAtMillis DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, completed: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // Chores
    @Query("SELECT * FROM chores ORDER BY completed ASC, id ASC")
    fun getAllChores(): Flow<List<ChoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChore(chore: ChoreEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChores(chores: List<ChoreEntity>)

    @Query("UPDATE chores SET completed = :completed, completedAtMillis = :completedAt WHERE id = :id")
    suspend fun updateChoreStatus(id: Long, completed: Boolean, completedAt: Long?)

    @Query("DELETE FROM chores WHERE id = :id")
    suspend fun deleteChore(id: Long)

    // Feed Items
    @Query("SELECT * FROM feed_items ORDER BY createdAtMillis DESC LIMIT 50")
    fun getAllFeedItems(): Flow<List<FeedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItem(feedItem: FeedItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItems(feedItems: List<FeedItemEntity>)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY createdAtMillis DESC LIMIT 50")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()
}

