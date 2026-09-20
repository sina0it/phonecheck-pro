package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.StatusHealthyGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.viewmodel.DiagnosticsViewModel

@Composable
fun DeviceScreen(
    viewModel: DiagnosticsViewModel,
    modifier: Modifier = Modifier
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val uptimeInfo by viewModel.uptimeInfo.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val accent = LocalAccentColor.current

    val onCopyValue: (String, String) -> Unit = { label, value ->
        clipboardManager.setText(AnnotatedString(value))
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.device_information),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${deviceInfo.identity.brand} ${deviceInfo.identity.model} • ${deviceInfo.system.androidVersion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section: Device Identity
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.device_identity),
                icon = Icons.Default.PhoneAndroid,
                rows = listOf(
                    stringResource(R.string.brand) to deviceInfo.identity.brand,
                    stringResource(R.string.manufacturer) to deviceInfo.identity.manufacturer,
                    stringResource(R.string.exact_model) to deviceInfo.identity.exactModelName,
                    stringResource(R.string.device_name) to deviceInfo.identity.deviceName,
                    stringResource(R.string.product_name) to deviceInfo.identity.productName,
                    stringResource(R.string.device_family) to deviceInfo.identity.deviceFamily,
                    stringResource(R.string.device_id) to deviceInfo.identity.deviceId
                ),
                onRowClick = onCopyValue
            )
        }

        // Section: Manufacturing & Origin
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.manufacturing_region),
                icon = Icons.Default.Language,
                rows = listOf(
                    stringResource(R.string.brand_origin) to deviceInfo.identity.brandOrigin,
                    stringResource(R.string.assembly_country) to deviceInfo.identity.assemblyCountry,
                    stringResource(R.string.release_year) to deviceInfo.identity.releaseYear,
                    stringResource(R.string.manufacturing_region_name) to deviceInfo.identity.manufacturingRegion,
                    stringResource(R.string.device_market) to deviceInfo.identity.deviceMarket,
                    stringResource(R.string.model_region_code) to deviceInfo.identity.modelRegionCode
                ),
                onRowClick = onCopyValue
            )
        }

        // Section: Uptime Details
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.device_uptime),
                icon = Icons.Default.Schedule,
                rows = listOf(
                    stringResource(R.string.current_uptime) to "${uptimeInfo.days} Days, ${uptimeInfo.hours} Hours, ${uptimeInfo.minutes} Min",
                    stringResource(R.string.last_boot_time) to uptimeInfo.formattedLastBootDate,
                    stringResource(R.string.time_since_boot) to "${uptimeInfo.uptimeMillis / 1000 / 60} minutes total"
                ),
                onRowClick = onCopyValue
            )
        }

        // Section: Hardware Specifications
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.hardware_specifications),
                icon = Icons.Default.DeveloperBoard,
                rows = listOf(
                    stringResource(R.string.cpu_architecture) to deviceInfo.hardware.cpuArchitecture,
                    stringResource(R.string.cpu_cores) to "${deviceInfo.hardware.cpuCores} Cores",
                    stringResource(R.string.cpu_frequency) to deviceInfo.hardware.cpuFrequency,
                    stringResource(R.string.gpu) to deviceInfo.hardware.gpuRenderer,
                    stringResource(R.string.total_ram) to formatBytes(deviceInfo.hardware.totalRamBytes),
                    stringResource(R.string.total_storage) to formatBytes(deviceInfo.hardware.totalStorageBytes)
                ),
                onRowClick = onCopyValue
            )
        }

        // Section: System & OS
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.system_os),
                icon = Icons.Default.Smartphone,
                rows = listOf(
                    stringResource(R.string.android_version) to deviceInfo.system.androidVersion,
                    stringResource(R.string.android_sdk) to "API ${deviceInfo.system.sdkInt}",
                    stringResource(R.string.build_number) to deviceInfo.system.buildNumber,
                    stringResource(R.string.kernel_version) to deviceInfo.system.kernelVersion,
                    stringResource(R.string.security_patch) to deviceInfo.system.securityPatch,
                    stringResource(R.string.bootloader) to deviceInfo.system.bootloader
                ),
                onRowClick = onCopyValue
            )
        }

        // Section: Display Specs
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.display_specs),
                icon = Icons.Default.Devices,
                rows = listOf(
                    stringResource(R.string.screen_resolution) to deviceInfo.display.resolution,
                    stringResource(R.string.screen_density) to "${deviceInfo.display.densityDpi} DPI",
                    stringResource(R.string.refresh_rate) to "${deviceInfo.display.refreshRate} Hz",
                    stringResource(R.string.screen_size) to deviceInfo.display.screenSizeInches
                ),
                onRowClick = onCopyValue
            )
        }

        // Section: Camera Specs
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.camera_specs),
                icon = Icons.Default.CameraAlt,
                rows = listOf(
                    stringResource(R.string.rear_camera) to if (deviceInfo.camera.hasRearCamera) deviceInfo.camera.rearCameraMegapixels else "Not Present",
                    stringResource(R.string.front_camera) to if (deviceInfo.camera.hasFrontCamera) deviceInfo.camera.frontCameraMegapixels else "Not Present",
                    stringResource(R.string.test_flash) to if (deviceInfo.camera.hasFlash) "Available (Hardware LED)" else "Not Available"
                ),
                onRowClick = onCopyValue
            )
        }

        // Section: Connectivity & Network
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.connectivity),
                icon = Icons.Default.Wifi,
                rows = listOf(
                    stringResource(R.string.wifi) to if (deviceInfo.connectivity.isWifiConnected) "Connected (${deviceInfo.connectivity.wifiLinkSpeed})" else if (deviceInfo.connectivity.isWifiEnabled) "Enabled (Idle)" else "Disabled",
                    stringResource(R.string.bluetooth) to if (deviceInfo.connectivity.isBluetoothEnabled) "Active" else "Disabled / Off",
                    stringResource(R.string.nfc) to if (deviceInfo.connectivity.isNfcSupported) (if (deviceInfo.connectivity.isNfcEnabled) "Active" else "Disabled") else "Not Supported on Device",
                    stringResource(R.string.gps) to if (deviceInfo.connectivity.isGpsEnabled) "Active & Locked" else "Disabled",
                    stringResource(R.string.mobile_network) to deviceInfo.connectivity.networkType
                ),
                onRowClick = onCopyValue
            )
        }

        // Section: Security Center
        item {
            DeviceSpecCard(
                sectionTitle = stringResource(R.string.security_center),
                icon = Icons.Default.Security,
                rows = listOf(
                    stringResource(R.string.screen_lock) to if (deviceInfo.security.isScreenLockSecured) stringResource(R.string.secured) else stringResource(R.string.not_secured),
                    stringResource(R.string.biometrics) to if (deviceInfo.security.isBiometricEnrolled) "Enrolled & Protected" else "Not Configured",
                    stringResource(R.string.encryption) to if (deviceInfo.security.isDeviceEncrypted) stringResource(R.string.encrypted) else "Standard Protection"
                ),
                onRowClick = onCopyValue
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DeviceSpecCard(
    sectionTitle: String,
    icon: ImageVector,
    rows: List<Pair<String, String>>,
    onRowClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalAccentColor.current

    LuxuryGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = sectionTitle,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.1f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.3f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }

                if (index < rows.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 0.8.dp
                    )
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) {
        String.format(java.util.Locale.US, "%.1f GB", gb)
    } else {
        val mb = bytes / (1024.0 * 1024.0)
        String.format(java.util.Locale.US, "%.0f MB", mb)
    }
}
