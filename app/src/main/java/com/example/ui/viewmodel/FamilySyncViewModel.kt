package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FamilyRepository
import com.example.location.LocationTrackerState
import com.example.location.LocationTrackingService
import com.example.model.CheckIn
import com.example.model.FamilyMember
import com.example.model.GeofenceZone
import com.example.model.LiveLocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilySyncViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FamilyRepository(application)

    val familyMembers: StateFlow<List<FamilyMember>> = repository.familyMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checkIns: StateFlow<List<CheckIn>> = repository.checkIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val geofenceZones: StateFlow<List<GeofenceZone>> = repository.geofenceZones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEvents: StateFlow<List<com.example.model.CalendarEvent>> = repository.calendarEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<com.example.model.TaskItem>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chores: StateFlow<List<com.example.model.ChoreItem>> = repository.chores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedItems: StateFlow<List<com.example.model.FeedItem>> = repository.feedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<com.example.model.NotificationItem>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Theme & Profile state
    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode = _themeMode.asStateFlow()

    private val _userProfilePhotoUri = MutableStateFlow<String?>(repository.getProfilePhotoPath())
    val userProfilePhotoUri = _userProfilePhotoUri.asStateFlow()

    private val _userDisplayName = MutableStateFlow(repository.getUserDisplayName())
    val userDisplayName = _userDisplayName.asStateFlow()

    private val _userMemberColor = MutableStateFlow(repository.getUserMemberColor())
    val userMemberColor = _userMemberColor.asStateFlow()

    private val _familyName = MutableStateFlow("Taylor Family")
    val familyName = _familyName.asStateFlow()

    private val _familyInviteCode = MutableStateFlow("FAMS-7892")
    val familyInviteCode = _familyInviteCode.asStateFlow()

    val isTrackingActive = LocationTrackerState.isTrackingActive

    val liveLocation = LocationTrackerState.currentLiveLocation
    val totalDistanceMeters = LocationTrackerState.totalDistanceMeters
    val recentLocations = LocationTrackerState.recentLocations
    val geofenceEvents = LocationTrackerState.geofenceEvents

    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember = _selectedMember.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage = _userFeedbackMessage.asStateFlow()

    fun selectMember(member: FamilyMember?) {
        _selectedMember.value = member
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun setThemeMode(mode: com.example.model.ThemeMode) {
        _themeMode.value = mode
        repository.setThemeMode(mode)
        _userFeedbackMessage.value = "Theme switched to ${mode.displayName}"
    }

    fun startTracking() {
        LocationTrackingService.start(getApplication())
        _userFeedbackMessage.value = "Family Tracking activated in background"
    }

    fun stopTracking() {
        LocationTrackingService.stop(getApplication())
        _userFeedbackMessage.value = "Family Tracking paused"
    }

    fun toggleTracking() {
        if (isTrackingActive.value) {
            stopTracking()
        } else {
            startTracking()
        }
    }


    fun submitCheckIn(note: String, category: String, customPlace: String = "") {
        viewModelScope.launch {
            val live = liveLocation.value
            val lat = live?.latitude ?: 32.9482
            val lng = live?.longitude ?: -96.7970
            val place = if (customPlace.isNotBlank()) customPlace else (live?.placeName ?: "Current Location")
            val addr = live?.address ?: "Dallas, TX"
            val speed = live?.speedMph ?: 0f
            val battery = live?.batteryPct ?: 94

            val checkIn = CheckIn(
                userId = "self_user",
                userName = _userDisplayName.value,
                userAvatar = _userDisplayName.value.take(2).uppercase(),
                userColor = _userMemberColor.value,
                latitude = lat,
                longitude = lng,
                locationName = place,
                address = addr,
                note = if (note.isBlank()) "Checked in from $place" else note,
                batteryPct = battery,
                speedMph = speed,
                avatarPhotoUri = _userProfilePhotoUri.value,
                createdAtMillis = System.currentTimeMillis()
            )

            repository.addCheckIn(checkIn)
            repository.updateSelfLocation(lat, lng, addr, speed, battery)
            _userFeedbackMessage.value = "Location shared with family at $place"
        }
    }


    fun deleteCheckIn(id: Long) {
        viewModelScope.launch {
            repository.deleteCheckIn(id)
            _userFeedbackMessage.value = "Check-in removed"
        }
    }

    fun addSafeZone(name: String, category: String, radiusMeters: Float, lat: Double?, lng: Double?) {
        viewModelScope.launch {
            val live = liveLocation.value
            val targetLat = lat ?: live?.latitude ?: 32.9482
            val targetLng = lng ?: live?.longitude ?: -96.7970

            val color = when (category) {
                "Home" -> 0xFF2563EB // Blue
                "School" -> 0xFFF59E0B // Amber
                "Activity" -> 0xFF10B981 // Green
                "Work" -> 0xFF8B5CF6 // Purple
                else -> 0xFFEC4899 // Pink
            }

            val zone = GeofenceZone(
                name = name,
                latitude = targetLat,
                longitude = targetLng,
                radiusMeters = radiusMeters,
                category = category,
                colorHex = color,
                notifyOnEnter = true,
                notifyOnExit = true
            )

            repository.addZone(zone)
            _userFeedbackMessage.value = "Safe zone \"$name\" created ($radiusMeters m radius)"
        }
    }

    fun deleteSafeZone(id: Long) {
        viewModelScope.launch {
            repository.deleteZone(id)
            _userFeedbackMessage.value = "Safe zone removed"
        }
    }

    fun sendSosEmergencyPing() {
        viewModelScope.launch {
            val live = liveLocation.value
            val lat = live?.latitude ?: 32.9482
            val lng = live?.longitude ?: -96.7970
            val addr = live?.address ?: "Current Location"

            val emergencyCheckIn = CheckIn(
                userId = "self_user",
                userName = "You",
                userAvatar = "ME",
                userColor = 0xFFEF4444, // Bright Red
                latitude = lat,
                longitude = lng,
                locationName = "🚨 SOS EMERGENCY ALERT",
                address = addr,
                note = "Emergency location broadcast sent to all family members!",
                batteryPct = live?.batteryPct ?: 90,
                speedMph = live?.speedMph ?: 0f,
                createdAtMillis = System.currentTimeMillis()
            )

            repository.addCheckIn(emergencyCheckIn)
            _userFeedbackMessage.value = "🚨 EMERGENCY PING BROADCASTED TO ALL FAMILY MEMBERS!"
        }
    }

    // Calendar actions
    fun addCalendarEvent(
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        memberId: String,
        memberName: String,
        memberColor: Long
    ) {
        viewModelScope.launch {
            val event = com.example.model.CalendarEvent(
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                description = description,
                memberId = memberId,
                memberName = memberName,
                memberColor = memberColor
            )
            repository.addEvent(event)
            repository.addFeedItem(
                com.example.model.FeedItem(
                    type = "event_added",
                    message = "$memberName added event: \"$title\" on $date",
                    userName = memberName,
                    userAvatar = memberName.take(2).uppercase(),
                    userColor = memberColor
                )
            )
            _userFeedbackMessage.value = "Event \"$title\" added to family calendar"
        }
    }

    fun deleteCalendarEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteEvent(id)
            _userFeedbackMessage.value = "Event removed from calendar"
        }
    }

    // Task actions
    fun addTask(
        title: String,
        description: String,
        priority: String,
        dueDate: String,
        isShoppingList: Boolean,
        assignedToId: String,
        assignedToName: String,
        assignedColor: Long
    ) {
        viewModelScope.launch {
            val task = com.example.model.TaskItem(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate,
                isShoppingList = isShoppingList,
                completed = false,
                assignedToId = assignedToId,
                assignedToName = assignedToName,
                assignedColor = assignedColor
            )
            repository.addTask(task)
            val typeStr = if (isShoppingList) "grocery item" else "task"
            _userFeedbackMessage.value = "Added $typeStr: \"$title\""
        }
    }

    fun toggleTask(task: com.example.model.TaskItem) {
        viewModelScope.launch {
            val nextState = !task.completed
            repository.setTaskCompleted(task.id, nextState)
            if (nextState) {
                repository.addFeedItem(
                    com.example.model.FeedItem(
                        type = "task_completed",
                        message = "${task.assignedToName} completed: \"${task.title}\"",
                        userName = task.assignedToName,
                        userAvatar = task.assignedToName.take(2).uppercase(),
                        userColor = task.assignedColor
                    )
                )
                _userFeedbackMessage.value = "Completed: \"${task.title}\" 🎉"
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            _userFeedbackMessage.value = "Task removed"
        }
    }

    // Chore actions
    fun addChore(
        title: String,
        pointValue: Int,
        recurrence: String,
        assignedToId: String,
        assignedToName: String,
        assignedColor: Long
    ) {
        viewModelScope.launch {
            val chore = com.example.model.ChoreItem(
                title = title,
                pointValue = pointValue,
                recurrence = recurrence,
                completed = false,
                streakCount = 1,
                assignedToId = assignedToId,
                assignedToName = assignedToName,
                assignedColor = assignedColor
            )
            repository.addChore(chore)
            _userFeedbackMessage.value = "Added chore: \"$title\" ($pointValue pts)"
        }
    }

    fun toggleChore(chore: com.example.model.ChoreItem) {
        viewModelScope.launch {
            val nextState = !chore.completed
            repository.setChoreCompleted(chore.id, nextState)
            if (nextState) {
                repository.addFeedItem(
                    com.example.model.FeedItem(
                        type = "chore_completed",
                        message = "${chore.assignedToName} finished \"${chore.title}\" (+${chore.pointValue} pts)",
                        userName = chore.assignedToName,
                        userAvatar = chore.assignedToName.take(2).uppercase(),
                        userColor = chore.assignedColor
                    )
                )
                repository.addNotification(
                    com.example.model.NotificationItem(
                        title = "Chore Complete! ⭐",
                        message = "${chore.assignedToName} finished \"${chore.title}\" and earned +${chore.pointValue} pts!",
                        type = "chore"
                    )
                )
                _userFeedbackMessage.value = "+${chore.pointValue} Points! \"${chore.title}\" completed! ⭐"
            }
        }
    }

    fun deleteChore(id: Long) {
        viewModelScope.launch {
            repository.deleteChore(id)
            _userFeedbackMessage.value = "Chore removed"
        }
    }

    // Family broadcast alert
    fun sendFamilyAlert(message: String) {
        viewModelScope.launch {
            repository.addFeedItem(
                com.example.model.FeedItem(
                    type = "family_alert",
                    message = "Family Alert from ${_userDisplayName.value}: $message",
                    userName = _userDisplayName.value,
                    userAvatar = _userDisplayName.value.take(2).uppercase(),
                    userColor = _userMemberColor.value
                )
            )
            repository.addNotification(
                com.example.model.NotificationItem(
                    title = "Family Alert 📢",
                    message = message,
                    type = "alert"
                )
            )
            _userFeedbackMessage.value = "Family broadcast sent to all members!"
        }
    }

    // User Profile
    fun updateProfile(displayName: String, colorHex: Long) {
        _userDisplayName.value = displayName
        _userMemberColor.value = colorHex
        repository.setUserDisplayName(displayName)
        repository.setUserMemberColor(colorHex)
        viewModelScope.launch {
            repository.updateSelfProfile(displayName, colorHex, _userProfilePhotoUri.value)
        }
        _userFeedbackMessage.value = "Profile updated"
    }

    fun updateProfilePhoto(uri: android.net.Uri) {
        viewModelScope.launch {
            val savedPath = repository.saveProfilePhotoFromUri(uri)
            if (savedPath != null) {
                _userProfilePhotoUri.value = savedPath
                repository.updateSelfProfile(_userDisplayName.value, _userMemberColor.value, savedPath)
                _userFeedbackMessage.value = "Profile photo updated"
            } else {
                _userFeedbackMessage.value = "Failed to update profile photo"
            }
        }
    }

    fun clearProfilePhoto() {
        viewModelScope.launch {
            repository.clearProfilePhoto()
            _userProfilePhotoUri.value = null
            repository.updateSelfProfile(_userDisplayName.value, _userMemberColor.value, null)
            _userFeedbackMessage.value = "Profile photo removed"
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }
}


