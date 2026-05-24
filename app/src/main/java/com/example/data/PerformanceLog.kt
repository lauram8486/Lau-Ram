package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "performance_logs")
data class PerformanceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Int,
    val speedMbps: Double,
    val dnsProvider: String,
    val isTunnelActive: Boolean,
    val maxResolutionUnlocked: String,
    val testScore: Int
)
