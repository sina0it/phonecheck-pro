package com.example.data.model

data class DeviceIdentity(
    val brand: String,
    val manufacturer: String,
    val model: String,
    val exactModelName: String,
    val modelNumber: String,
    val deviceName: String,
    val productName: String,
    val deviceFamily: String,
    val deviceId: String,
    val brandOrigin: String,
    val assemblyCountry: String,
    val releaseYear: String,
    val manufacturingRegion: String,
    val deviceMarket: String,
    val modelRegionCode: String
)

data class SystemSpecs(
    val androidVersion: String,
    val sdkInt: Int,
    val buildNumber: String,
    val kernelVersion: String,
    val securityPatch: String,
    val bootloader: String,
    val supportedAbis: List<String>
)

data class HardwareSpecs(
    val cpuArchitecture: String,
    val cpuCores: Int,
    val cpuFrequency: String,
    val gpuRenderer: String,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long
)

data class DisplaySpecs(
    val resolution: String,
    val densityDpi: Int,
    val refreshRate: Float,
    val screenSizeInches: String
)

data class CameraHardwareInfo(
    val hasFrontCamera: Boolean,
    val frontCameraMegapixels: String,
    val hasRearCamera: Boolean,
    val rearCameraMegapixels: String,
    val hasFlash: Boolean
)

data class ConnectivitySpecs(
    val isWifiEnabled: Boolean,
    val isWifiConnected: Boolean,
    val wifiSsid: String,
    val wifiLinkSpeed: String,
    val isBluetoothSupported: Boolean,
    val isBluetoothEnabled: Boolean,
    val isNfcSupported: Boolean,
    val isNfcEnabled: Boolean,
    val isGpsAvailable: Boolean,
    val isGpsEnabled: Boolean,
    val isMobileDataConnected: Boolean,
    val networkType: String
)

data class SecuritySpecs(
    val isScreenLockSecured: Boolean,
    val hasBiometricHardware: Boolean,
    val isBiometricEnrolled: Boolean,
    val isDeviceEncrypted: Boolean
)

data class CompleteDeviceInfo(
    val identity: DeviceIdentity,
    val system: SystemSpecs,
    val hardware: HardwareSpecs,
    val display: DisplaySpecs,
    val camera: CameraHardwareInfo,
    val connectivity: ConnectivitySpecs,
    val security: SecuritySpecs
)
