package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentCode: String,
    val name: String,
    val className: String,
    val roll: Int,
    val photoUri: String? = null,
    val parentName: String,
    val phone: String,
    val address: String,
    val admissionDate: String,
    val password: String = "std123"
)

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE, LEAVE
}

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentCode: String,
    val studentName: String,
    val date: String, // YYYY-MM-DD
    val status: AttendanceStatus,
    val markedAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

enum class FeeStatus {
    PAID, DUE, PARTIAL
}

@Entity(tableName = "fee_records")
data class FeeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentCode: String,
    val studentName: String,
    val month: String,
    val year: Int,
    val totalAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val status: FeeStatus,
    val paymentDate: String,
    val receiptNo: String
)

enum class NotificationCategory {
    ATTENDANCE, FEES, EXAM, HOLIDAY, ANNOUNCEMENT
}

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentCode: String? = null, // null means global or class broadcast
    val title: String,
    val message: String,
    val category: NotificationCategory,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isGlobal: Boolean = true,
    val targetClass: String? = null
)
