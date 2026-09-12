package com.example.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.FamilyRepository
import com.example.model.GeofenceZone
import com.example.model.LiveLocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var repository: FamilyRepository
    private lateinit var notificationManager: NotificationManager

    private var currentBatteryPct = 100
    private var cachedZones: List<GeofenceZone> = emptyList()
    private val insideZonesIds = mutableSetOf<Long>()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    currentBatteryPct = (level * 100) / scale
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = FamilyRepository(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Observe safe zones to track entry/exit
        serviceScope.launch {
            repository.geofenceZones.collect { zones ->
                cachedZones = zones
            }
        }

        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_TRACKING) {
            stopTracking()
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildForegroundNotification("Acquiring GPS location..."))
        startLocationUpdates()
        LocationTrackerState.setTrackingActive(true)

        return START_STICKY
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val loc = locationResult.lastLocation ?: return
                handleNewLocation(loc)
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(1.0f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun handleNewLocation(loc: Location) {
        val speedMph = if (loc.hasSpeed()) (loc.speed * 2.23694f) else 0f
        val accuracy = if (loc.hasAccuracy()) loc.accuracy else 5.0f
        val altitude = if (loc.hasAltitude()) loc.altitude else 0.0
        val bearing = if (loc.hasBearing()) loc.bearing else 0f

        serviceScope.launch {
            // Reverse geocode in background
            val (placeName, fullAddress) = repository.reverseGeocode(loc.latitude, loc.longitude)

            val liveData = LiveLocationData(
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = accuracy,
                altitudeMeters = altitude,
                speedMph = speedMph,
                bearing = bearing,
                address = fullAddress,
                placeName = placeName,
                timestampMillis = loc.time,
                isGpsLocked = accuracy < 20f,
                batteryPct = currentBatteryPct
            )

            LocationTrackerState.updateLocation(liveData)

            // Update self location in Room DB
            repository.updateSelfLocation(
                lat = loc.latitude,
                lng = loc.longitude,
                address = fullAddress,
                speedMph = speedMph,
                batteryPct = currentBatteryPct
            )

            // Update ongoing notification with current address and speed
            val summaryText = if (speedMph > 2f) {
                "Moving at ${String.format(java.util.Locale.US, "%.0f", speedMph)} mph · $placeName"
            } else {
                "At $placeName (Accuracy: ±${accuracy.toInt()}m)"
            }
            notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification(summaryText))

            // Check Geofence Boundaries
            checkGeofenceBoundaries(loc.latitude, loc.longitude)
        }
    }

    private fun checkGeofenceBoundaries(lat: Double, lng: Double) {
        val results = FloatArray(1)
        for (zone in cachedZones) {
            Location.distanceBetween(lat, lng, zone.latitude, zone.longitude, results)
            val distanceMeters = results[0]
            val isInside = distanceMeters <= zone.radiusMeters
            val wasInside = insideZonesIds.contains(zone.id)

            if (isInside && !wasInside) {
                // Just entered zone!
                insideZonesIds.add(zone.id)
                if (zone.notifyOnEnter) {
                    val alert = "Entered safe zone: ${zone.name}"
                    LocationTrackerState.emitGeofenceAlert(alert)
                    sendZoneAlertNotification("Safe Zone Alert", "You arrived at ${zone.name}")
                }
            } else if (!isInside && wasInside) {
                // Just left zone!
                insideZonesIds.remove(zone.id)
                if (zone.notifyOnExit) {
                    val alert = "Departed safe zone: ${zone.name}"
                    LocationTrackerState.emitGeofenceAlert(alert)
                    sendZoneAlertNotification("Safe Zone Alert", "You left ${zone.name}")
                }
            }
        }
    }

    private fun sendZoneAlertNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt() + 100, notification)
    }

    private fun buildForegroundNotification(contentText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val launchPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Our FamilySync · Family Tracking Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(launchPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Pause Family Tracking", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Family Tracking (Life360 Live Location)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs background Family Tracking with foreground notification to keep GPS active and monitor safe geofence zones."
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun stopTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
        LocationTrackerState.setTrackingActive(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "family_location_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_TRACKING = "com.example.action.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.example.action.STOP_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }
}
