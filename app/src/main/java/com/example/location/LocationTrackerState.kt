package com.example.location

import com.example.model.LiveLocationData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object LocationTrackerState {
    private val _isTrackingActive = MutableStateFlow(false)
    val isTrackingActive = _isTrackingActive.asStateFlow()

    private val _currentLiveLocation = MutableStateFlow<LiveLocationData?>(null)
    val currentLiveLocation = _currentLiveLocation.asStateFlow()

    private val _totalDistanceMeters = MutableStateFlow(0f)
    val totalDistanceMeters = _totalDistanceMeters.asStateFlow()

    private val _recentLocations = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val recentLocations = _recentLocations.asStateFlow()

    private val _geofenceEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val geofenceEvents = _geofenceEvents.asSharedFlow()

    fun setTrackingActive(active: Boolean) {
        _isTrackingActive.value = active
    }

    fun updateLocation(data: LiveLocationData) {
        val prev = _currentLiveLocation.value
        _currentLiveLocation.value = data

        if (prev != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                prev.latitude, prev.longitude,
                data.latitude, data.longitude,
                results
            )
            val dist = results[0]
            if (dist in 1.0..1000.0) { // filter out GPS jumps
                _totalDistanceMeters.value += dist
            }
        }

        val updatedList = (_recentLocations.value + Pair(data.latitude, data.longitude)).takeLast(50)
        _recentLocations.value = updatedList
    }

    fun emitGeofenceAlert(message: String) {
        _geofenceEvents.tryEmit(message)
    }

    fun resetDistance() {
        _totalDistanceMeters.value = 0f
        _recentLocations.value = emptyList()
    }
}
