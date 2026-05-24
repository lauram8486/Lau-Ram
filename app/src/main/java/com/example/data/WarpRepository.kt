package com.example.data

import kotlinx.coroutines.flow.Flow

class WarpRepository(private val performanceLogDao: PerformanceLogDao) {
    val allLogs: Flow<List<PerformanceLog>> = performanceLogDao.getAllLogs()

    suspend fun insertLog(log: PerformanceLog) {
        performanceLogDao.insertLog(log)
    }

    suspend fun clearAllLogs() {
        performanceLogDao.clearAllLogs()
    }

    suspend fun deleteLogById(id: Int) {
        performanceLogDao.deleteLogById(id)
    }
}
