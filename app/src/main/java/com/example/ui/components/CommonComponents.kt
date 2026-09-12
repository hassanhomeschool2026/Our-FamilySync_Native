package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ui.theme.FamilyError
import com.example.ui.theme.FamilySuccess
import com.example.ui.theme.FamilyTertiary

@Composable
fun MemberAvatarBadge(
    initials: String,
    colorHex: Long,
    photoUri: String? = null,
    size: Dp = 44.dp,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val memberColor = Color(colorHex)
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = initials,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, memberColor, CircleShape)
            )
        } else {
            // Outer colored ring with initials
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(memberColor.copy(alpha = 0.15f))
                    .border(2.dp, memberColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = memberColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38).sp
                )
            }
        }

        // Live active indicator dot
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(size * 0.32f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(FamilySuccess)
            )
        }
    }
}

@Composable
fun TrackingStatusBadge(
    isTracking: Boolean,
    accuracyMeters: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isTracking) FamilySuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            if (isTracking) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(FamilySuccess)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (accuracyMeters > 0) "Family Tracking Active (±${accuracyMeters.toInt()}m)" else "Family Tracking Active",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FamilySuccess
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Family Tracking Paused",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun BatteryBadge(
    batteryPct: Int,
    modifier: Modifier = Modifier
) {
    val (batteryColor, icon) = when {
        batteryPct <= 20 -> Pair(FamilyError, Icons.Default.BatteryChargingFull)
        batteryPct <= 45 -> Pair(FamilyTertiary, Icons.Default.BatteryFull)
        else -> Pair(FamilySuccess, Icons.Default.BatteryFull)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(batteryColor.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Battery level",
            tint = batteryColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "$batteryPct%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = batteryColor
        )
    }
}

@Composable
fun NativeLocationPermissionCard(
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (hasPermissions) return

    Card(
        modifier = modifier.testTag("permission_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Location Access",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable Native Real-Time GPS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Allows FamilySync to keep running continuously in the background when your screen is locked.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.testTag("enable_gps_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Grant Location Permissions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun formatTimeAgo(timeMillis: Long): String {
    val diffSec = (System.currentTimeMillis() - timeMillis) / 1000
    return when {
        diffSec < 45 -> "Just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        else -> "${diffSec / 86400}d ago"
    }
}

@Composable
fun GradientStarHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    trailingBadge: @Composable (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_twinkle")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(
                        Color(0xFF01DCBA),
                        Color(0xFF0EA5E9),
                        Color(0xFF1E3A8A)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Decorative star dots
        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
            val starPositions = listOf(
                Pair(0.08f, 0.35f),
                Pair(0.24f, 0.70f),
                Pair(0.48f, 0.25f),
                Pair(0.72f, 0.75f),
                Pair(0.90f, 0.30f)
            )
            starPositions.forEach { (xFrac, yFrac) ->
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha),
                    radius = 2.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(size.width * xFrac, size.height * yFrac)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                if (trailingBadge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingBadge()
                }
            }

            if (actionText != null && onActionClick != null) {
                Text(
                    text = actionText,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onActionClick() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PriorityBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (priority.lowercase()) {
        "high" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
        "medium" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
        else -> Pair(Color(0xFFCCFBF1), Color(0xFF0D9488))
    }

    Text(
        text = priority.replaceFirstChar { it.uppercase() },
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun TopAppHeader(
    unreadNotificationCount: Int = 0,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_top_header"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("brand_logo_title")
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.fs_logo_icon),
                    contentDescription = "Our FamilySync Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = "Our FamilySync",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Native Live Location & Circle",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Notification Bell with Badge
            Box(contentAlignment = Alignment.TopEnd) {
                androidx.compose.material3.IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("notification_bell_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakBadge(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEF3C7))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = "🔥", fontSize = 12.sp)
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "$streakCount-day streak",
            color = Color(0xFFB45309),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}



