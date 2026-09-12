package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FamilyMember
import com.example.model.GeofenceZone
import com.example.ui.components.BatteryBadge
import com.example.ui.components.MemberAvatarBadge
import com.example.ui.components.TrackingStatusBadge
import com.example.ui.components.formatTimeAgo
import com.example.ui.theme.FamilyCardDark
import com.example.ui.theme.FamilyError
import com.example.ui.theme.FamilyPrimary
import com.example.ui.theme.FamilySuccess
import com.example.ui.viewmodel.FamilySyncViewModel
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

@Composable
fun MapScreen(
    viewModel: FamilySyncViewModel,
    modifier: Modifier = Modifier
) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val geofenceZones by viewModel.geofenceZones.collectAsState()
    val isTracking by viewModel.isTrackingActive.collectAsState()
    val liveLocation by viewModel.liveLocation.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()

    // Map Viewport state (Center Lat/Lng, Zoom Scale, Pan Offset)
    var centerLat by remember { mutableStateOf(32.9490) }
    var centerLng by remember { mutableStateOf(-96.7950) }
    var zoomScale by remember { mutableFloatStateOf(38000f) } // Pixels per degree
    var showZones by remember { mutableStateOf(true) }

    // Radar pulse animation for active tracking
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFE2E8F0))) {
        // Native interactive canvas rendering map grid, zones, and member beacons
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_map_canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(12000f, 150000f)
                        val latDelta = pan.y / zoomScale
                        val lngDelta = -pan.x / (zoomScale * cos(Math.toRadians(centerLat)).toFloat())
                        centerLat += latDelta
                        centerLng += lngDelta
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            // Helper to project Lat/Lng to Screen Offset
            fun project(lat: Double, lng: Double): Offset {
                val cosLat = cos(Math.toRadians(centerLat)).toFloat()
                val x = centerX + ((lng - centerLng) * zoomScale * cosLat).toFloat()
                val y = centerY - ((lat - centerLat) * zoomScale).toFloat()
                return Offset(x, y)
            }

            // Draw clean topographical grid lines representing city blocks
            val gridColor = Color(0xFFCBD5E1)
            val step = 60f
            var gx = 0f
            while (gx < width) {
                drawLine(gridColor, Offset(gx, 0f), Offset(gx, height), strokeWidth = 1f)
                gx += step
            }
            var gy = 0f
            while (gy < height) {
                drawLine(gridColor, Offset(0f, gy), Offset(width, gy), strokeWidth = 1f)
                gy += step
            }

            // Draw major transit avenues
            drawLine(
                Color(0xFFFFFFFF),
                Offset(0f, centerY - 80f),
                Offset(width, centerY - 80f),
                strokeWidth = 14f
            )
            drawLine(
                Color(0xFFFFFFFF),
                Offset(centerX + 60f, 0f),
                Offset(centerX + 60f, height),
                strokeWidth = 14f
            )

            // Draw Geofence Safe Zones (Translucent radius circles & boundary strokes)
            if (showZones) {
                for (zone in geofenceZones) {
                    val zoneCenter = project(zone.latitude, zone.longitude)
                    // Convert radius in meters to screen pixels based on zoom
                    // Approx 1 degree latitude ~ 111,000 meters
                    val radiusPx = (zone.radiusMeters / 111000f) * zoomScale

                    // Translucent fill
                    drawCircle(
                        color = Color(zone.colorHex).copy(alpha = 0.16f),
                        radius = radiusPx,
                        center = zoneCenter
                    )
                    // Dashed boundary ring
                    drawCircle(
                        color = Color(zone.colorHex).copy(alpha = 0.7f),
                        radius = radiusPx,
                        center = zoneCenter,
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                    )
                }
            }

            // Draw Family Members Pins & Radar Beacons
            for (member in familyMembers) {
                val memberPos = project(member.latitude, member.longitude)
                val isSelected = selectedMember?.id == member.id
                val memberColor = Color(member.colorHex)

                // If live GPS active or selected, draw radar pulse ring
                if (isTracking && member.isSelf) {
                    drawCircle(
                        color = memberColor.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = memberPos
                    )
                }

                // Selection highlight ring
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = 28f,
                        center = memberPos,
                        style = Stroke(width = 6f)
                    )
                }

                // Outer beacon shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.25f),
                    radius = 20f,
                    center = memberPos + Offset(0f, 3f)
                )

                // Base node circle
                drawCircle(
                    color = memberColor,
                    radius = 18f,
                    center = memberPos
                )

                // White inner dot / status core
                drawCircle(
                    color = Color.White,
                    radius = 7f,
                    center = memberPos
                )
            }
        }

        // Top Overlay: Status Bar & Family Quick Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrackingStatusBadge(
                    isTracking = isTracking,
                    accuracyMeters = liveLocation?.accuracyMeters ?: 0f
                )

                // Quick layer toggle & zoom controls
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showZones = !showZones },
                            modifier = Modifier.size(36.dp).testTag("toggle_zones_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Toggle Geofence Zones",
                                tint = if (showZones) FamilyPrimary else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(150000f) },
                            modifier = Modifier.size(36.dp).testTag("zoom_in_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(12000f) },
                            modifier = Modifier.size(36.dp).testTag("zoom_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Member Avatars Carousel Bar
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(familyMembers) { member ->
                        val isSelected = selectedMember?.id == member.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .clickable {
                                    viewModel.selectMember(member)
                                    centerLat = member.latitude
                                    centerLng = member.longitude
                                    zoomScale = 45000f
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            MemberAvatarBadge(
                                initials = member.avatarInitials,
                                colorHex = member.colorHex,
                                photoUri = member.avatarPhotoUri,
                                size = 32.dp,
                                isActive = true
                            )

                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = member.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = member.statusText,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Buttons on Map: "Recenter on Me" & "SOS Emergency"
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            SmallFloatingActionButton(
                onClick = {
                    viewModel.sendSosEmergencyPing()
                },
                containerColor = FamilyError,
                contentColor = Color.White,
                modifier = Modifier.testTag("sos_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Send SOS Emergency Alert",
                    modifier = Modifier.size(20.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    val live = liveLocation
                    if (live != null) {
                        centerLat = live.latitude
                        centerLng = live.longitude
                    } else {
                        val self = familyMembers.firstOrNull { it.isSelf }
                        if (self != null) {
                            centerLat = self.latitude
                            centerLng = self.longitude
                        }
                    }
                    zoomScale = 52000f
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("recenter_button")
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Center on My Location"
                )
            }
        }

        // Bottom Selected Member Card
        val activeMember = selectedMember ?: familyMembers.firstOrNull { it.isSelf }
        activeMember?.let { member ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("member_detail_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MemberAvatarBadge(
                            initials = member.avatarInitials,
                            colorHex = member.colorHex,
                            size = 48.dp,
                            isActive = true
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = member.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(member.colorHex).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = member.role,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(member.colorHex),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = member.address,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        BatteryBadge(batteryPct = member.batteryPct)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (member.speedMph > 1.5f) {
                                    "${String.format(Locale.US, "%.0f", member.speedMph)} mph"
                                } else {
                                    "Stationary"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "Updated ${formatTimeAgo(member.lastUpdatedMillis)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
