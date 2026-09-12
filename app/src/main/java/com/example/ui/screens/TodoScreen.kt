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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.example.model.TaskItem
import com.example.ui.components.GradientStarHeader
import com.example.ui.components.MemberAvatarBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.FamilyPrimary
import com.example.ui.viewmodel.FamilySyncViewModel

@Composable
fun TodoScreen(
    viewModel: FamilySyncViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Tasks, 1: My Tasks, 2: Shopping List
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = when (selectedTab) {
        0 -> tasks.filter { !it.isShoppingList }
        1 -> tasks.filter { !it.isShoppingList && it.assignedToId == "self_user" }
        else -> tasks.filter { it.isShoppingList }
    }

    val completedCount = filteredTasks.count { it.completed }
    val totalCount = filteredTasks.size

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card with Gradient & Starfield
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("todo_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        GradientStarHeader(
                            title = if (selectedTab == 2) "Family Shopping List" else "Family To-Do List",
                            actionText = "+ Add Item",
                            onActionClick = { showAddTaskDialog = true },
                            trailingBadge = {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "$completedCount/$totalCount done",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        )

                        // Segmented Tab Filter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(3.dp)
                        ) {
                            listOf("All Tasks", "My Tasks", "Shopping List").forEachIndexed { index, tabTitle ->
                                val isSelected = selectedTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                        .clickable { selectedTab = index }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (index == 2) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = tabTitle,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
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
                            Text(text = if (selectedTab == 2) "🛒" else "✅", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (selectedTab == 2) "Shopping list is empty" else "All caught up!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedTab == 2) "Add groceries or household items for the family." else "No pending tasks here. Great job keeping sync!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddTaskDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (selectedTab == 2) "Add Groceries" else "Add New Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskRowCard(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_task_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            isShoppingDefault = selectedTab == 2,
            familyMembers = familyMembers,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, prio, due, isShop, memId, memName, memColor ->
                viewModel.addTask(title, desc, prio, due, isShop, memId, memName, memColor)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskRowCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_row_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.completed) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = FamilyPrimary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!task.isShoppingList) {
                        PriorityBadge(priority = task.priority)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (task.dueDate.isNotBlank()) {
                        Text(
                            text = "Due ${task.dueDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Assigned Member
            MemberAvatarBadge(
                initials = task.assignedToName.take(2).uppercase(),
                colorHex = task.assignedColor,
                size = 30.dp,
                isActive = false
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    isShoppingDefault: Boolean,
    familyMembers: List<com.example.model.FamilyMember>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Boolean, String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") } // "high", "medium", "low"
    var dueDate by remember { mutableStateOf("Today") }
    var isShoppingList by remember { mutableStateOf(isShoppingDefault) }

    var selectedMemberId by remember { mutableStateOf(familyMembers.firstOrNull()?.id ?: "self_user") }
    var selectedMemberName by remember { mutableStateOf(familyMembers.firstOrNull()?.name ?: "You") }
    var selectedMemberColor by remember { mutableStateOf(familyMembers.firstOrNull()?.colorHex ?: 0xFF2563EB) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isShoppingList) "Add to Shopping List" else "Add Family Task",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
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
                    label = { Text(if (isShoppingList) "Item name" else "Task title") },
                    placeholder = { Text(if (isShoppingList) "e.g. Milk, Apples, Paper Towels" else "e.g. Sign permission slip") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Details, brand, size...") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isShoppingList) {
                    Text(
                        text = "Priority:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("low", "medium", "high").forEach { p ->
                            val isSelected = priority == p
                            val (bg, fg) = when (p) {
                                "high" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
                                "medium" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                                else -> Pair(Color(0xFFCCFBF1), Color(0xFF0D9488))
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) bg else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { priority = p }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.replaceFirstChar { it.uppercase() },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (isSelected) fg else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Toggle between task and grocery item
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isShoppingList = !isShoppingList }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = isShoppingList, onCheckedChange = { isShoppingList = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Mark as Shopping List item", fontSize = 13.sp)
                }

                // Member Assignee
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
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
                            description.trim(),
                            priority,
                            dueDate.trim(),
                            isShoppingList,
                            selectedMemberId,
                            selectedMemberName,
                            selectedMemberColor
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_task_button")
            ) {
                Text("Save")
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
