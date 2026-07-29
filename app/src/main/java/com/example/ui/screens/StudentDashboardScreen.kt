package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusLateYellow
import com.example.ui.theme.StatusLeaveBlue
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: MainViewModel,
    onNavigateToAttendance: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    val student by viewModel.loggedInStudent.collectAsState()
    val allAttendance by viewModel.attendance.collectAsState(initial = emptyList())
    val allFees by viewModel.fees.collectAsState(initial = emptyList())
    val unreadCount by viewModel.unreadNotificationCount.collectAsState(initial = 0)

    val studentCode = student?.studentCode ?: ""
    val studentAttendance = allAttendance.filter { it.studentCode == studentCode }
    val studentFees = allFees.filter { it.studentCode == studentCode }

    val presentCount = studentAttendance.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = studentAttendance.count { it.status == AttendanceStatus.ABSENT }
    val lateCount = studentAttendance.count { it.status == AttendanceStatus.LATE }
    val totalDays = studentAttendance.size.coerceAtLeast(1)

    val attendancePercentage = ((presentCount.toFloat() / totalDays) * 100).toInt()

    val totalPaid = studentFees.sumOf { it.paidAmount }
    val totalDue = studentFees.sumOf { it.dueAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Portal Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondary
                ),
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Student Profile Header Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student?.name?.take(1) ?: "S",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = student?.name ?: "Student",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Code: ${student?.studentCode ?: "STU-1001"}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${student?.className ?: "Class 10"} | Roll: ${student?.roll ?: 1}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "Parent: ${student?.parentName ?: "-"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Attendance Overview Cards
            Text(
                text = "Attendance Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Attendance Score",
                    value = "$attendancePercentage%",
                    icon = Icons.Default.CheckCircle,
                    cardColor = StatusPresentGreen.copy(alpha = 0.15f),
                    textColor = StatusPresentGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Present Days",
                    value = "$presentCount",
                    icon = Icons.Default.EventAvailable,
                    cardColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Absent Days",
                    value = "$absentCount",
                    icon = Icons.Default.Warning,
                    cardColor = StatusAbsentRed.copy(alpha = 0.15f),
                    textColor = StatusAbsentRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Late Days",
                    value = "$lateCount",
                    icon = Icons.Default.Schedule,
                    cardColor = StatusLateYellow.copy(alpha = 0.15f),
                    textColor = StatusLateYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fees Overview Cards
            Text(
                text = "Fees Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Paid Fees",
                    value = "$${totalPaid.toInt()}",
                    icon = Icons.Default.ReceiptLong,
                    cardColor = StatusPresentGreen.copy(alpha = 0.15f),
                    textColor = StatusPresentGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Due Fees",
                    value = "$${totalDue.toInt()}",
                    icon = Icons.Default.Warning,
                    cardColor = StatusAbsentRed.copy(alpha = 0.15f),
                    textColor = StatusAbsentRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive Calendar Preview
            Text(
                text = "July 2026 Attendance Calendar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendDot("Green = Present", StatusPresentGreen)
                LegendDot("Red = Absent", StatusAbsentRed)
                LegendDot("Yellow = Late", StatusLateYellow)
                LegendDot("Blue = Leave", StatusLeaveBlue)
            }

            Spacer(modifier = Modifier.height(12.dp))

            CalendarGridPreview(studentAttendance = studentAttendance)

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Action Shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.CalendarMonth,
                    label = "Full Attendance",
                    color = StatusPresentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAttendance
                )
                QuickActionButton(
                    icon = Icons.Default.ReceiptLong,
                    label = "Fee Receipts",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToFees
                )
                QuickActionButton(
                    icon = Icons.Default.NotificationsActive,
                    label = "Notice Board",
                    color = StatusLateYellow,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToNotifications
                )
            }
        }
    }
}

@Composable
fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CalendarGridPreview(studentAttendance: List<Attendance>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(day, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Render 28 days for July 2026 grid preview
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0..3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 1..7) {
                            val dayNumber = row * 7 + col
                            val dateStr = "2026-07-${if (dayNumber < 10) "0$dayNumber" else dayNumber}"
                            val status = studentAttendance.find { it.date == dateStr }?.status

                            val bgColor = when (status) {
                                AttendanceStatus.PRESENT -> StatusPresentGreen
                                AttendanceStatus.ABSENT -> StatusAbsentRed
                                AttendanceStatus.LATE -> StatusLateYellow
                                AttendanceStatus.LEAVE -> StatusLeaveBlue
                                null -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (status != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
