package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TopAppHeader
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CheckInScreen
import com.example.ui.screens.ChoresScreen
import com.example.ui.screens.GeofenceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.NotificationsDialog
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.TodoScreen
import com.example.ui.theme.FamilyPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FamilySyncViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FamilySyncViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                FamilySyncApp(viewModel = viewModel)
            }
        }
    }
}


@Composable
fun FamilySyncApp(
    viewModel: FamilySyncViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val userFeedback by viewModel.userFeedbackMessage.collectAsState()
    val geofenceEvent by viewModel.geofenceEvents.collectAsState(initial = null)
    val notifications by viewModel.notifications.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showGeofenceScreen by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { !it.isRead }

    // Check location permission
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher for fine location and notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            hasLocationPermission = true
            viewModel.startTracking()
        }
    }

    fun requestPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    // Show feedback snackbars
    LaunchedEffect(userFeedback) {
        userFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(geofenceEvent) {
        geofenceEvent?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Prompt location permissions on first launch if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            requestPermissions()
        } else {
            // Auto start live background tracking
            viewModel.startTracking()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppHeader(
                unreadNotificationCount = unreadCount,
                onNotificationsClick = { showNotificationsDialog = true }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                // Tab 0: Home
                NavigationBarItem(
                    selected = selectedTab == 0 && !showGeofenceScreen,
                    onClick = {
                        selectedTab = 0
                        showGeofenceScreen = false
                    },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = if (selectedTab == 0 && !showGeofenceScreen) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FamilyPrimary, selectedTextColor = FamilyPrimary),
                    modifier = Modifier.testTag("nav_home")
                )

                // Tab 1: Calendar
                NavigationBarItem(
                    selected = selectedTab == 1 && !showGeofenceScreen,
                    onClick = {
                        selectedTab = 1
                        showGeofenceScreen = false
                    },
                    icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar", fontSize = 11.sp, fontWeight = if (selectedTab == 1 && !showGeofenceScreen) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FamilyPrimary, selectedTextColor = FamilyPrimary),
                    modifier = Modifier.testTag("nav_calendar")
                )

                // Tab 2: To-Do
                NavigationBarItem(
                    selected = selectedTab == 2 && !showGeofenceScreen,
                    onClick = {
                        selectedTab = 2
                        showGeofenceScreen = false
                    },
                    icon = { Icon(imageVector = Icons.Default.Checklist, contentDescription = "To-Do") },
                    label = { Text("To-Do", fontSize = 11.sp, fontWeight = if (selectedTab == 2 && !showGeofenceScreen) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FamilyPrimary, selectedTextColor = FamilyPrimary),
                    modifier = Modifier.testTag("nav_todo")
                )

                // Tab 3: Chores
                NavigationBarItem(
                    selected = selectedTab == 3 && !showGeofenceScreen,
                    onClick = {
                        selectedTab = 3
                        showGeofenceScreen = false
                    },
                    icon = { Icon(imageVector = Icons.Default.CleaningServices, contentDescription = "Chores") },
                    label = { Text("Chores", fontSize = 11.sp, fontWeight = if (selectedTab == 3 && !showGeofenceScreen) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FamilyPrimary, selectedTextColor = FamilyPrimary),
                    modifier = Modifier.testTag("nav_chores")
                )

                // Tab 4: Live Map & GPS
                NavigationBarItem(
                    selected = selectedTab == 4 && !showGeofenceScreen,
                    onClick = {
                        selectedTab = 4
                        showGeofenceScreen = false
                    },
                    icon = { Icon(imageVector = Icons.Default.Map, contentDescription = "Map") },
                    label = { Text("Live Map", fontSize = 11.sp, fontWeight = if (selectedTab == 4 && !showGeofenceScreen) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FamilyPrimary, selectedTextColor = FamilyPrimary),
                    modifier = Modifier.testTag("nav_map")
                )

                // Tab 5: Profile & Settings
                NavigationBarItem(
                    selected = selectedTab == 5 || showGeofenceScreen,
                    onClick = {
                        selectedTab = 5
                        showGeofenceScreen = false
                    },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = if (selectedTab == 5 || showGeofenceScreen) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FamilyPrimary, selectedTextColor = FamilyPrimary),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (showGeofenceScreen) {
                GeofenceScreen(
                    viewModel = viewModel
                )
            } else {
                when (selectedTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        hasLocationPermission = hasLocationPermission,
                        onRequestPermissions = { requestPermissions() },
                        onNavigateToMap = { selectedTab = 4 },
                        onNavigateToCheckIn = { selectedTab = 4 },
                        onNavigateToCalendar = { selectedTab = 1 },
                        onNavigateToTodo = { selectedTab = 2 },
                        onNavigateToChores = { selectedTab = 3 },
                        onQuickActionAddEvent = { selectedTab = 1 },
                        onQuickActionAddTask = { selectedTab = 2 },
                        onQuickActionSendAlert = { showNotificationsDialog = true }
                    )
                    1 -> CalendarScreen(
                        viewModel = viewModel
                    )
                    2 -> TodoScreen(
                        viewModel = viewModel
                    )
                    3 -> ChoresScreen(
                        viewModel = viewModel
                    )
                    4 -> MapScreen(
                        viewModel = viewModel
                    )
                    5 -> ProfileSettingsScreen(
                        viewModel = viewModel,
                        onNavigateToGeofences = { showGeofenceScreen = true }
                    )
                }
            }
        }
    }

    // Notifications and Broadcast Alert Dialog
    if (showNotificationsDialog) {
        NotificationsDialog(
            viewModel = viewModel,
            onDismiss = { showNotificationsDialog = false }
        )
    }
}
