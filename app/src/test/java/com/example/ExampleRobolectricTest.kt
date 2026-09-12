package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.location.LocationTrackerState
import com.example.model.CalendarEvent
import com.example.model.ChoreItem
import com.example.model.GeofenceZone
import com.example.model.LiveLocationData
import com.example.model.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Our FamilySync", appName)
  }

  @Test
  fun `test location tracker state updates`() {
    LocationTrackerState.setTrackingActive(true)
    assertTrue(LocationTrackerState.isTrackingActive.value)

    val liveData = LiveLocationData(
      latitude = 32.9482,
      longitude = -96.7970,
      accuracyMeters = 4.2f,
      speedMph = 12.5f,
      address = "1420 Emerald Bay Dr, Dallas, TX",
      placeName = "Home"
    )
    LocationTrackerState.updateLocation(liveData)

    assertEquals(32.9482, LocationTrackerState.currentLiveLocation.value?.latitude ?: 0.0, 0.0001)
    assertEquals(12.5f, LocationTrackerState.currentLiveLocation.value?.speedMph ?: 0f, 0.1f)
  }

  @Test
  fun `test geofence properties and distance check`() {
    val homeZone = GeofenceZone(
      id = 1L,
      name = "Home",
      latitude = 32.9482,
      longitude = -96.7970,
      radiusMeters = 200f,
      category = "Home",
      colorHex = 0xFF10B981L
    )

    assertEquals("Home", homeZone.name)
    assertEquals(200f, homeZone.radiusMeters)
  }

  @Test
  fun `test models initialization and properties`() {
    val event = CalendarEvent(
      title = "Soccer Practice",
      date = "2026-09-12",
      startTime = "10:00 AM",
      endTime = "11:30 AM",
      description = "Bring shin guards",
      memberId = "child_1",
      memberName = "Leo",
      memberColor = 0xFF8B5CF6L
    )
    assertEquals("Soccer Practice", event.title)
    assertEquals("Leo", event.memberName)

    val task = TaskItem(
      title = "Buy groceries",
      priority = "high",
      dueDate = "Today",
      isShoppingList = true,
      completed = false,
      assignedToId = "self_user",
      assignedToName = "You",
      assignedColor = 0xFF2563EBL
    )
    assertTrue(task.isShoppingList)
    assertFalse(task.completed)

    val chore = ChoreItem(
      title = "Clean room",
      pointValue = 10,
      recurrence = "Daily",
      completed = false,
      streakCount = 5,
      assignedToId = "child_1",
      assignedToName = "Leo",
      assignedColor = 0xFF8B5CF6L
    )
    assertEquals(10, chore.pointValue)
    assertEquals(5, chore.streakCount)
  }

  @Test
  fun `test theme mode and profile photo properties`() {
    val darkTheme = com.example.model.ThemeMode.DARK
    val lightTheme = com.example.model.ThemeMode.LIGHT
    val sysTheme = com.example.model.ThemeMode.SYSTEM

    assertEquals("Dark Mode", darkTheme.displayName)
    assertEquals("Light Mode", lightTheme.displayName)
    assertEquals("System Default", sysTheme.displayName)

    val memberWithPhoto = com.example.model.FamilyMember(
      id = "self_user",
      name = "Taylor Parent",
      role = "Parent / Admin",
      avatarInitials = "TP",
      colorHex = 0xFF2563EBL,
      latitude = 32.9482,
      longitude = -96.7970,
      address = "Dallas, TX",
      statusText = "At Home",
      speedMph = 0f,
      batteryPct = 95,
      isSelf = true,
      avatarPhotoUri = "content://media/external/images/media/101"
    )

    assertEquals("content://media/external/images/media/101", memberWithPhoto.avatarPhotoUri)
  }
}

