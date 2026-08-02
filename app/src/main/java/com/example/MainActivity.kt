package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PushNotificationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.StudentManagementScreen
import com.example.ui.screens.StudentNotificationScreen
import com.example.ui.theme.TowfikEduTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }

        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)

            TowfikEduTheme(darkTheme = isDarkMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val userType by viewModel.userType.collectAsState(initial = "")
    val unreadCount by viewModel.unreadNotificationCount.collectAsState(initial = 0)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"

    val startDestination = when (userType) {
        "ADMIN" -> "admin_dashboard"
        "STUDENT" -> "student_dashboard"
        else -> "login"
    }

    val showBottomBar = currentRoute != "login"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    if (userType == "ADMIN") {
                        NavigationBarItem(
                            selected = currentRoute == "admin_dashboard",
                            onClick = {
                                navController.navigate("admin_dashboard") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "student_management",
                            onClick = {
                                navController.navigate("student_management") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                            label = { Text("Students") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "attendance",
                            onClick = {
                                navController.navigate("attendance") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Attendance") },
                            label = { Text("Attendance") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "fees",
                            onClick = {
                                navController.navigate("fees") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Fees") },
                            label = { Text("Fees") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "settings",
                            onClick = {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    } else if (userType == "STUDENT") {
                        NavigationBarItem(
                            selected = currentRoute == "student_dashboard",
                            onClick = {
                                navController.navigate("student_dashboard") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "attendance",
                            onClick = {
                                navController.navigate("attendance") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Attendance") },
                            label = { Text("Attendance") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "fees",
                            onClick = {
                                navController.navigate("fees") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Fees") },
                            label = { Text("Fees") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "student_notifications",
                            onClick = {
                                navController.navigate("student_notifications") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge { Text("$unreadCount") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                }
                            },
                            label = { Text("Notices") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "settings",
                            onClick = {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onAdminLoginSuccess = {
                        navController.navigate("admin_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onStudentLoginSuccess = {
                        navController.navigate("student_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("admin_dashboard") {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToStudents = { navController.navigate("student_management") },
                    onNavigateToAttendance = { navController.navigate("attendance") },
                    onNavigateToFees = { navController.navigate("fees") },
                    onNavigateToPushNotifications = { navController.navigate("push_notifications") }
                )
            }

            composable("student_management") {
                StudentManagementScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("attendance") {
                AttendanceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("fees") {
                FeesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("push_notifications") {
                PushNotificationScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("student_dashboard") {
                StudentDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAttendance = { navController.navigate("attendance") },
                    onNavigateToFees = { navController.navigate("fees") },
                    onNavigateToNotifications = { navController.navigate("student_notifications") },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("student_notifications") {
                StudentNotificationScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
