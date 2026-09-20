package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String,
    val healthScore: Int,
    val batteryLevel: Int,
    val batteryHealth: String,
    val batteryTemp: Float,
    val storageUsedBytes: Long,
    val storageTotalBytes: Long,
    val ramUsedBytes: Long,
    val ramTotalBytes: Long,
    val issuesCount: Int,
    val criticalCount: Int,
    val statusSummary: String,
    val problemsSummary: String
)
