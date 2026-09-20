package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ScanRecordEntity
import com.example.data.model.BatteryInfo
import com.example.data.model.CompleteDeviceInfo
import com.example.data.model.DiagnosticIssue
import com.example.data.model.HardwareTestItem
import com.example.data.model.IssueSeverity
import com.example.data.model.MemoryInfo
import com.example.data.model.StorageInfo
import com.example.data.model.TestStatus
import com.example.data.model.UptimeInfo
import com.example.data.repository.DeviceDiagnosticsRepository
import com.example.data.repository.ScanHistoryRepository
import com.example.domain.HardwareTestController
import com.example.domain.SensorMonitor
import com.example.domain.SensorReading
import com.example.ui.theme.AccentStyle
import com.example.util.SafeLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class StartupState {
    data class Loading(val progress: Float, val statusText: String) : StartupState()
    object Ready : StartupState()
}

data class ScanProgressState(
    val isScanning: Boolean = false,
    val stepIndex: Int = 0,
    val stepName: String = "",
    val isCompleted: Boolean = false
)

class DiagnosticsViewModel(application: Application) : AndroidViewModel(application) {

    private val diagnosticsRepo = DeviceDiagnosticsRepository(application)
    private val database = try {
        AppDatabase.getDatabase(application)
    } catch (t: Throwable) {
        SafeLog.e("Failed to initialize AppDatabase", t)
        null
    }
    private val historyRepo = database?.let { ScanHistoryRepository(it.scanRecordDao()) }
    val hardwareController = HardwareTestController(application)
    private val sensorMonitor = SensorMonitor(application)

    // Startup State Machine
    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading(0.15f, "Initializing PhoneCheck Pro..."))
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    // Safe default initial device state (no heavy hardware calls on Main thread constructor)
    private val _deviceInfo = MutableStateFlow(
        CompleteDeviceInfo(
            identity = diagnosticsRepo.safeDefaultIdentity(),
            system = diagnosticsRepo.safeDefaultSystem(),
            hardware = diagnosticsRepo.safeDefaultHardware(),
            display = diagnosticsRepo.safeDefaultDisplay(),
            camera = diagnosticsRepo.safeDefaultCamera(),
            connectivity = diagnosticsRepo.safeDefaultConnectivity(),
            security = diagnosticsRepo.safeDefaultSecurity()
        )
    )
    val deviceInfo: StateFlow<CompleteDeviceInfo> = _deviceInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(diagnosticsRepo.safeDefaultBattery())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _storageInfo = MutableStateFlow(diagnosticsRepo.safeDefaultStorage())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val _memoryInfo = MutableStateFlow(diagnosticsRepo.safeDefaultMemory())
    val memoryInfo: StateFlow<MemoryInfo> = _memoryInfo.asStateFlow()

    private val _uptimeInfo = MutableStateFlow(diagnosticsRepo.safeDefaultUptime())
    val uptimeInfo: StateFlow<UptimeInfo> = _uptimeInfo.asStateFlow()

    private val _issues = MutableStateFlow<List<DiagnosticIssue>>(emptyList())
    val issues: StateFlow<List<DiagnosticIssue>> = _issues.asStateFlow()

    private val _healthScore = MutableStateFlow(95)
    val healthScore: StateFlow<Int> = _healthScore.asStateFlow()

    // Scanner state
    private val _scanProgress = MutableStateFlow(ScanProgressState())
    val scanProgress: StateFlow<ScanProgressState> = _scanProgress.asStateFlow()

    // History from Room (with error handling)
    val scanHistory: StateFlow<List<ScanRecordEntity>> = (historyRepo?.allRecords
        ?.catch { e ->
            SafeLog.e("Error loading scan history", e)
            emit(emptyList())
        } ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestTwoRecords: StateFlow<List<ScanRecordEntity>> = (historyRepo?.latestTwoRecords
        ?.catch { e ->
            SafeLog.e("Error loading latest scan records", e)
            emit(emptyList())
        } ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sensors: cold flow, only subscribed when TestsScreen / Sensors is displayed
    val sensorReadings: StateFlow<Map<Int, SensorReading>> = sensorMonitor.observeSensors()
        .catch { e ->
            SafeLog.w("Error observing sensors", e)
            emit(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Hardware Tests Suite
    private val _hardwareTests = MutableStateFlow(initialTests())
    val hardwareTests: StateFlow<List<HardwareTestItem>> = _hardwareTests.asStateFlow()

    // Settings
    val isDarkMode = MutableStateFlow(true)
    val accentStyle = MutableStateFlow(AccentStyle.CYAN)
    val selectedLanguage = MutableStateFlow("en")

    init {
        performSafeAsyncStartup()
    }

    private fun performSafeAsyncStartup() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SafeLog.i("Starting safe background initialization...")
                _startupState.value = StartupState.Loading(0.25f, "Inspecting System Architecture...")

                // Step 1: Device Identity & OS Specs
                try {
                    val identity = diagnosticsRepo.getDeviceIdentity()
                    val system = diagnosticsRepo.getSystemSpecs()
                    val display = diagnosticsRepo.getDisplaySpecs()
                    _deviceInfo.value = _deviceInfo.value.copy(
                        identity = identity,
                        system = system,
                        display = display
                    )
                } catch (t: Throwable) {
                    SafeLog.w("Error during identity extraction", t)
                }

                delay(250)
                _startupState.value = StartupState.Loading(0.55f, "Reading Battery & Thermal Status...")

                // Step 2: Battery & Power Info
                try {
                    _batteryInfo.value = diagnosticsRepo.getBatteryInfo()
                } catch (t: Throwable) {
                    SafeLog.w("Error during battery extraction", t)
                }

                delay(250)
                _startupState.value = StartupState.Loading(0.75f, "Allocating Storage & Memory Metrics...")

                // Step 3: Storage, RAM & Uptime
                try {
                    _storageInfo.value = diagnosticsRepo.getStorageInfo()
                    _memoryInfo.value = diagnosticsRepo.getMemoryInfo()
                    _uptimeInfo.value = diagnosticsRepo.getUptimeInfo()
                    val hw = diagnosticsRepo.getHardwareSpecs()
                    _deviceInfo.value = _deviceInfo.value.copy(hardware = hw)
                } catch (t: Throwable) {
                    SafeLog.w("Error during storage/memory extraction", t)
                }

                delay(200)
                _startupState.value = StartupState.Loading(0.90f, "Finalizing Diagnostic Engine...")

                // Step 4: Connectivity, Camera & Security Specs (Safe deferred)
                try {
                    val conn = diagnosticsRepo.getConnectivitySpecs()
                    val cam = diagnosticsRepo.getCameraHardwareInfo()
                    val sec = diagnosticsRepo.getSecuritySpecs()
                    _deviceInfo.value = _deviceInfo.value.copy(
                        connectivity = conn,
                        camera = cam,
                        security = sec
                    )
                } catch (t: Throwable) {
                    SafeLog.w("Error during connectivity/security extraction", t)
                }

                // Step 5: Issues & Health Score
                try {
                    val detectedIssues = diagnosticsRepo.detectPotentialIssues()
                    _issues.value = detectedIssues
                    recomputeHealthScore(detectedIssues)
                } catch (t: Throwable) {
                    SafeLog.w("Error detecting issues", t)
                }

                SafeLog.i("Safe initialization completed successfully.")
            } catch (fatal: Throwable) {
                SafeLog.e("Fatal error in startup coroutine, falling back to ready", fatal)
            } finally {
                delay(300)
                _startupState.value = StartupState.Ready
            }
        }
    }

    private fun recomputeHealthScore(detectedIssues: List<DiagnosticIssue>) {
        var score = 100
        for (issue in detectedIssues) {
            when (issue.severity) {
                IssueSeverity.CRITICAL -> score -= 15
                IssueSeverity.WARNING -> score -= 6
                IssueSeverity.HEALTHY -> score -= 1
            }
        }
        if (_storageInfo.value.isAlmostFull) score -= 10
        if (_memoryInfo.value.isLowRam) score -= 8
        if (_batteryInfo.value.temperatureCelsius > 40f) score -= 8

        _healthScore.value = score.coerceIn(40, 100)
    }

    fun refreshAllDiagnostics() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _deviceInfo.value = diagnosticsRepo.getCompleteDeviceInfo()
                _batteryInfo.value = diagnosticsRepo.getBatteryInfo()
                _storageInfo.value = diagnosticsRepo.getStorageInfo()
                _memoryInfo.value = diagnosticsRepo.getMemoryInfo()
                _uptimeInfo.value = diagnosticsRepo.getUptimeInfo()
                val detectedIssues = diagnosticsRepo.detectPotentialIssues()
                _issues.value = detectedIssues
                recomputeHealthScore(detectedIssues)
            } catch (t: Throwable) {
                SafeLog.e("Error refreshing diagnostics", t)
            }
        }
    }

    fun startPhoneScan() {
        viewModelScope.launch {
            if (_scanProgress.value.isScanning) return@launch

            val stepTitles = listOf(
                "01 Device Identity & Kernel",
                "02 Battery Cells & Thermal Profile",
                "03 Storage Blocks & File Allocation",
                "04 RAM Congestion & Activity Manager",
                "05 CPU Cores & Scaling Governor",
                "06 Sensors Bus & Gyro Diagnostics",
                "07 Connectivity (Wi-Fi, Bluetooth, NFC)",
                "08 Display Panel & Density Validation",
                "09 Camera Sensor Arrays & Flash",
                "10 Android Security Patch & Keyguard"
            )

            _scanProgress.value = ScanProgressState(
                isScanning = true,
                stepIndex = 0,
                stepName = stepTitles[0],
                isCompleted = false
            )

            for (i in stepTitles.indices) {
                _scanProgress.value = ScanProgressState(
                    isScanning = true,
                    stepIndex = i,
                    stepName = stepTitles[i],
                    isCompleted = false
                )
                delay(380)
            }

            // Finish scan and refresh data
            refreshAllDiagnostics()
            val currentScore = _healthScore.value
            val bat = _batteryInfo.value
            val stor = _storageInfo.value
            val ram = _memoryInfo.value
            val iss = _issues.value

            // Persist scan result to Room Database safely
            try {
                val entity = ScanRecordEntity(
                    deviceModel = "${_deviceInfo.value.identity.manufacturer} ${_deviceInfo.value.identity.model}",
                    healthScore = currentScore,
                    batteryLevel = bat.levelPercent,
                    batteryHealth = bat.healthStatus,
                    batteryTemp = bat.temperatureCelsius,
                    storageUsedBytes = stor.usedBytes,
                    storageTotalBytes = stor.totalBytes,
                    ramUsedBytes = ram.usedBytes,
                    ramTotalBytes = ram.totalBytes,
                    issuesCount = iss.size,
                    criticalCount = iss.count { it.severity == IssueSeverity.CRITICAL },
                    statusSummary = if (currentScore >= 80) "Optimal Health" else "Attention Required",
                    problemsSummary = iss.joinToString(" | ") { it.title }
                )
                historyRepo?.saveScanRecord(entity)
            } catch (t: Throwable) {
                SafeLog.w("Failed to save scan record to database", t)
            }

            // Haptic notification of scan completion
            try {
                hardwareController.vibrateDevice()
            } catch (_: Throwable) {}

            _scanProgress.value = ScanProgressState(
                isScanning = false,
                stepIndex = 10,
                stepName = "Diagnostic Scan Complete",
                isCompleted = true
            )
        }
    }

    fun dismissScanOverlay() {
        _scanProgress.value = ScanProgressState(isScanning = false, isCompleted = false)
    }

    fun updateTestStatus(testId: String, status: TestStatus, metric: String? = null) {
        _hardwareTests.value = _hardwareTests.value.map { item ->
            if (item.id == testId) {
                item.copy(status = status, metricValue = metric)
            } else item
        }
    }

    fun deleteHistoryRecord(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepo?.deleteRecord(id)
            } catch (t: Throwable) {
                SafeLog.w("Failed to delete record", t)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepo?.clearHistory()
            } catch (t: Throwable) {
                SafeLog.w("Failed to clear history", t)
            }
        }
    }

    private fun initialTests(): List<HardwareTestItem> {
        return listOf(
            HardwareTestItem("touch", R.string.test_touch, R.string.touch_hint, "touch_app"),
            HardwareTestItem("multitouch", R.string.test_multitouch, R.string.multitouch_hint, "pinch"),
            HardwareTestItem("display", R.string.test_display, R.string.display_test_hint, "smartphone"),
            HardwareTestItem("vibration", R.string.test_vibration, R.string.test_vibrate_prompt, "vibration"),
            HardwareTestItem("speaker", R.string.test_speaker, R.string.play_test_sound, "volume_up"),
            HardwareTestItem("earpiece", R.string.test_earpiece, R.string.did_you_hear_sound, "hearing"),
            HardwareTestItem("microphone", R.string.test_microphone, R.string.record_test, "mic"),
            HardwareTestItem("camera_rear", R.string.test_camera_rear, R.string.camera_specs, "camera_alt"),
            HardwareTestItem("camera_front", R.string.test_camera_front, R.string.front_camera, "face"),
            HardwareTestItem("flash", R.string.test_flash, R.string.toggle_torch, "flash_on"),
            HardwareTestItem("sensors", R.string.test_sensors, R.string.sensors, "sensors"),
            HardwareTestItem("connectivity", R.string.test_connectivity, R.string.connectivity, "wifi")
        )
    }
}
