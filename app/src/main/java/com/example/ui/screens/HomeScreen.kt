package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FamilyMember
import com.example.ui.components.BatteryBadge
import com.example.ui.components.GradientStarHeader
import com.example.ui.components.MemberAvatarBadge
import com.example.ui.components.NativeLocationPermissionCard
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StreakBadge
import com.example.ui.components.TrackingStatusBadge
import com.example.ui.components.formatTimeAgo
import com.example.ui.theme.FamilyError
import com.example.ui.theme.FamilyPrimary
import com.example.ui.theme.FamilySuccess
import com.example.ui.viewmodel.FamilySyncViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: FamilySyncViewModel,
    hasLocationPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToChores: () -> Unit,
    onQuickActionAddEvent: () -> Unit,
    onQuickActionAddTask: () -> Unit,
    onQuickActionSendAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val checkIns by viewModel.checkIns.collectAsState()
    val geofenceZones by viewModel.geofenceZones.collectAsState()
    val isTracking by viewModel.isTrackingActive.collectAsState()
    val liveLocation by viewModel.liveLocation.collectAsState()
    val events by viewModel.calendarEvents.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val chores by viewModel.chores.collectAsState()
    val feedItems by viewModel.feedItems.collectAsState()
    val familyName by viewModel.familyName.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }

    val choresCompleted = chores.count { it.completed }
    val choresTotal = chores.size.coerceAtLeast(1)
    val choresProgress = choresCompleted.toFloat() / choresTotal.toFloat()
    val choresPoints = chores.filter { it.completed }.sumOf { it.pointValue }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Greeting Card with Sky Gradient inspired by the PWA HomePage.jsx
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("hero_greeting_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A), // Deep Indigo
                                    Color(0xFF2563EB), // Royal Blue
                                    Color(0xFF0284C7)  // Ocean Blue
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = Color(0xFFFDE047),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "74°F · Clear skies",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }

                            TrackingStatusBadge(
                                isTracking = isTracking,
                                accuracyMeters = liveLocation?.accuracyMeters ?: 0f
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "$greeting, $familyName!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "All ${familyMembers.size} members connected & synced with native background GPS",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Quick Action Buttons on Hero
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToMap,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier.weight(1f).testTag("hero_map_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = FamilyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Map", color = FamilyPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = onNavigateToCheckIn,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.weight(1f).testTag("hero_checkin_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Permission Card (if not granted)
        item {
            NativeLocationPermissionCard(
                hasPermissions = hasLocationPermission,
                onRequestPermissions = onRequestPermissions
            )
        }

        // Family Check-ins Card with GradientStarHeader (PWA Homepage Section 1)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("home_family_checkins_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    GradientStarHeader(
                        title = "$familyName Check-ins",
                        actionText = "View Map >",
                        onActionClick = onNavigateToMap
                    )

                    // Horizontal Member Status Row
                    LazyRow(
                        contentPadding = PaddingValues(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(familyMembers, key = { it.id }) { member ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .width(130.dp)
                                    .clickable {
                                        viewModel.selectMember(member)
                                        onNavigateToMap()
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    MemberAvatarBadge(
                                        initials = member.avatarInitials,
                                        colorHex = member.colorHex,
                                        photoUri = member.avatarPhotoUri,
                                        size = 40.dp,
                                        isActive = true
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = member.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = member.statusText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(member.colorHex),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    BatteryBadge(batteryPct = member.batteryPct)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Events Card (PWA Section 2A)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("home_events_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    GradientStarHeader(
                        title = "Today's Events",
                        actionText = "View Calendar >",
                        onActionClick = onNavigateToCalendar,
                        trailingBadge = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "${events.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    )

                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (events.isEmpty()) {
                            Text(
                                text = "No events scheduled for today",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            events.take(3).forEach { event ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(event.memberColor))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = event.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = event.startTime.ifBlank { "All Day" },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tasks Due Card (PWA Section 2B)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("home_tasks_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    val pendingTasks = tasks.filter { !it.completed && !it.isShoppingList }
                    GradientStarHeader(
                        title = "Tasks Due",
                        actionText = "Go to To-Do >",
                        onActionClick = onNavigateToTodo,
                        trailingBadge = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "${pendingTasks.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    )

                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (pendingTasks.isEmpty()) {
                            Text(
                                text = "All tasks completed! Great job!",
                                fontSize = 12.sp,
                                color = FamilySuccess
                            )
                        } else {
                            pendingTasks.take(3).forEach { task ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    PriorityBadge(priority = task.priority)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = task.assignedToName,
                                        fontSize = 11.sp,
                                        color = Color(task.assignedColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chore Progress Card (PWA Section 3)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("home_chores_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    GradientStarHeader(
                        title = "Chore Progress",
                        actionText = "View Chores >",
                        onActionClick = onNavigateToChores
                    )

                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StreakBadge(streakCount = 5)

                            Text(
                                text = "$choresCompleted/$choresTotal done (+$choresPoints pts)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { choresProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = FamilyPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Top Helper Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7).copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⭐", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Top Helper This Week: Leo (60 pts)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "Quick Family Actions",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add Event
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onQuickActionAddEvent() }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = FamilyPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add Event", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Add Task
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onQuickActionAddTask() }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null, tint = Color(0xFF0D9488))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add Task", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Check In
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToCheckIn() }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AddLocation, contentDescription = null, tint = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Check In", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Send Alert
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onQuickActionSendAlert() }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Family Alert", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Recent Family Feed Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Recent Activity Feed",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (feedItems.isEmpty()) {
            item {
                Text(
                    text = "No recent family activity yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(feedItems.take(5), key = { it.id }) { item ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MemberAvatarBadge(
                            initials = item.userAvatar,
                            colorHex = item.userColor,
                            photoUri = item.avatarPhotoUri,
                            size = 34.dp,
                            isActive = false
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.message,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatTimeAgo(item.createdAtMillis),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
