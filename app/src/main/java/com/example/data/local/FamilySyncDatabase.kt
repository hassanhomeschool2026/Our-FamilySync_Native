package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CheckInEntity::class,
        FamilyMemberEntity::class,
        GeofenceZoneEntity::class,
        CalendarEventEntity::class,
        TaskEntity::class,
        ChoreEntity::class,
        FeedItemEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FamilySyncDatabase : RoomDatabase() {
    abstract fun familySyncDao(): FamilySyncDao

    companion object {
        @Volatile
        private var INSTANCE: FamilySyncDatabase? = null

        fun getDatabase(context: Context): FamilySyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FamilySyncDatabase::class.java,
                    "family_sync_db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default family data
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).familySyncDao()
                            seedDefaultData(dao)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaultData(dao: FamilySyncDao) {
            val defaultMembers = listOf(
                FamilyMemberEntity(
                    id = "self_user",
                    name = "You",
                    role = "Parent",
                    colorHex = 0xFF2563EB, // Royal Blue
                    avatarInitials = "ME",
                    isSelf = true,
                    latitude = 32.9482,
                    longitude = -96.7970,
                    address = "1420 Emerald Bay Dr, Dallas, TX",
                    statusText = "At Home",
                    batteryPct = 94,
                    speedMph = 0f,
                    lastUpdatedMillis = System.currentTimeMillis() - (2 * 60 * 1000)
                ),
                FamilyMemberEntity(
                    id = "member_mom",
                    name = "Amina",
                    role = "Parent",
                    colorHex = 0xFF0D9488, // Teal
                    avatarInitials = "AM",
                    isSelf = false,
                    latitude = 32.9515,
                    longitude = -96.7910,
                    address = "Town Center Market, Dallas, TX",
                    statusText = "Shopping",
                    batteryPct = 81,
                    speedMph = 14f,
                    lastUpdatedMillis = System.currentTimeMillis() - (5 * 60 * 1000)
                ),
                FamilyMemberEntity(
                    id = "member_child1",
                    name = "Zayd",
                    role = "Child",
                    colorHex = 0xFFF59E0B, // Amber
                    avatarInitials = "ZY",
                    isSelf = false,
                    latitude = 32.9560,
                    longitude = -96.8040,
                    address = "Westfield Academy, Dallas, TX",
                    statusText = "At School",
                    batteryPct = 68,
                    speedMph = 0f,
                    lastUpdatedMillis = System.currentTimeMillis() - (12 * 60 * 1000)
                ),
                FamilyMemberEntity(
                    id = "member_child2",
                    name = "Hana",
                    role = "Teen",
                    colorHex = 0xFF8B5CF6, // Purple
                    avatarInitials = "HN",
                    isSelf = false,
                    latitude = 32.9430,
                    longitude = -96.7850,
                    address = "Youth Soccer Field, Dallas, TX",
                    statusText = "Soccer Practice",
                    batteryPct = 54,
                    speedMph = 3f,
                    lastUpdatedMillis = System.currentTimeMillis() - (8 * 60 * 1000)
                )
            )
            dao.insertOrUpdateMembers(defaultMembers)

            val defaultZones = listOf(
                GeofenceZoneEntity(
                    name = "Family Home",
                    latitude = 32.9482,
                    longitude = -96.7970,
                    radiusMeters = 250f,
                    category = "Home",
                    colorHex = 0xFF2563EB,
                    notifyOnEnter = true,
                    notifyOnExit = true
                ),
                GeofenceZoneEntity(
                    name = "Westfield Academy",
                    latitude = 32.9560,
                    longitude = -96.8040,
                    radiusMeters = 300f,
                    category = "School",
                    colorHex = 0xFFF59E0B,
                    notifyOnEnter = true,
                    notifyOnExit = true
                ),
                GeofenceZoneEntity(
                    name = "Sports Complex & Field",
                    latitude = 32.9430,
                    longitude = -96.7850,
                    radiusMeters = 350f,
                    category = "Activity",
                    colorHex = 0xFF10B981,
                    notifyOnEnter = true,
                    notifyOnExit = true
                )
            )
            defaultZones.forEach { dao.insertZone(it) }

            val initialCheckIns = listOf(
                CheckInEntity(
                    userId = "member_mom",
                    userName = "Amina",
                    userAvatar = "AM",
                    userColor = 0xFF0D9488,
                    latitude = 32.9515,
                    longitude = -96.7910,
                    locationName = "Town Center Market",
                    address = "1820 Preston Rd, Dallas, TX",
                    note = "Picking up groceries for dinner!",
                    batteryPct = 81,
                    speedMph = 0f,
                    createdAtMillis = System.currentTimeMillis() - (25 * 60 * 1000)
                ),
                CheckInEntity(
                    userId = "member_child1",
                    userName = "Zayd",
                    userAvatar = "ZY",
                    userColor = 0xFFF59E0B,
                    latitude = 32.9560,
                    longitude = -96.8040,
                    locationName = "Westfield Academy",
                    address = "Westfield School Campus, Dallas, TX",
                    note = "Arrived safely at class",
                    batteryPct = 78,
                    speedMph = 0f,
                    createdAtMillis = System.currentTimeMillis() - (2 * 3600 * 1000)
                )
            )
            initialCheckIns.forEach { dao.insertCheckIn(it) }

            // Default Calendar Events
            val defaultEvents = listOf(
                CalendarEventEntity(
                    title = "Youth Soccer Match",
                    date = "2026-09-12",
                    startTime = "10:00 AM",
                    endTime = "11:30 AM",
                    description = "Field #3 vs Riverdale. Bring water bottles & shin guards.",
                    memberId = "member_child2",
                    memberName = "Hana",
                    memberColor = 0xFF8B5CF6
                ),
                CalendarEventEntity(
                    title = "Family Pizza & Board Games",
                    date = "2026-09-12",
                    startTime = "6:30 PM",
                    endTime = "8:30 PM",
                    description = "Friday game night at home! Catan & Ticket to Ride.",
                    memberId = "self_user",
                    memberName = "Family",
                    memberColor = 0xFF2563EB
                ),
                CalendarEventEntity(
                    title = "Piano Recital Rehearsal",
                    date = "2026-09-14",
                    startTime = "4:00 PM",
                    endTime = "5:15 PM",
                    description = "Community Arts Center, Room 204.",
                    memberId = "member_child1",
                    memberName = "Zayd",
                    memberColor = 0xFFF59E0B
                )
            )
            dao.insertEvents(defaultEvents)

            // Default Tasks & Shopping
            val defaultTasks = listOf(
                TaskEntity(
                    title = "Pick up prescription at CVS",
                    description = "Ready by 2 PM on Preston Rd",
                    priority = "high",
                    dueDate = "Today",
                    isShoppingList = false,
                    completed = false,
                    assignedToId = "self_user",
                    assignedToName = "You",
                    assignedColor = 0xFF2563EB,
                    createdAtMillis = System.currentTimeMillis() - (4 * 3600 * 1000)
                ),
                TaskEntity(
                    title = "Order 6th grade math workbook",
                    description = "Amazon prime for Zayd's homework class",
                    priority = "medium",
                    dueDate = "Tomorrow",
                    isShoppingList = false,
                    completed = false,
                    assignedToId = "member_mom",
                    assignedToName = "Amina",
                    assignedColor = 0xFF0D9488,
                    createdAtMillis = System.currentTimeMillis() - (2 * 3600 * 1000)
                ),
                TaskEntity(
                    title = "Return library books",
                    description = "3 books due before weekend",
                    priority = "low",
                    dueDate = "Friday",
                    isShoppingList = false,
                    completed = true,
                    assignedToId = "member_child1",
                    assignedToName = "Zayd",
                    assignedColor = 0xFFF59E0B,
                    createdAtMillis = System.currentTimeMillis() - (10 * 3600 * 1000)
                ),
                // Shopping list items
                TaskEntity(
                    title = "Organic Whole Milk & Oat Milk",
                    description = "2 gallons",
                    priority = "medium",
                    dueDate = "Shopping",
                    isShoppingList = true,
                    completed = false,
                    assignedToId = "member_mom",
                    assignedToName = "Amina",
                    assignedColor = 0xFF0D9488,
                    createdAtMillis = System.currentTimeMillis() - (3 * 3600 * 1000)
                ),
                TaskEntity(
                    title = "Honeycrisp Apples & Bananas",
                    description = "Fruit bowl restock",
                    priority = "low",
                    dueDate = "Shopping",
                    isShoppingList = true,
                    completed = false,
                    assignedToId = "self_user",
                    assignedToName = "You",
                    assignedColor = 0xFF2563EB,
                    createdAtMillis = System.currentTimeMillis() - (3 * 3600 * 1000)
                )
            )
            dao.insertTasks(defaultTasks)

            // Default Chores
            val defaultChores = listOf(
                ChoreEntity(
                    title = "Unload & wash breakfast dishes",
                    pointValue = 5,
                    recurrence = "Daily",
                    completed = true,
                    completedAtMillis = System.currentTimeMillis() - (2 * 3600 * 1000),
                    completedById = "member_child2",
                    streakCount = 6,
                    assignedToId = "member_child2",
                    assignedToName = "Hana",
                    assignedColor = 0xFF8B5CF6
                ),
                ChoreEntity(
                    title = "Walk & feed the family golden retriever",
                    pointValue = 5,
                    recurrence = "Daily",
                    completed = false,
                    completedAtMillis = null,
                    completedById = null,
                    streakCount = 4,
                    assignedToId = "member_child1",
                    assignedToName = "Zayd",
                    assignedColor = 0xFFF59E0B
                ),
                ChoreEntity(
                    title = "Take out recycling and trash bins",
                    pointValue = 10,
                    recurrence = "Weekdays",
                    completed = false,
                    completedAtMillis = null,
                    completedById = null,
                    streakCount = 5,
                    assignedToId = "self_user",
                    assignedToName = "You",
                    assignedColor = 0xFF2563EB
                ),
                ChoreEntity(
                    title = "Fold clean laundry & organize closets",
                    pointValue = 8,
                    recurrence = "Weekly",
                    completed = false,
                    completedAtMillis = null,
                    completedById = null,
                    streakCount = 3,
                    assignedToId = "member_mom",
                    assignedToName = "Amina",
                    assignedColor = 0xFF0D9488
                )
            )
            dao.insertChores(defaultChores)

            // Feed Items
            val defaultFeed = listOf(
                FeedItemEntity(
                    type = "checkin",
                    message = "Amina checked in from Town Center Market",
                    userName = "Amina",
                    userAvatar = "AM",
                    userColor = 0xFF0D9488,
                    createdAtMillis = System.currentTimeMillis() - (25 * 60 * 1000)
                ),
                FeedItemEntity(
                    type = "chore_completed",
                    message = "Hana finished \"Unload & wash breakfast dishes\" (+5 pts)",
                    userName = "Hana",
                    userAvatar = "HN",
                    userColor = 0xFF8B5CF6,
                    createdAtMillis = System.currentTimeMillis() - (2 * 3600 * 1000)
                ),
                FeedItemEntity(
                    type = "event_added",
                    message = "Added new event: Youth Soccer Match on Sep 12",
                    userName = "Hana",
                    userAvatar = "HN",
                    userColor = 0xFF8B5CF6,
                    createdAtMillis = System.currentTimeMillis() - (4 * 3600 * 1000)
                ),
                FeedItemEntity(
                    type = "family_alert",
                    message = "Amina sent a family broadcast: Pick up milk on the way home please!",
                    userName = "Amina",
                    userAvatar = "AM",
                    userColor = 0xFF0D9488,
                    createdAtMillis = System.currentTimeMillis() - (6 * 3600 * 1000)
                )
            )
            dao.insertFeedItems(defaultFeed)

            // Notifications
            val defaultNotifications = listOf(
                NotificationEntity(
                    title = "Safe Zone Arrival",
                    message = "Zayd safely entered Westfield Academy zone.",
                    type = "geofence",
                    isRead = false,
                    createdAtMillis = System.currentTimeMillis() - (35 * 60 * 1000)
                ),
                NotificationEntity(
                    title = "Chore Complete",
                    message = "Hana checked off breakfast dishes! Streak: 6 days.",
                    type = "chore",
                    isRead = false,
                    createdAtMillis = System.currentTimeMillis() - (2 * 3600 * 1000)
                ),
                NotificationEntity(
                    title = "Event Reminder",
                    message = "Youth Soccer Match starts tomorrow at 10:00 AM.",
                    type = "calendar",
                    isRead = true,
                    createdAtMillis = System.currentTimeMillis() - (5 * 3600 * 1000)
                )
            )
            dao.insertNotifications(defaultNotifications)
        }
    }
}

