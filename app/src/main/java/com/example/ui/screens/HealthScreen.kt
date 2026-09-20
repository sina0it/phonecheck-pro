package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IssueCard
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.LuxuryRingProgress
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.StatusHealthyGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.viewmodel.DiagnosticsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HealthScreen(
    viewModel: DiagnosticsViewModel,
    modifier: Modifier = Modifier
) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val healthScore by viewModel.healthScore.collectAsState()
    val issues by viewModel.issues.collectAsState()
    val history by viewModel.scanHistory.collectAsState()
    val latestTwo by viewModel.latestTwoRecords.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val accent = LocalAccentColor.current
    val context = LocalContext.current

    val onShareReport = {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val shareBody = """
            📱 PhoneCheck Pro Diagnostic Report
            ------------------------------------
            Device: ${deviceInfo.identity.manufacturer} ${deviceInfo.identity.model}
            Health Score: $healthScore/100
            Status: ${if (healthScore >= 80) "Optimal Health" else "Attention Required"}
            Date: $dateStr
            
            🔋 Battery Diagnostics:
            • Level: ${batteryInfo.levelPercent}%
            • Health: ${batteryInfo.healthStatus}
            • Temperature: ${batteryInfo.temperatureCelsius}°C
            • Voltage: ${batteryInfo.voltageMilliVolts} mV
            • Status: ${batteryInfo.chargingStatus}
            • Cycle Count: ${batteryInfo.cycleCount}
            
            ⚠️ Detected Issues: ${issues.size}
            ${issues.joinToString("\n") { "• ${it.title} (${it.severity}): ${it.recommendedAction}" }}
            
            Verified by PhoneCheck Pro (Developed by Sina Naderi)
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareBody)
            putExtra(Intent.EXTRA_SUBJECT, "PhoneCheck Pro Diagnostic Report")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Phone Health Report")
        context.startActivity(shareIntent)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.phone_health_report),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time battery and hardware health diagnostics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onShareReport,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_report),
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section: Battery Health Hero Ring
        item {
            LuxuryGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                hasGlow = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LuxuryRingProgress(
                        score = batteryInfo.healthScorePercent,
                        subText = stringResource(R.string.battery_health)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${batteryInfo.levelPercent}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.battery_level),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${batteryInfo.temperatureCelsius}°C",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (batteryInfo.temperatureCelsius > 40) StatusWarningAmber else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.battery_temperature),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${batteryInfo.voltageMilliVolts} mV",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.battery_voltage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: Comprehensive Battery Metrics List
        item {
            LuxuryGlassCard(modifier = Modifier.fillMaxWidth()) {
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
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = "Battery",
                                tint = accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.battery_health),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    val rows = listOf(
                        stringResource(R.string.charging_status) to batteryInfo.chargingStatus,
                        stringResource(R.string.power_source) to batteryInfo.powerSource,
                        stringResource(R.string.battery_current) to batteryInfo.currentNowMicroAmperes,
                        stringResource(R.string.battery_capacity) to batteryInfo.reportedCapacityMah,
                        stringResource(R.string.design_capacity) to batteryInfo.designCapacityMah,
                        stringResource(R.string.remaining_capacity) to batteryInfo.estimatedRemainingMah,
                        stringResource(R.string.battery_technology) to batteryInfo.technology,
                        stringResource(R.string.cycle_count) to batteryInfo.cycleCount,
                        stringResource(R.string.battery_status) to batteryInfo.generalStatus
                    )

                    rows.forEachIndexed { index, (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1.2f),
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

        // Section: Battery Education Note
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.battery_info_note),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section: Compare Latest vs Previous Scan (if at least 2 records exist)
        if (latestTwo.size >= 2) {
            val latest = latestTwo[0]
            val previous = latestTwo[1]
            item {
                Text(
                    text = stringResource(R.string.compare_results),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                LuxuryGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.latest),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent
                                )
                                Text(
                                    text = "${latest.healthScore}%",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (latest.healthScore >= 80) StatusHealthyGreen else StatusWarningAmber
                                )
                                Text(
                                    text = "${latest.issuesCount} Issues",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Compare",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.CenterVertically)
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.previous),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${previous.healthScore}%",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (previous.healthScore >= 80) StatusHealthyGreen else StatusWarningAmber
                                )
                                Text(
                                    text = "${previous.issuesCount} Issues",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Diagnostic History from Room
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.test_history),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (history.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (history.isEmpty()) {
            item {
                LuxuryGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No history",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.no_history_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.run_first_scan),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(history) { record ->
                val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
                val dateStr = sdf.format(Date(record.timestamp))

                LuxuryGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.statusSummary,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (record.healthScore >= 80) StatusHealthyGreen else StatusWarningAmber
                            )
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Battery: ${record.batteryLevel}% (${record.batteryTemp}°C) • ${record.issuesCount} warnings",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(accent.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${record.healthScore}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = accent
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
