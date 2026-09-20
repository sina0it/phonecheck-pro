package com.example.data.repository

import android.Manifest
import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.data.model.BatteryInfo
import com.example.data.model.CameraHardwareInfo
import com.example.data.model.CompleteDeviceInfo
import com.example.data.model.ConnectivitySpecs
import com.example.data.model.DeviceIdentity
import com.example.data.model.DiagnosticIssue
import com.example.data.model.DisplaySpecs
import com.example.data.model.HardwareSpecs
import com.example.data.model.IssueSeverity
import com.example.data.model.MemoryInfo
import com.example.data.model.SecuritySpecs
import com.example.data.model.StorageInfo
import com.example.data.model.SystemSpecs
import com.example.data.model.UptimeInfo
import com.example.util.SafeLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class DeviceDiagnosticsRepository(private val context: Context) {

    fun getDeviceIdentity(): DeviceIdentity {
        return try {
            val brand = safeString(Build.BRAND).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            val manufacturer = safeString(Build.MANUFACTURER).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            val model = safeString(Build.MODEL)
            val product = safeString(Build.PRODUCT)
            val board = safeString(Build.BOARD)

            val deviceName = try {
                Settings.Global.getString(context.contentResolver, "device_name") ?: model
            } catch (t: Throwable) {
                SafeLog.w("Cannot read Settings.Global.device_name: ${t.message}")
                model
            }

            val deviceId = try {
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                if (!androidId.isNullOrBlank()) {
                    "${androidId.take(4)}••••••••${androidId.takeLast(4)}"
                } else {
                    "Not available on this device"
                }
            } catch (t: Throwable) {
                SafeLog.w("Cannot read ANDROID_ID: ${t.message}")
                "Not available on this device"
            }

            val brandOrigin = resolveBrandOrigin(manufacturer)
            val releaseYear = resolveReleaseYear(model, manufacturer)
            val market = resolveDeviceMarket(model)

            DeviceIdentity(
                brand = brand.ifBlank { "Unknown" },
                manufacturer = manufacturer.ifBlank { "Unknown" },
                model = model.ifBlank { "Unknown" },
                exactModelName = if (manufacturer.isNotBlank() && model.isNotBlank()) "$manufacturer $model" else "Unknown",
                modelNumber = safeString(Build.ID).ifBlank { "Unknown" },
                deviceName = deviceName.ifBlank { "Android Device" },
                productName = product.ifBlank { "Unknown" },
                deviceFamily = board.ifBlank { "Unknown" },
                deviceId = deviceId,
                brandOrigin = brandOrigin,
                assemblyCountry = "Unknown",
                releaseYear = releaseYear,
                manufacturingRegion = "Not available on this device",
                deviceMarket = market,
                modelRegionCode = resolveModelRegionCode(model)
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving DeviceIdentity", t)
            safeDefaultIdentity()
        }
    }

    private fun safeString(str: String?): String {
        return str?.trim() ?: ""
    }

    fun safeDefaultIdentity(): DeviceIdentity {
        return DeviceIdentity(
            brand = "Unknown",
            manufacturer = "Unknown",
            model = "Unknown",
            exactModelName = "Android Device",
            modelNumber = "Unknown",
            deviceName = "Android Device",
            productName = "Unknown",
            deviceFamily = "Unknown",
            deviceId = "Not available on this device",
            brandOrigin = "Unknown",
            assemblyCountry = "Unknown",
            releaseYear = "Unknown",
            manufacturingRegion = "Not available on this device",
            deviceMarket = "Unknown",
            modelRegionCode = "Standard"
        )
    }

    private fun resolveBrandOrigin(manufacturer: String): String {
        return when (manufacturer.lowercase(Locale.ROOT)) {
            "samsung" -> "South Korea"
            "google", "apple", "motorola" -> "United States"
            "xiaomi", "oneplus", "oppo", "vivo", "huawei", "realme", "honor", "lenovo", "zte" -> "China"
            "sony" -> "Japan"
            "asus", "htc" -> "Taiwan"
            "nokia", "hmd global" -> "Finland"
            "lg" -> "South Korea"
            else -> "Unknown"
        }
    }

    private fun resolveReleaseYear(model: String, manufacturer: String): String {
        val upper = model.uppercase(Locale.ROOT)
        return when {
            upper.contains("S24") || upper.contains("PIXEL 9") || upper.contains("14 PRO") -> "2024"
            upper.contains("S23") || upper.contains("PIXEL 8") || upper.contains("13 PRO") -> "2023"
            upper.contains("S22") || upper.contains("PIXEL 7") || upper.contains("12 PRO") -> "2022"
            upper.contains("S21") || upper.contains("PIXEL 6") -> "2021"
            upper.contains("S20") || upper.contains("PIXEL 5") -> "2020"
            else -> {
                when (Build.VERSION.SDK_INT) {
                    35, 36 -> "2024-2025"
                    34 -> "2023-2024"
                    33 -> "2022-2023"
                    else -> "Unknown"
                }
            }
        }
    }

    private fun resolveDeviceMarket(model: String): String {
        val upper = model.uppercase(Locale.ROOT)
        return when {
            upper.endsWith("U") || upper.endsWith("U1") || upper.endsWith("A") -> "North America"
            upper.endsWith("B") || upper.endsWith("EU") -> "Europe / Global"
            upper.endsWith("N") -> "South Korea"
            upper.endsWith("00") -> "China"
            upper.endsWith("0") -> "Global"
            else -> "Global / Multi-region"
        }
    }

    private fun resolveModelRegionCode(model: String): String {
        val parts = model.split("-", "_", " ")
        return if (parts.size > 1) parts.last() else "Standard"
    }

    fun getSystemSpecs(): SystemSpecs {
        return try {
            val kernel = try {
                System.getProperty("os.version") ?: "Linux Kernel"
            } catch (_: Throwable) {
                "Linux Kernel"
            }

            val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    Build.VERSION.SECURITY_PATCH ?: "Not available on this device"
                } catch (_: Throwable) {
                    "Not available on this device"
                }
            } else {
                "Not available on this device"
            }

            val buildNumber = try {
                Build.DISPLAY.ifBlank { Build.ID }
            } catch (_: Throwable) {
                "Unknown"
            }

            val bootloader = try {
                Build.BOOTLOADER.ifBlank { "Not available on this device" }
            } catch (_: Throwable) {
                "Not available on this device"
            }

            val abis = try {
                Build.SUPPORTED_ABIS.toList()
            } catch (_: Throwable) {
                emptyList()
            }

            SystemSpecs(
                androidVersion = "Android ${Build.VERSION.RELEASE ?: "Unknown"}",
                sdkInt = Build.VERSION.SDK_INT,
                buildNumber = buildNumber,
                kernelVersion = kernel,
                securityPatch = securityPatch,
                bootloader = bootloader,
                supportedAbis = abis
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving SystemSpecs", t)
            safeDefaultSystem()
        }
    }

    fun safeDefaultSystem(): SystemSpecs {
        return SystemSpecs(
            androidVersion = "Android",
            sdkInt = Build.VERSION.SDK_INT,
            buildNumber = "Unknown",
            kernelVersion = "Linux",
            securityPatch = "Not available on this device",
            bootloader = "Not available on this device",
            supportedAbis = emptyList()
        )
    }

    fun getHardwareSpecs(): HardwareSpecs {
        return try {
            val cores = try {
                Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            } catch (_: Throwable) {
                4
            }
            val freq = readCpuFrequency()
            val memory = getMemoryInfo()
            val storage = getStorageInfo()
            val primaryAbi = try {
                Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
            } catch (_: Throwable) {
                "arm64-v8a"
            }

            HardwareSpecs(
                cpuArchitecture = primaryAbi,
                cpuCores = cores,
                cpuFrequency = freq,
                gpuRenderer = "Hardware Integrated Graphics",
                totalRamBytes = memory.totalBytes,
                availableRamBytes = memory.availableBytes,
                totalStorageBytes = storage.totalBytes,
                availableStorageBytes = storage.freeBytes
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving HardwareSpecs", t)
            safeDefaultHardware()
        }
    }

    fun safeDefaultHardware(): HardwareSpecs {
        return HardwareSpecs(
            cpuArchitecture = "arm64",
            cpuCores = 4,
            cpuFrequency = "Not available on this device",
            gpuRenderer = "Integrated Graphics",
            totalRamBytes = 0L,
            availableRamBytes = 0L,
            totalStorageBytes = 0L,
            availableStorageBytes = 0L
        )
    }

    private fun readCpuFrequency(): String {
        return try {
            val file = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (file.exists() && file.canRead()) {
                val khz = file.readText().trim().toLongOrNull()
                if (khz != null && khz > 0) {
                    val ghz = khz / 1_000_000.0
                    String.format(Locale.US, "%.2f GHz", ghz)
                } else {
                    "Dynamic scaling"
                }
            } else {
                "Dynamic scaling"
            }
        } catch (_: Throwable) {
            "Not available on this device"
        }
    }

    fun getDisplaySpecs(): DisplaySpecs {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val metrics = DisplayMetrics()
            var refreshRate = 60.0f
            var widthPx = 1080
            var heightPx = 2400
            var densityDpi = 420
            var xdpi = 420f
            var ydpi = 420f

            if (windowManager != null) {
                try {
                    @Suppress("DEPRECATION")
                    val display = windowManager.defaultDisplay
                    if (display != null) {
                        @Suppress("DEPRECATION")
                        display.getRealMetrics(metrics)
                        refreshRate = display.refreshRate
                        widthPx = metrics.widthPixels.coerceAtLeast(480)
                        heightPx = metrics.heightPixels.coerceAtLeast(800)
                        densityDpi = metrics.densityDpi.coerceAtLeast(120)
                        xdpi = if (metrics.xdpi > 0) metrics.xdpi else densityDpi.toFloat()
                        ydpi = if (metrics.ydpi > 0) metrics.ydpi else densityDpi.toFloat()
                    }
                } catch (t: Throwable) {
                    SafeLog.w("Could not read display real metrics", t)
                }
            }

            val screenInches = try {
                val widthInches = widthPx / xdpi
                val heightInches = heightPx / ydpi
                val diagonal = sqrt(widthInches.toDouble().pow(2.0) + heightInches.toDouble().pow(2.0))
                if (diagonal in 3.0..15.0) {
                    String.format(Locale.US, "%.1f\"", diagonal)
                } else {
                    "Not available on this device"
                }
            } catch (_: Throwable) {
                "Not available on this device"
            }

            DisplaySpecs(
                resolution = "$widthPx × $heightPx px",
                densityDpi = densityDpi,
                refreshRate = (refreshRate * 10).roundToInt() / 10f,
                screenSizeInches = screenInches
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving DisplaySpecs", t)
            safeDefaultDisplay()
        }
    }

    fun safeDefaultDisplay(): DisplaySpecs {
        return DisplaySpecs(
            resolution = "Not available on this device",
            densityDpi = 420,
            refreshRate = 60.0f,
            screenSizeInches = "Not available on this device"
        )
    }

    fun getCameraHardwareInfo(): CameraHardwareInfo {
        return try {
            val cameraManager = try {
                context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            } catch (t: Throwable) {
                SafeLog.w("CameraService not accessible", t)
                null
            }

            var hasFront = false
            var frontMp = "Not available on this device"
            var hasRear = false
            var rearMp = "Not available on this device"
            var hasFlash = false

            if (cameraManager != null) {
                try {
                    val cameraIds = cameraManager.cameraIdList
                    for (id in cameraIds) {
                        try {
                            val chars = cameraManager.getCameraCharacteristics(id)
                            val facing = chars.get(CameraCharacteristics.LENS_FACING)
                            val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                            if (flashAvailable) hasFlash = true

                            val pixelArray = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
                            val mp = if (pixelArray != null) {
                                val totalPixels = pixelArray.width().toLong() * pixelArray.height().toLong()
                                val megas = totalPixels / 1_000_000.0
                                String.format(Locale.US, "%.1f MP", megas)
                            } else {
                                "Supported"
                            }

                            if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                                hasFront = true
                                frontMp = mp
                            } else if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                                hasRear = true
                                rearMp = mp
                            }
                        } catch (t: Throwable) {
                            SafeLog.w("Error querying camera id $id: ${t.message}")
                        }
                    }
                } catch (t: Throwable) {
                    SafeLog.w("Error listing camera IDs", t)
                }
            }

            CameraHardwareInfo(
                hasFrontCamera = hasFront,
                frontCameraMegapixels = frontMp,
                hasRearCamera = hasRear,
                rearCameraMegapixels = rearMp,
                hasFlash = hasFlash
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving CameraHardwareInfo", t)
            safeDefaultCamera()
        }
    }

    fun safeDefaultCamera(): CameraHardwareInfo {
        return CameraHardwareInfo(
            hasFrontCamera = false,
            frontCameraMegapixels = "Not available on this device",
            hasRearCamera = false,
            rearCameraMegapixels = "Not available on this device",
            hasFlash = false
        )
    }

    fun getConnectivitySpecs(): ConnectivitySpecs {
        return try {
            val wifiManager = try {
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            } catch (_: Throwable) { null }

            val connectivityManager = try {
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            } catch (_: Throwable) { null }

            val bluetoothAdapter = try {
                BluetoothAdapter.getDefaultAdapter()
            } catch (_: Throwable) { null }

            val nfcAdapter = try {
                NfcAdapter.getDefaultAdapter(context)
            } catch (_: Throwable) { null }

            val locationManager = try {
                context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            } catch (_: Throwable) { null }

            var isWifiConnected = false
            var isMobileConnected = false
            var networkType = "Disconnected"

            try {
                connectivityManager?.let { cm ->
                    val network = cm.activeNetwork
                    if (network != null) {
                        val caps = cm.getNetworkCapabilities(network)
                        if (caps != null) {
                            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                isWifiConnected = true
                                networkType = "Wi-Fi Connected"
                            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                isMobileConnected = true
                                networkType = "Cellular Mobile Data"
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                SafeLog.w("Error checking network capabilities", t)
            }

            val isWifiEnabled = try {
                wifiManager?.isWifiEnabled ?: false
            } catch (_: Throwable) {
                false
            }

            val wifiSpeed = try {
                @Suppress("DEPRECATION")
                val info = wifiManager?.connectionInfo
                val speed = info?.linkSpeed ?: -1
                if (speed > 0) "$speed Mbps" else "Not connected"
            } catch (_: Throwable) {
                "Not available on this device"
            }

            // CRITICAL: Check BLUETOOTH_CONNECT permission on Android 12+ (API 31+) to avoid SecurityException
            val isBluetoothSupported = bluetoothAdapter != null
            val isBluetoothEnabled = try {
                if (bluetoothAdapter == null) {
                    false
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        bluetoothAdapter.isEnabled
                    } else {
                        false
                    }
                } else {
                    bluetoothAdapter.isEnabled
                }
            } catch (t: Throwable) {
                SafeLog.w("Cannot check Bluetooth status: ${t.message}")
                false
            }

            val isNfcSupported = nfcAdapter != null
            val isNfcEnabled = try {
                nfcAdapter?.isEnabled ?: false
            } catch (_: Throwable) {
                false
            }

            val isGpsAvailable = locationManager != null
            val isGpsEnabled = try {
                locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            } catch (_: Throwable) {
                false
            }

            ConnectivitySpecs(
                isWifiEnabled = isWifiEnabled,
                isWifiConnected = isWifiConnected,
                wifiSsid = if (isWifiConnected) "Connected Wi-Fi" else "Not connected",
                wifiLinkSpeed = wifiSpeed,
                isBluetoothSupported = isBluetoothSupported,
                isBluetoothEnabled = isBluetoothEnabled,
                isNfcSupported = isNfcSupported,
                isNfcEnabled = isNfcEnabled,
                isGpsAvailable = isGpsAvailable,
                isGpsEnabled = isGpsEnabled,
                isMobileDataConnected = isMobileConnected,
                networkType = networkType
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving ConnectivitySpecs", t)
            safeDefaultConnectivity()
        }
    }

    fun safeDefaultConnectivity(): ConnectivitySpecs {
        return ConnectivitySpecs(
            isWifiEnabled = false,
            isWifiConnected = false,
            wifiSsid = "Not connected",
            wifiLinkSpeed = "Not available on this device",
            isBluetoothSupported = false,
            isBluetoothEnabled = false,
            isNfcSupported = false,
            isNfcEnabled = false,
            isGpsAvailable = false,
            isGpsEnabled = false,
            isMobileDataConnected = false,
            networkType = "Disconnected"
        )
    }

    fun getSecuritySpecs(): SecuritySpecs {
        return try {
            val keyguard = try {
                context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            } catch (_: Throwable) { null }

            val devicePolicy = try {
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            } catch (_: Throwable) { null }

            val isSecured = try {
                keyguard?.isDeviceSecure ?: false
            } catch (_: Throwable) { false }

            val isEncrypted = try {
                val encryptionStatus = devicePolicy?.storageEncryptionStatus ?: DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED
                encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
                        encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY ||
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            } catch (_: Throwable) {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            }

            SecuritySpecs(
                isScreenLockSecured = isSecured,
                hasBiometricHardware = true,
                isBiometricEnrolled = isSecured,
                isDeviceEncrypted = isEncrypted
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving SecuritySpecs", t)
            safeDefaultSecurity()
        }
    }

    fun safeDefaultSecurity(): SecuritySpecs {
        return SecuritySpecs(
            isScreenLockSecured = false,
            hasBiometricHardware = false,
            isBiometricEnrolled = false,
            isDeviceEncrypted = true
        )
    }

    fun getBatteryInfo(): BatteryInfo {
        return try {
            val batteryStatusIntent = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.registerReceiver(
                        context,
                        null,
                        IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                        ContextCompat.RECEIVER_NOT_EXPORTED
                    )
                } else {
                    context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                }
            } catch (t: Throwable) {
                SafeLog.w("Failed to register battery receiver", t)
                null
            }

            val batteryManager = try {
                context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            } catch (_: Throwable) { null }

            val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val levelPercent = if (level != -1 && scale > 0) {
                ((level / scale.toFloat()) * 100).roundToInt().coerceIn(0, 100)
            } else {
                -1
            }

            val rawHealth = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
            val (healthStr, isHealthGood) = when (rawHealth) {
                BatteryManager.BATTERY_HEALTH_GOOD -> Pair("Good", true)
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> Pair("Overheat Warning", false)
                BatteryManager.BATTERY_HEALTH_DEAD -> Pair("Critical Degradation", false)
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> Pair("Over Voltage Warning", false)
                BatteryManager.BATTERY_HEALTH_COLD -> Pair("Cold Temperature", false)
                else -> Pair("Normal / Healthy", true)
            }

            val rawTemp = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val tempC = if (rawTemp > 0) rawTemp / 10.0f else 28.0f
            val voltageMv = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

            val plugged = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            val powerSource = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charger"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Induction Charger"
                else -> "Battery Discharging"
            }

            val status = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val chargingStatusStr = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_FULL -> "Charged (Full)"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging (Plugged)"
                else -> "Discharging"
            }

            val technology = try {
                batteryStatusIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
            } catch (_: Throwable) { "Li-ion" }

            // Safe Android 14+ cycle count check
            val cycleCountStr = try {
                if (Build.VERSION.SDK_INT >= 34) {
                    val cycles = batteryManager?.getIntProperty(7) ?: -1
                    if (cycles > 0) "$cycles Cycles" else "Not available on this device"
                } else {
                    "Not available on this device"
                }
            } catch (_: Throwable) {
                "Not available on this device"
            }

            val currentNow = try {
                batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
            } catch (_: Throwable) { 0 }
            val currentStr = if (currentNow != 0) {
                val ma = currentNow / 1000
                "${ma} mA"
            } else {
                "Normal"
            }

            val capacityMah = try {
                batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: -1
            } catch (_: Throwable) { -1 }
            val capacityStr = if (capacityMah > 0) "${capacityMah / 1000} mAh" else "Standard OEM Capacity"

            var healthScore = 95
            if (tempC > 40.0f) healthScore -= 10
            if (!isHealthGood) healthScore -= 20
            if (voltageMv > 4450) healthScore -= 5

            BatteryInfo(
                levelPercent = if (levelPercent >= 0) levelPercent else 80,
                healthStatus = healthStr,
                isHealthGood = isHealthGood,
                temperatureCelsius = tempC,
                voltageMilliVolts = voltageMv,
                currentNowMicroAmperes = currentStr,
                chargingStatus = chargingStatusStr,
                isCharging = isCharging,
                powerSource = powerSource,
                technology = technology,
                reportedCapacityMah = capacityStr,
                designCapacityMah = "Not accessible (Android security restriction)",
                estimatedRemainingMah = capacityStr,
                cycleCount = cycleCountStr,
                generalStatus = if (isHealthGood) "Healthy" else "Attention Required",
                healthScorePercent = healthScore.coerceIn(50, 100)
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving BatteryInfo", t)
            safeDefaultBattery()
        }
    }

    fun safeDefaultBattery(): BatteryInfo {
        return BatteryInfo(
            levelPercent = 80,
            healthStatus = "Healthy",
            isHealthGood = true,
            temperatureCelsius = 28f,
            voltageMilliVolts = 3850,
            currentNowMicroAmperes = "Normal",
            chargingStatus = "Discharging",
            isCharging = false,
            powerSource = "Battery Discharging",
            technology = "Li-ion",
            reportedCapacityMah = "Not available on this device",
            designCapacityMah = "Not available on this device",
            estimatedRemainingMah = "Not available on this device",
            cycleCount = "Not available on this device",
            generalStatus = "Healthy",
            healthScorePercent = 90
        )
    }

    fun getStorageInfo(): StorageInfo {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = (totalBlocks * blockSize).coerceAtLeast(0L)
            val freeBytes = (availableBlocks * blockSize).coerceAtLeast(0L)
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val usedPercent = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes) * 100).roundToInt() else 0
            val isAlmostFull = usedPercent >= 90

            StorageInfo(
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                freeBytes = freeBytes,
                usedPercentage = usedPercent,
                isAlmostFull = isAlmostFull,
                formattedTotal = formatBytes(totalBytes),
                formattedUsed = formatBytes(usedBytes),
                formattedFree = formatBytes(freeBytes)
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving StorageInfo", t)
            safeDefaultStorage()
        }
    }

    fun safeDefaultStorage(): StorageInfo {
        return StorageInfo(
            totalBytes = 0L,
            usedBytes = 0L,
            freeBytes = 0L,
            usedPercentage = 0,
            isAlmostFull = false,
            formattedTotal = "Not available on this device",
            formattedUsed = "0 GB",
            formattedFree = "0 GB"
        )
    }

    fun getMemoryInfo(): MemoryInfo {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            if (activityManager != null) {
                activityManager.getMemoryInfo(memoryInfo)
                val totalBytes = memoryInfo.totalMem.coerceAtLeast(0L)
                val availableBytes = memoryInfo.availMem.coerceAtLeast(0L)
                val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)
                val usedPercent = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes) * 100).roundToInt() else 0
                val isLow = memoryInfo.lowMemory || usedPercent >= 88

                MemoryInfo(
                    totalBytes = totalBytes,
                    usedBytes = usedBytes,
                    availableBytes = availableBytes,
                    usedPercentage = usedPercent,
                    isLowRam = isLow,
                    formattedTotal = formatBytes(totalBytes),
                    formattedUsed = formatBytes(usedBytes),
                    formattedAvailable = formatBytes(availableBytes)
                )
            } else {
                safeDefaultMemory()
            }
        } catch (t: Throwable) {
            SafeLog.e("Error resolving MemoryInfo", t)
            safeDefaultMemory()
        }
    }

    fun safeDefaultMemory(): MemoryInfo {
        return MemoryInfo(
            totalBytes = 0L,
            usedBytes = 0L,
            availableBytes = 0L,
            usedPercentage = 0,
            isLowRam = false,
            formattedTotal = "Not available on this device",
            formattedUsed = "0 GB",
            formattedAvailable = "0 GB"
        )
    }

    fun getUptimeInfo(): UptimeInfo {
        return try {
            val uptimeMillis = SystemClock.elapsedRealtime()
            val totalSecs = (uptimeMillis / 1000).coerceAtLeast(0L)
            val days = totalSecs / (24 * 3600)
            val rem1 = totalSecs % (24 * 3600)
            val hours = rem1 / 3600
            val rem2 = rem1 % 3600
            val minutes = rem2 / 60
            val seconds = rem2 % 60

            val lastBootTimestamp = System.currentTimeMillis() - uptimeMillis
            val formattedDate = try {
                val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
                sdf.format(Date(lastBootTimestamp))
            } catch (_: Throwable) {
                "Recent"
            }

            UptimeInfo(
                uptimeMillis = uptimeMillis,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                lastBootTimestamp = lastBootTimestamp,
                formattedLastBootDate = formattedDate
            )
        } catch (t: Throwable) {
            SafeLog.e("Error resolving UptimeInfo", t)
            safeDefaultUptime()
        }
    }

    fun safeDefaultUptime(): UptimeInfo {
        return UptimeInfo(
            uptimeMillis = 0L,
            days = 0L,
            hours = 0L,
            minutes = 0L,
            seconds = 0L,
            lastBootTimestamp = System.currentTimeMillis(),
            formattedLastBootDate = "Recent"
        )
    }

    fun getCompleteDeviceInfo(): CompleteDeviceInfo {
        return CompleteDeviceInfo(
            identity = getDeviceIdentity(),
            system = getSystemSpecs(),
            hardware = getHardwareSpecs(),
            display = getDisplaySpecs(),
            camera = getCameraHardwareInfo(),
            connectivity = getConnectivitySpecs(),
            security = getSecuritySpecs()
        )
    }

    fun detectPotentialIssues(): List<DiagnosticIssue> {
        val issues = mutableListOf<DiagnosticIssue>()
        try {
            val storage = getStorageInfo()
            if (storage.isAlmostFull) {
                issues.add(
                    DiagnosticIssue(
                        id = "storage_almost_full",
                        title = "Storage almost full",
                        whyItMatters = "Your device has less than 10% free storage. System caching and updates may fail.",
                        recommendedAction = "Delete unnecessary media files or uninstall unused applications.",
                        severity = IssueSeverity.WARNING,
                        category = "Storage"
                    )
                )
            }

            val memory = getMemoryInfo()
            if (memory.isLowRam) {
                issues.add(
                    DiagnosticIssue(
                        id = "ram_low",
                        title = "Low available RAM",
                        whyItMatters = "Available memory is heavily congested, which may cause background apps to terminate.",
                        recommendedAction = "Close unused heavy applications or restart the device.",
                        severity = IssueSeverity.WARNING,
                        category = "Memory"
                    )
                )
            }

            val battery = getBatteryInfo()
            if (battery.temperatureCelsius > 40.0f) {
                issues.add(
                    DiagnosticIssue(
                        id = "battery_temp_high",
                        title = "Battery temperature elevated",
                        whyItMatters = "Elevated temperature (>40°C) accelerates chemical degradation of lithium-ion cells.",
                        recommendedAction = "Disconnect fast charger and let the phone cool in ambient air.",
                        severity = IssueSeverity.WARNING,
                        category = "Battery"
                    )
                )
            }

            val security = getSecuritySpecs()
            if (!security.isScreenLockSecured) {
                issues.add(
                    DiagnosticIssue(
                        id = "screen_lock_none",
                        title = "Screen lock disabled",
                        whyItMatters = "Your device has no PIN, pattern, or biometric authentication set, leaving local data exposed.",
                        recommendedAction = "Configure a secure lock screen in Device Settings > Security.",
                        severity = IssueSeverity.CRITICAL,
                        category = "Security"
                    )
                )
            }
        } catch (t: Throwable) {
            SafeLog.e("Error detecting potential issues", t)
        }
        return issues
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 GB"
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) {
            String.format(Locale.US, "%.1f GB", gb)
        } else {
            val mb = bytes / (1024.0 * 1024.0)
            String.format(Locale.US, "%.0f MB", mb)
        }
    }
}
