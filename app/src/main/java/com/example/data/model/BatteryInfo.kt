package com.example.data.model

data class BatteryInfo(
    val levelPercent: Int,
    val healthStatus: String,
    val isHealthGood: Boolean,
    val temperatureCelsius: Float,
    val voltageMilliVolts: Int,
    val currentNowMicroAmperes: String,
    val chargingStatus: String,
    val isCharging: Boolean,
    val powerSource: String,
    val technology: String,
    val reportedCapacityMah: String,
    val designCapacityMah: String,
    val estimatedRemainingMah: String,
    val cycleCount: String,
    val generalStatus: String,
    val healthScorePercent: Int
)
