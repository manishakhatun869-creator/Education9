package com.example.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY className ASC, roll ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE studentCode = :code LIMIT 1")
    suspend fun getStudentByCode(code: String): Student?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR studentCode LIKE '%' || :query || '%' OR className LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Query("SELECT * FROM students WHERE className = :className")
    suspend fun getStudentsByClass(className: String): List<Student>

    @Query("SELECT MAX(id) FROM students")
    suspend fun getMaxStudentId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC, markedAt DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentCode = :studentCode ORDER BY date DESC")
    fun getAttendanceByStudentCode(studentCode: String): Flow<List<Attendance>>

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = :status")
    fun getCountByStatusAndDate(date: String, status: AttendanceStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE studentCode = :studentCode AND status = :status")
    fun getStudentAttendanceCount(studentCode: String, status: AttendanceStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE studentCode = :studentCode")
    fun getStudentTotalAttendanceDays(studentCode: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Query("SELECT * FROM attendance WHERE studentCode = :studentCode AND date = :date LIMIT 1")
    suspend fun getAttendanceForStudentOnDate(studentCode: String, date: String): Attendance?

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)
}

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_records ORDER BY year DESC, month DESC")
    fun getAllFees(): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE studentCode = :studentCode ORDER BY year DESC, month DESC")
    fun getFeesByStudentCode(studentCode: String): Flow<List<FeeRecord>>

    @Query("SELECT SUM(paidAmount) FROM fee_records")
    fun getTotalCollectedFees(): Flow<Double?>

    @Query("SELECT SUM(dueAmount) FROM fee_records")
    fun getTotalPendingFees(): Flow<Double?>

    @Query("SELECT * FROM fee_records WHERE status = 'DUE' OR status = 'PARTIAL'")
    fun getDueFeeRecords(): Flow<List<FeeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecord(feeRecord: FeeRecord): Long

    @Update
    suspend fun updateFeeRecord(feeRecord: FeeRecord)

    @Delete
    suspend fun deleteFeeRecord(feeRecord: FeeRecord)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT * FROM notifications WHERE studentCode = :studentCode OR isGlobal = 1 OR targetClass = :className ORDER BY timestamp DESC")
    fun getNotificationsForStudent(studentCode: String, className: String): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM notifications WHERE (studentCode = :studentCode OR isGlobal = 1 OR targetClass = :className) AND isRead = 0")
    fun getUnreadCountForStudent(studentCode: String, className: String): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE studentCode = :studentCode OR isGlobal = 1")
    suspend fun markAllAsReadForStudent(studentCode: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem): Long

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}
