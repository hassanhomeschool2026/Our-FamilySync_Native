package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeofenceZone
import com.example.ui.theme.FamilyPrimary
import com.example.ui.theme.FamilySuccess
import com.example.ui.viewmodel.FamilySyncViewModel

@Composable
fun GeofenceScreen(
    viewModel: FamilySyncViewModel,
    modifier: Modifier = Modifier
) {
    val zones by viewModel.geofenceZones.collectAsState()
    val liveLocation by viewModel.liveLocation.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var zoneName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Home") }
    var radiusMeters by remember { mutableFloatStateOf(250f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Safe Geofence Zones",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time arrival & departure alerts",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = !showAddDialog },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FamilyPrimary),
                    modifier = Modifier.testTag("add_zone_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Zone", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Native Geofence Explanation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FamilyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Native Android Geofence Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Unlike a web PWA, your phone automatically notifies your family whenever members cross these boundaries.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Add Zone Form Card
        item {
            AnimatedVisibility(visible = showAddDialog) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("add_zone_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Define Safe Zone",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = zoneName,
                            onValueChange = { zoneName = it },
                            label = { Text("Zone Name") },
                            placeholder = { Text("e.g. Grandparent's House, Soccer Field") },
                            modifier = Modifier.fillMaxWidth().testTag("zone_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Selection
                        Text(
                            text = "Category",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val categories = listOf("Home", "School", "Activity", "Work", "Other")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) FamilyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Radius Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Geofence Radius",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${radiusMeters.toInt()} meters",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FamilyPrimary
                            )
                        }
                        Slider(
                            value = radiusMeters,
                            onValueChange = { radiusMeters = it },
                            valueRange = 50f..1000f,
                            steps = 18,
                            colors = SliderDefaults.colors(thumbColor = FamilyPrimary, activeTrackColor = FamilyPrimary),
                            modifier = Modifier.fillMaxWidth().testTag("radius_slider")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (zoneName.isNotBlank()) {
                                        viewModel.addSafeZone(
                                            name = zoneName,
                                            category = selectedCategory,
                                            radiusMeters = radiusMeters,
                                            lat = liveLocation?.latitude,
                                            lng = liveLocation?.longitude
                                        )
                                        zoneName = ""
                                        showAddDialog = false
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FamilyPrimary),
                                modifier = Modifier.testTag("save_zone_button")
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Safe Zone")
                            }
                        }
                    }
                }
            }
        }

        // Active Safe Zones Count
        item {
            Text(
                text = "Configured Safe Zones (${zones.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Safe Zones List
        items(zones, key = { it.id }) { zone ->
            GeofenceZoneCard(
                zone = zone,
                onDelete = { viewModel.deleteSafeZone(zone.id) }
            )
        }
    }
}

@Composable
fun GeofenceZoneCard(
    zone: GeofenceZone,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneColor = Color(zone.colorHex)
    val icon = when (zone.category) {
        "Home" -> Icons.Default.Home
        "School" -> Icons.Default.School
        "Activity" -> Icons.Default.FitnessCenter
        "Work" -> Icons.Default.Work
        else -> Icons.Default.LocationOn
    }

    Card(
        modifier = modifier.fillMaxWidth().testTag("zone_card_${zone.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(zoneColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = zoneColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = zone.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = zoneColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = zone.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = zoneColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Radius: ${zone.radiusMeters.toInt()}m · Alerts on enter & exit",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("delete_zone_${zone.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Zone",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
