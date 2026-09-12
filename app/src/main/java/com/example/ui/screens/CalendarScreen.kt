package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalendarEvent
import com.example.ui.components.GradientStarHeader
import com.example.ui.components.MemberAvatarBadge
import com.example.ui.theme.FamilyPrimary
import com.example.ui.viewmodel.FamilySyncViewModel

@Composable
fun CalendarScreen(
    viewModel: FamilySyncViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.calendarEvents.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()

    var selectedView by remember { mutableStateOf("Month") } // "Month", "Week", "Day"
    var selectedDayNumber by remember { mutableIntStateOf(12) }
    val currentMonthYear = "September 2026"

    var showAddEventDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gradient Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("calendar_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        GradientStarHeader(
                            title = "Family Calendar",
                            actionText = "+ Add Event",
                            onActionClick = { showAddEventDialog = true }
                        )

                        // View Switcher (Month / Week / Day)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Month selector title
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (selectedDayNumber > 1) selectedDayNumber-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                                }
                                Text(
                                    text = currentMonthYear,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = { if (selectedDayNumber < 30) selectedDayNumber++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                                }
                            }

                            // View tabs
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(2.dp)
                            ) {
                                listOf("Month", "Week", "Day").forEach { viewName ->
                                    val isSelected = selectedView == viewName
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { selectedView = viewName }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = viewName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Month Grid Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("calendar_month_grid_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Days of week header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Days 1..30 in grid
                        val daysList = (1..30).toList()
                        // Group into rows of 7
                        daysList.chunked(7).forEach { weekChunk ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                weekChunk.forEach { dayNum ->
                                    val isSelected = dayNum == selectedDayNumber
                                    val dayDateStr = "2026-09-${if (dayNum < 10) "0$dayNum" else dayNum}"
                                    val hasEvent = events.any { it.date == dayDateStr }

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else if (hasEvent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                else Color.Transparent
                                            )
                                            .clickable { selectedDayNumber = dayNum },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || hasEvent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (hasEvent && !isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(FamilyPrimary)
                                                )
                                            }
                                        }
                                    }
                                }
                                // Fill trailing empty spots in the last row
                                if (weekChunk.size < 7) {
                                    repeat(7 - weekChunk.size) {
                                        Spacer(modifier = Modifier.size(38.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Events on Selected Date Section
            item {
                val selectedDateStr = "2026-09-${if (selectedDayNumber < 10) "0$selectedDayNumber" else selectedDayNumber}"
                val dayEvents = events.filter { it.date == selectedDateStr }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Events for Sep $selectedDayNumber, 2026",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${dayEvents.size} event${if (dayEvents.size == 1) "" else "s"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            val selectedDateStr = "2026-09-${if (selectedDayNumber < 10) "0$selectedDayNumber" else selectedDayNumber}"
            val filteredEvents = events.filter { it.date == selectedDateStr }

            if (filteredEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📅", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No family events scheduled",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap '+ Add Event' to schedule practice, appointments, or gatherings.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddEventDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Add Event for Sep $selectedDayNumber", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onDelete = { viewModel.deleteCalendarEvent(event.id) }
                    )
                }
            }

            // All Upcoming Events Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All Upcoming Events (${events.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(events, key = { "all_${it.id}" }) { event ->
                EventCard(
                    event = event,
                    onDelete = { viewModel.deleteCalendarEvent(event.id) }
                )
            }
        }

        // FAB to Add Event
        FloatingActionButton(
            onClick = { showAddEventDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_event_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Event")
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        AddEventDialog(
            defaultDate = "2026-09-${if (selectedDayNumber < 10) "0$selectedDayNumber" else selectedDayNumber}",
            familyMembers = familyMembers,
            onDismiss = { showAddEventDialog = false },
            onConfirm = { title, date, start, end, desc, memId, memName, memColor ->
                viewModel.addCalendarEvent(title, date, start, end, desc, memId, memName, memColor)
                showAddEventDialog = false
            }
        )
    }
}

@Composable
fun EventCard(
    event: CalendarEvent,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val memberColor = Color(event.memberColor)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color accent pill
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(memberColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.date} · ${if (event.startTime.isNotBlank()) "${event.startTime} - ${event.endTime}" else "All Day"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (event.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = event.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Member Badge
            MemberAvatarBadge(
                initials = event.memberName.take(2).uppercase(),
                colorHex = event.memberColor,
                size = 32.dp,
                isActive = false
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Event",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddEventDialog(
    defaultDate: String,
    familyMembers: List<com.example.model.FamilyMember>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }
    var startTime by remember { mutableStateOf("10:00 AM") }
    var endTime by remember { mutableStateOf("11:30 AM") }
    var description by remember { mutableStateOf("") }

    var selectedMemberId by remember { mutableStateOf(familyMembers.firstOrNull()?.id ?: "self_user") }
    var selectedMemberName by remember { mutableStateOf(familyMembers.firstOrNull()?.name ?: "You") }
    var selectedMemberColor by remember { mutableStateOf(familyMembers.firstOrNull()?.colorHex ?: 0xFF2563EB) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Family Event", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    placeholder = { Text("e.g. Soccer Practice, Dentist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("event_title_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Location") },
                    placeholder = { Text("Field 3, bring water...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Assign Family Member:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    familyMembers.forEach { member ->
                        val isSelected = selectedMemberId == member.id
                        val memColor = Color(member.colorHex)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) memColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, memColor) else null,
                            modifier = Modifier
                                .clickable {
                                    selectedMemberId = member.id
                                    selectedMemberName = member.name
                                    selectedMemberColor = member.colorHex
                                }
                        ) {
                            Text(
                                text = member.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) memColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            date.trim(),
                            startTime.trim(),
                            endTime.trim(),
                            description.trim(),
                            selectedMemberId,
                            selectedMemberName,
                            selectedMemberColor
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_event_button")
            ) {
                Text("Save Event")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text("Cancel")
            }
        }
    )
}
