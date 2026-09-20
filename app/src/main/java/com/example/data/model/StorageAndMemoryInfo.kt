package com.example.data.model

data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedPercentage: Int,
    val isAlmostFull: Boolean,
    val formattedTotal: String,
    val formattedUsed: String,
    val formattedFree: String
)

data class MemoryInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val usedPercentage: Int,
    val isLowRam: Boolean,
    val formattedTotal: String,
    val formattedUsed: String,
    val formattedAvailable: String
)

data class UptimeInfo(
    val uptimeMillis: Long,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val lastBootTimestamp: Long,
    val formattedLastBootDate: String
)
