package com.example.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.data.local.CheckInEntity
import com.example.data.local.FamilyMemberEntity
import com.example.data.local.FamilySyncDao
import com.example.data.local.FamilySyncDatabase
import com.example.data.local.GeofenceZoneEntity
import com.example.model.CheckIn
import com.example.model.FamilyMember
import com.example.model.GeofenceZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class FamilyRepository(private val context: Context) {
    private val dao: FamilySyncDao = FamilySyncDatabase.getDatabase(context).familySyncDao()

    val checkIns: Flow<List<CheckIn>> = dao.getAllCheckIns().map { list ->
        list.map { it.toModel() }
    }

    val familyMembers: Flow<List<FamilyMember>> = dao.getAllMembers().map { list ->
        list.map { it.toModel() }
    }

    val geofenceZones: Flow<List<GeofenceZone>> = dao.getAllZones().map { list ->
        list.map { it.toModel() }
    }

    val calendarEvents: Flow<List<com.example.model.CalendarEvent>> = dao.getAllEvents().map { list ->
        list.map { it.toModel() }
    }

    val tasks: Flow<List<com.example.model.TaskItem>> = dao.getAllTasks().map { list ->
        list.map { it.toModel() }
    }

    val chores: Flow<List<com.example.model.ChoreItem>> = dao.getAllChores().map { list ->
        list.map { it.toModel() }
    }

    val feedItems: Flow<List<com.example.model.FeedItem>> = dao.getAllFeedItems().map { list ->
        list.map { it.toModel() }
    }

    val notifications: Flow<List<com.example.model.NotificationItem>> = dao.getAllNotifications().map { list ->
        list.map { it.toModel() }
    }

    // Events
    suspend fun addEvent(event: com.example.model.CalendarEvent): Long = withContext(Dispatchers.IO) {
        dao.insertEvent(com.example.data.local.CalendarEventEntity.fromModel(event))
    }

    suspend fun deleteEvent(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteEvent(id)
    }

    // Tasks
    suspend fun addTask(task: com.example.model.TaskItem): Long = withContext(Dispatchers.IO) {
        dao.insertTask(com.example.data.local.TaskEntity.fromModel(task))
    }

    suspend fun setTaskCompleted(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        dao.updateTaskStatus(id, completed)
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTask(id)
    }

    // Chores
    suspend fun addChore(chore: com.example.model.ChoreItem): Long = withContext(Dispatchers.IO) {
        dao.insertChore(com.example.data.local.ChoreEntity.fromModel(chore))
    }

    suspend fun setChoreCompleted(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        dao.updateChoreStatus(id, completed, completedAt)
    }

    suspend fun deleteChore(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteChore(id)
    }

    // Feed
    suspend fun addFeedItem(feedItem: com.example.model.FeedItem): Long = withContext(Dispatchers.IO) {
        dao.insertFeedItem(com.example.data.local.FeedItemEntity.fromModel(feedItem))
    }

    // Notifications
    suspend fun addNotification(notification: com.example.model.NotificationItem): Long = withContext(Dispatchers.IO) {
        dao.insertNotification(com.example.data.local.NotificationEntity.fromModel(notification))
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        dao.markAllNotificationsRead()
    }


    suspend fun addCheckIn(checkIn: CheckIn): Long = withContext(Dispatchers.IO) {
        dao.insertCheckIn(CheckInEntity.fromModel(checkIn))
    }

    suspend fun deleteCheckIn(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteCheckIn(id)
    }

    suspend fun addZone(zone: GeofenceZone): Long = withContext(Dispatchers.IO) {
        dao.insertZone(GeofenceZoneEntity.fromModel(zone))
    }

    suspend fun updateZone(zone: GeofenceZone) = withContext(Dispatchers.IO) {
        dao.updateZone(GeofenceZoneEntity.fromModel(zone))
    }

    suspend fun deleteZone(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteZone(id)
    }

    suspend fun updateSelfLocation(lat: Double, lng: Double, address: String, speedMph: Float, batteryPct: Int) = withContext(Dispatchers.IO) {
        dao.updateSelfLocation(lat, lng, address, speedMph, batteryPct, System.currentTimeMillis())
    }

    suspend fun updateSelfProfile(name: String, colorHex: Long, avatarUri: String?) = withContext(Dispatchers.IO) {
        dao.updateSelfProfile(name, colorHex, avatarUri)
    }

    suspend fun updateMemberStatus(member: FamilyMember) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateMember(FamilyMemberEntity.fromModel(member))
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (addresses.isNotEmpty()) {
                                val addr = addresses[0]
                                val place = addr.featureName ?: addr.thoroughfare ?: "Current Location"
                                val fullAddr = addr.getAddressLine(0) ?: "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
                                continuation.resume(Pair(place, fullAddr))
                            } else {
                                continuation.resume(formatFallbackLocation(latitude, longitude))
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(formatFallbackLocation(latitude, longitude))
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val place = addr.featureName ?: addr.thoroughfare ?: "Current Location"
                    val fullAddr = addr.getAddressLine(0) ?: "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
                    Pair(place, fullAddr)
                } else {
                    formatFallbackLocation(latitude, longitude)
                }
            }
        } catch (e: Exception) {
            formatFallbackLocation(latitude, longitude)
        }
    }

    private fun formatFallbackLocation(lat: Double, lng: Double): Pair<String, String> {
        val coordStr = "${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)}"
        return Pair("Coordinates", coordStr)
    }

    // App Preferences (Theme, Profile Photo, Profile Name, Color)
    private val prefs = context.getSharedPreferences("family_sync_prefs", Context.MODE_PRIVATE)

    fun getThemeMode(): com.example.model.ThemeMode {
        val name = prefs.getString("pref_theme_mode", com.example.model.ThemeMode.SYSTEM.name)
        return try {
            com.example.model.ThemeMode.valueOf(name ?: com.example.model.ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            com.example.model.ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: com.example.model.ThemeMode) {
        prefs.edit().putString("pref_theme_mode", mode.name).apply()
    }

    fun getProfilePhotoPath(): String? {
        val path = prefs.getString("pref_profile_photo_path", null)
        if (path != null && java.io.File(path).exists()) {
            return path
        }
        return null
    }

    suspend fun saveProfilePhotoFromUri(uri: android.net.Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val file = java.io.File(context.filesDir, "user_profile_photo.jpg")
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            val absolutePath = file.absolutePath
            prefs.edit().putString("pref_profile_photo_path", absolutePath).apply()
            absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearProfilePhoto() = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(context.filesDir, "user_profile_photo.jpg")
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        prefs.edit().remove("pref_profile_photo_path").apply()
    }

    fun getUserDisplayName(): String {
        return prefs.getString("pref_user_display_name", "Taylor Parent") ?: "Taylor Parent"
    }

    fun setUserDisplayName(name: String) {
        prefs.edit().putString("pref_user_display_name", name).apply()
    }

    fun getUserMemberColor(): Long {
        return prefs.getLong("pref_user_member_color", 0xFF2563EBL)
    }

    fun setUserMemberColor(color: Long) {
        prefs.edit().putLong("pref_user_member_color", color).apply()
    }
}

