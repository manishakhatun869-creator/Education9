package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Attendance
import com.example.data.model.AttendanceDao
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeDao
import com.example.data.model.FeeRecord
import com.example.data.model.FeeStatus
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationDao
import com.example.data.model.NotificationItem
import com.example.data.model.Student
import com.example.data.model.StudentDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)

    @TypeConverter
    fun fromFeeStatus(status: FeeStatus): String = status.name

    @TypeConverter
    fun toFeeStatus(value: String): FeeStatus = FeeStatus.valueOf(value)

    @TypeConverter
    fun fromNotificationCategory(category: NotificationCategory): String = category.name

    @TypeConverter
    fun toNotificationCategory(value: String): NotificationCategory = NotificationCategory.valueOf(value)
}

@Database(
    entities = [Student::class, Attendance::class, FeeRecord::class, NotificationItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun feeDao(): FeeDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "towfik_edu_db"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(db: AppDatabase) {
                val studentDao = db.studentDao()
                val attendanceDao = db.attendanceDao()
                val feeDao = db.feeDao()
                val notificationDao = db.notificationDao()

                val s1 = Student(
                    studentCode = "STU-1001",
                    name = "Towfik Rahman",
                    className = "Class 10-A",
                    roll = 1,
                    parentName = "Mahmudur Rahman",
                    phone = "+8801700000001",
                    address = "123 Education Ave, Dhaka",
                    admissionDate = "2024-01-10",
                    password = "std123"
                )
                val s2 = Student(
                    studentCode = "STU-1002",
                    name = "Aminur Islam",
                    className = "Class 10-A",
                    roll = 2,
                    parentName = "Rafiqul Islam",
                    phone = "+8801700000002",
                    address = "45 Science Road, Chittagong",
                    admissionDate = "2024-01-12",
                    password = "std123"
                )
                val s3 = Student(
                    studentCode = "STU-1003",
                    name = "Fatema Khatun",
                    className = "Class 10-B",
                    roll = 1,
                    parentName = "Abdul Karim",
                    phone = "+8801700000003",
                    address = "78 Model Town, Sylhet",
                    admissionDate = "2024-01-15",
                    password = "std123"
                )
                val s4 = Student(
                    studentCode = "STU-1004",
                    name = "Rahat Chowdhury",
                    className = "Class 9-A",
                    roll = 5,
                    parentName = "Nazmul Chowdhury",
                    phone = "+8801700000004",
                    address = "12 Academy Street, Rajshahi",
                    admissionDate = "2024-02-01",
                    password = "std123"
                )

                val id1 = studentDao.insertStudent(s1)
                val id2 = studentDao.insertStudent(s2)
                val id3 = studentDao.insertStudent(s3)
                val id4 = studentDao.insertStudent(s4)

                val today = "2026-07-25"
                val yesterday = "2026-07-24"
                val dayBefore = "2026-07-23"

                attendanceDao.insertAttendance(Attendance(studentId = id1, studentCode = "STU-1001", studentName = "Towfik Rahman", date = today, status = AttendanceStatus.PRESENT))
                attendanceDao.insertAttendance(Attendance(studentId = id2, studentCode = "STU-1002", studentName = "Aminur Islam", date = today, status = AttendanceStatus.ABSENT))
                attendanceDao.insertAttendance(Attendance(studentId = id3, studentCode = "STU-1003", studentName = "Fatema Khatun", date = today, status = AttendanceStatus.LATE))
                attendanceDao.insertAttendance(Attendance(studentId = id4, studentCode = "STU-1004", studentName = "Rahat Chowdhury", date = today, status = AttendanceStatus.PRESENT))

                attendanceDao.insertAttendance(Attendance(studentId = id1, studentCode = "STU-1001", studentName = "Towfik Rahman", date = yesterday, status = AttendanceStatus.PRESENT))
                attendanceDao.insertAttendance(Attendance(studentId = id2, studentCode = "STU-1002", studentName = "Aminur Islam", date = yesterday, status = AttendanceStatus.PRESENT))
                attendanceDao.insertAttendance(Attendance(studentId = id3, studentCode = "STU-1003", studentName = "Fatema Khatun", date = yesterday, status = AttendanceStatus.PRESENT))
                attendanceDao.insertAttendance(Attendance(studentId = id4, studentCode = "STU-1004", studentName = "Rahat Chowdhury", date = yesterday, status = AttendanceStatus.LEAVE))

                attendanceDao.insertAttendance(Attendance(studentId = id1, studentCode = "STU-1001", studentName = "Towfik Rahman", date = dayBefore, status = AttendanceStatus.PRESENT))
                attendanceDao.insertAttendance(Attendance(studentId = id2, studentCode = "STU-1002", studentName = "Aminur Islam", date = dayBefore, status = AttendanceStatus.LATE))

                feeDao.insertFeeRecord(FeeRecord(studentId = id1, studentCode = "STU-1001", studentName = "Towfik Rahman", month = "July", year = 2026, totalAmount = 2500.0, paidAmount = 2500.0, dueAmount = 0.0, status = FeeStatus.PAID, paymentDate = "2026-07-10", receiptNo = "REC-20260710-001"))
                feeDao.insertFeeRecord(FeeRecord(studentId = id2, studentCode = "STU-1002", studentName = "Aminur Islam", month = "July", year = 2026, totalAmount = 2500.0, paidAmount = 1000.0, dueAmount = 1500.0, status = FeeStatus.PARTIAL, paymentDate = "2026-07-12", receiptNo = "REC-20260712-002"))
                feeDao.insertFeeRecord(FeeRecord(studentId = id3, studentCode = "STU-1003", studentName = "Fatema Khatun", month = "July", year = 2026, totalAmount = 2500.0, paidAmount = 0.0, dueAmount = 2500.0, status = FeeStatus.DUE, paymentDate = "-", receiptNo = "N/A"))
                feeDao.insertFeeRecord(FeeRecord(studentId = id4, studentCode = "STU-1004", studentName = "Rahat Chowdhury", month = "July", year = 2026, totalAmount = 2200.0, paidAmount = 2200.0, dueAmount = 0.0, status = FeeStatus.PAID, paymentDate = "2026-07-05", receiptNo = "REC-20260705-003"))

                notificationDao.insertNotification(
                    NotificationItem(
                        studentCode = "STU-1001",
                        title = "Attendance Marked",
                        message = "✅ Towfik, you are marked Present today.",
                        category = NotificationCategory.ATTENDANCE,
                        timestamp = System.currentTimeMillis() - 3600000,
                        isGlobal = false
                    )
                )
                notificationDao.insertNotification(
                    NotificationItem(
                        studentCode = "STU-1001",
                        title = "Fee Received",
                        message = "💰 Your July fee has been received successfully.",
                        category = NotificationCategory.FEES,
                        timestamp = System.currentTimeMillis() - 86400000,
                        isGlobal = false
                    )
                )
                notificationDao.insertNotification(
                    NotificationItem(
                        title = "Mid-Term Examination Schedule",
                        message = "Mid-Term examinations start from August 15, 2026. Routine has been uploaded.",
                        category = NotificationCategory.EXAM,
                        timestamp = System.currentTimeMillis() - 172800000,
                        isGlobal = true
                    )
                )
                notificationDao.insertNotification(
                    NotificationItem(
                        title = "Institute Holiday Announcement",
                        message = "The institute will remain closed on August 1st for National Holiday.",
                        category = NotificationCategory.HOLIDAY,
                        timestamp = System.currentTimeMillis() - 259200000,
                        isGlobal = true
                    )
                )
            }
        }
    }
}
