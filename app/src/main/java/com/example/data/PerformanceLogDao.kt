package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceLogDao {
    @Query("SELECT * FROM performance_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<PerformanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PerformanceLog)

    @Query("DELETE FROM performance_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM performance_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)
}
