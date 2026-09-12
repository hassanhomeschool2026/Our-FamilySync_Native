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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChoreItem
import com.example.ui.components.GradientStarHeader
import com.example.ui.components.MemberAvatarBadge
import com.example.ui.components.StreakBadge
import com.example.ui.theme.FamilyPrimary
import com.example.ui.theme.FamilySuccess
import com.example.ui.viewmodel.FamilySyncViewModel

@Composable
fun ChoresScreen(
    viewModel: FamilySyncViewModel,
    modifier: Modifier = Modifier
) {
    val chores by viewModel.chores.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()

    var showAddChoreDialog by remember { mutableStateOf(false) }

    val completedCount = chores.count { it.completed }
    val totalCount = chores.size
    val totalPoints = chores.filter { it.completed }.sumOf { it.pointValue }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Gradient & Starfield
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("chores_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        GradientStarHeader(
                            title = "Chores & Family Points",
                            actionText = "+ Add Chore",
                            onActionClick = { showAddChoreDialog = true },
                            trailingBadge = {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "$totalPoints pts earned",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        )

                        // Streak and progress strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StreakBadge(streakCount = 5)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$completedCount of $totalCount completed",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Leaderboard / Star Standings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("chores_leaderboard_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Star Leaderboard ⭐",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Resets Sunday",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Compute points per member
                        val memberScores = familyMembers.map { member ->
                            val score = chores.filter { it.assignedToId == member.id && it.completed }
                                .sumOf { it.pointValue } + when (member.name) {
                                    "Leo" -> 35
                                    "Maya" -> 20
                                    else -> 15
                                }
                            Pair(member, score)
                        }.sortedByDescending { it.second }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(memberScores) { (member, score) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.width(105.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        MemberAvatarBadge(
                                            initials = member.name.take(2).uppercase(),
                                            colorHex = member.colorHex,
                                            size = 38.dp,
                                            isActive = false
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = member.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "$score pts",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Chores List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Family Chores (${chores.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (chores.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🧹", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No chores added yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Set up tasks like Walk Dog, Empty Dishwasher, or Fold Laundry to earn points.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddChoreDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Add First Chore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(chores, key = { it.id }) { chore ->
                    ChoreRowCard(
                        chore = chore,
                        onToggle = { viewModel.toggleChore(chore) },
                        onDelete = { viewModel.deleteChore(chore.id) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddChoreDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_chore_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Chore")
        }
    }

    // Add Chore Dialog
    if (showAddChoreDialog) {
        AddChoreDialog(
            familyMembers = familyMembers,
            onDismiss = { showAddChoreDialog = false },
            onConfirm = { title, points, recurrence, memId, memName, memColor ->
                viewModel.addChore(title, points, recurrence, memId, memName, memColor)
                showAddChoreDialog = false
            }
        )
    }
}

@Composable
fun ChoreRowCard(
    chore: ChoreItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chore_card_${chore.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chore.completed) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (chore.completed) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check Circle
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp).testTag("chore_toggle_${chore.id}")
            ) {
                Icon(
                    imageVector = if (chore.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (chore.completed) "Completed" else "Mark Complete",
                    tint = if (chore.completed) FamilySuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chore.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (chore.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (chore.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Points pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "+${chore.pointValue} pts",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = chore.recurrence,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Assigned Member
            MemberAvatarBadge(
                initials = chore.assignedToName.take(2).uppercase(),
                colorHex = chore.assignedColor,
                size = 32.dp,
                isActive = false
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Chore",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddChoreDialog(
    familyMembers: List<com.example.model.FamilyMember>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedPoints by remember { mutableIntStateOf(10) }
    var recurrence by remember { mutableStateOf("Daily") } // "Daily", "Weekly", "Weekdays"

    var selectedMemberId by remember { mutableStateOf(familyMembers.firstOrNull()?.id ?: "self_user") }
    var selectedMemberName by remember { mutableStateOf(familyMembers.firstOrNull()?.name ?: "You") }
    var selectedMemberColor by remember { mutableStateOf(familyMembers.firstOrNull()?.colorHex ?: 0xFF2563EB) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Family Chore", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    label = { Text("Chore Title") },
                    placeholder = { Text("e.g. Empty Dishwasher, Walk Dog") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("chore_title_input")
                )

                Text(
                    text = "Point Reward:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { pt ->
                        val isSelected = selectedPoints == pt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color(0xFFD97706) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPoints = pt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+$pt pts",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (isSelected) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "Recurrence:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Daily", "Weekdays", "Weekly").forEach { r ->
                        val isSelected = recurrence == r
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { recurrence = r }
                        ) {
                            Text(
                                text = r,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Assign to:",
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
                            selectedPoints,
                            recurrence,
                            selectedMemberId,
                            selectedMemberName,
                            selectedMemberColor
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_chore_button")
            ) {
                Text("Save Chore")
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
