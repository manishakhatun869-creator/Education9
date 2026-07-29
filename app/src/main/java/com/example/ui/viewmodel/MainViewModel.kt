package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeRecord
import com.example.data.model.FeeStatus
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationItem
import com.example.data.model.Student
import com.example.util.NotificationHelper
import com.example.util.PreferencesManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val studentDao = db.studentDao()
    private val attendanceDao = db.attendanceDao()
    private val feeDao = db.feeDao()
    private val notificationDao = db.notificationDao()
    private val prefs = PreferencesManager(application)

    val isDarkMode: Flow<Boolean> = prefs.isDarkMode
    val userType: Flow<String> = prefs.userType
    val studentCode: Flow<String> = prefs.studentCode

    val students: Flow<List<Student>> = studentDao.getAllStudents()
    val attendance: Flow<List<Attendance>> = attendanceDao.getAllAttendance()
    val fees: Flow<List<FeeRecord>> = feeDao.getAllFees()
    val notifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications()

    val totalStudentsCount: Flow<Int> = studentDao.getStudentCount()

    private val todayDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val presentTodayCount: Flow<Int> = attendanceDao.getCountByStatusAndDate(todayDateStr, AttendanceStatus.PRESENT)
    val absentTodayCount: Flow<Int> = attendanceDao.getCountByStatusAndDate(todayDateStr, AttendanceStatus.ABSENT)
    val lateTodayCount: Flow<Int> = attendanceDao.getCountByStatusAndDate(todayDateStr, AttendanceStatus.LATE)

    val totalCollectedFees: Flow<Double?> = feeDao.getTotalCollectedFees()
    val totalPendingFees: Flow<Double?> = feeDao.getTotalPendingFees()
    val dueFeeRecords: Flow<List<FeeRecord>> = feeDao.getDueFeeRecords()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<List<Student>> = _searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) {
            studentDao.getAllStudents()
        } else {
            studentDao.searchStudents(query)
        }
    }

    // Student specific state
    private val _loggedInStudent = MutableStateFlow<Student?>(null)
    val loggedInStudent: StateFlow<Student?> = _loggedInStudent.asStateFlow()

    init {
        viewModelScope.launch {
            studentCode.collect { code ->
                if (code.isNotEmpty()) {
                    _loggedInStudent.value = studentDao.getStudentByCode(code)
                } else {
                    _loggedInStudent.value = null
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentStudentNotifications: Flow<List<NotificationItem>> = combine(studentCode, _loggedInStudent) { code, student ->
        Pair(code, student?.className ?: "")
    }.flatMapLatest { (code, className) ->
        if (code.isNotEmpty()) {
            notificationDao.getNotificationsForStudent(code, className)
        } else {
            flowOf(emptyList())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotificationCount: Flow<Int> = combine(studentCode, _loggedInStudent) { code, student ->
        Pair(code, student?.className ?: "")
    }.flatMapLatest { (code, className) ->
        if (code.isNotEmpty()) {
            notificationDao.getUnreadCountForStudent(code, className)
        } else {
            flowOf(0)
        }
    }

    fun loginAdmin(email: String, remember: Boolean) {
        viewModelScope.launch {
            prefs.setLoggedInAdmin(email, remember)
        }
    }

    suspend fun loginStudent(code: String, pass: String): Boolean {
        val student = studentDao.getStudentByCode(code.trim())
        if (student != null && student.password == pass.trim()) {
            prefs.setLoggedInStudent(student.studentCode)
            _loggedInStudent.value = student
            return true
        }
        return false
    }

    fun logout() {
        viewModelScope.launch {
            prefs.logout()
            _loggedInStudent.value = null
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setDarkMode(enabled)
        }
    }

    suspend fun autoGenerateStudentCode(): String {
        val maxId = studentDao.getMaxStudentId() ?: 0
        val nextId = maxId + 1
        return "STU-${1000 + nextId}"
    }

    fun saveStudent(student: Student, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (student.id == 0L) {
                studentDao.insertStudent(student)
            } else {
                studentDao.updateStudent(student)
            }
            onComplete()
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            studentDao.deleteStudent(student)
        }
    }

    fun markAttendance(student: Student, date: String, status: AttendanceStatus, note: String = "") {
        viewModelScope.launch {
            val existing = attendanceDao.getAttendanceForStudentOnDate(student.studentCode, date)
            val record = Attendance(
                id = existing?.id ?: 0L,
                studentId = student.id,
                studentCode = student.studentCode,
                studentName = student.name,
                date = date,
                status = status,
                note = note
            )
            attendanceDao.insertAttendance(record)

            val statusEmoji = when (status) {
                AttendanceStatus.PRESENT -> "✅"
                AttendanceStatus.ABSENT -> "❌"
                AttendanceStatus.LATE -> "⏰"
                AttendanceStatus.LEAVE -> "📅"
            }
            val statusText = when (status) {
                AttendanceStatus.PRESENT -> "marked Present today."
                AttendanceStatus.ABSENT -> "marked Absent today."
                AttendanceStatus.LATE -> "marked Late today."
                AttendanceStatus.LEAVE -> "marked on Leave today."
            }

            val title = "Attendance Notification"
            val message = "$statusEmoji ${student.name}, you are $statusText"

            NotificationHelper.showNotification(getApplication(), title, message)

            notificationDao.insertNotification(
                NotificationItem(
                    studentCode = student.studentCode,
                    title = title,
                    message = message,
                    category = NotificationCategory.ATTENDANCE,
                    isGlobal = false,
                    targetClass = student.className
                )
            )
        }
    }

    fun addFeeRecord(fee: FeeRecord, onComplete: () -> Unit) {
        viewModelScope.launch {
            feeDao.insertFeeRecord(fee)

            val msg = when (fee.status) {
                FeeStatus.PAID -> "💰 Your ${fee.month} fee of $${fee.paidAmount.toInt()} has been received successfully."
                FeeStatus.DUE -> "⚠️ Your ${fee.month} fee of $${fee.totalAmount.toInt()} is due."
                FeeStatus.PARTIAL -> "📄 Partial payment of $${fee.paidAmount.toInt()} for ${fee.month} received. Due: $${fee.dueAmount.toInt()}"
            }

            val title = "Fee Update"
            NotificationHelper.showNotification(getApplication(), title, msg)

            notificationDao.insertNotification(
                NotificationItem(
                    studentCode = fee.studentCode,
                    title = title,
                    message = msg,
                    category = NotificationCategory.FEES,
                    isGlobal = false
                )
            )
            onComplete()
        }
    }

    fun sendBroadcastNotification(
        targetType: String, // "ALL", "CLASS", "STUDENT"
        targetValue: String,
        title: String,
        message: String,
        category: NotificationCategory
    ) {
        viewModelScope.launch {
            val isGlobal = targetType == "ALL"
            val studentCodeParam = if (targetType == "STUDENT") targetValue else null
            val classParam = if (targetType == "CLASS") targetValue else null

            val item = NotificationItem(
                studentCode = studentCodeParam,
                title = title,
                message = message,
                category = category,
                isGlobal = isGlobal,
                targetClass = classParam
            )
            notificationDao.insertNotification(item)

            NotificationHelper.showNotification(getApplication(), "📢 $title", message)
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            notificationDao.markAsRead(id)
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationDao.deleteNotificationById(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationDao.clearAllNotifications()
        }
    }
}
