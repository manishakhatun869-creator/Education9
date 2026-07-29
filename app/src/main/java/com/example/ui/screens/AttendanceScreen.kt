package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusLateYellow
import com.example.ui.theme.StatusLeaveBlue
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Mark Today, 1 = View History Logs

    val students by viewModel.students.collectAsState(initial = emptyList())
    val attendanceLogs by viewModel.attendance.collectAsState(initial = emptyList())

    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Mark Attendance", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Attendance Logs", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // MARK ATTENDANCE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Marking for Date:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        modifier = Modifier.width(180.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (students.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No students added yet. Add students first!")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(students, key = { it.id }) { student ->
                            val currentStatus = attendanceLogs.find {
                                it.studentCode == student.studentCode && it.date == selectedDate
                            }?.status

                            StudentAttendanceRow(
                                student = student,
                                currentStatus = currentStatus,
                                onSelectStatus = { status ->
                                    viewModel.markAttendance(student, selectedDate, status)
                                    Toast.makeText(
                                        context,
                                        "Marked ${status.name} for ${student.name}. Push Notification Triggered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }

            } else {
                // ATTENDANCE LOGS HISTORY
                if (attendanceLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No attendance history records found.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(attendanceLogs) { log ->
                            AttendanceLogCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAttendanceRow(
    student: Student,
    currentStatus: AttendanceStatus?,
    onSelectStatus: (AttendanceStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${student.className} | Roll ${student.roll} (${student.studentCode})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                currentStatus?.let { status ->
                    val color = when (status) {
                        AttendanceStatus.PRESENT -> StatusPresentGreen
                        AttendanceStatus.ABSENT -> StatusAbsentRed
                        AttendanceStatus.LATE -> StatusLateYellow
                        AttendanceStatus.LEAVE -> StatusLeaveBlue
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttendanceButton(
                    label = "Present",
                    color = StatusPresentGreen,
                    isSelected = currentStatus == AttendanceStatus.PRESENT,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectStatus(AttendanceStatus.PRESENT) }
                )
                AttendanceButton(
                    label = "Absent",
                    color = StatusAbsentRed,
                    isSelected = currentStatus == AttendanceStatus.ABSENT,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectStatus(AttendanceStatus.ABSENT) }
                )
                AttendanceButton(
                    label = "Late",
                    color = StatusLateYellow,
                    isSelected = currentStatus == AttendanceStatus.LATE,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectStatus(AttendanceStatus.LATE) }
                )
                AttendanceButton(
                    label = "Leave",
                    color = StatusLeaveBlue,
                    isSelected = currentStatus == AttendanceStatus.LEAVE,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectStatus(AttendanceStatus.LEAVE) }
                )
            }
        }
    }
}

@Composable
fun AttendanceButton(
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else color.copy(alpha = 0.15f),
            contentColor = if (isSelected) Color.White else color
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(38.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun AttendanceLogCard(log: Attendance) {
    val color = when (log.status) {
        AttendanceStatus.PRESENT -> StatusPresentGreen
        AttendanceStatus.ABSENT -> StatusAbsentRed
        AttendanceStatus.LATE -> StatusLateYellow
        AttendanceStatus.LEAVE -> StatusLeaveBlue
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = log.studentName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Code: ${log.studentCode} | Date: ${log.date}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = log.status.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
