package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IssueCard
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.LuxuryRingProgress
import com.example.ui.components.MetricTile
import com.example.ui.components.ScannerRadarOverlay
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.StatusHealthyGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.viewmodel.DiagnosticsViewModel

@Composable
fun HomeScreen(
    viewModel: DiagnosticsViewModel,
    onNavigateToDevice: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToTests: () -> Unit,
    modifier: Modifier = Modifier
) {
    val healthScore by viewModel.healthScore.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    val memoryInfo by viewModel.memoryInfo.collectAsState()
    val uptimeInfo by viewModel.uptimeInfo.collectAsState()
    val issues by viewModel.issues.collectAsState()
    val scanState by viewModel.scanProgress.collectAsState()
    val accentColor = LocalAccentColor.current

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // App Luxury Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshAllDiagnostics() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Big Central Health Score Hero Card
            item {
                LuxuryGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    hasGlow = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LuxuryRingProgress(
                            score = healthScore,
                            subText = stringResource(R.string.health_score)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status Badge
                        val isHealthy = healthScore >= 80
                        val badgeColor = if (isHealthy) StatusHealthyGreen else StatusWarningAmber
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isHealthy) stringResource(R.string.device_healthy) else stringResource(R.string.issues_detected),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = badgeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // SCAN MY PHONE Action Button
                        Button(
                            onClick = { viewModel.startPhoneScan() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.scan_my_phone),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            // Quick Metrics Section (2x2 Grid)
            item {
                Text(
                    text = stringResource(R.string.quick_metrics),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        title = stringResource(R.string.battery),
                        value = "${batteryInfo.levelPercent}%",
                        subValue = "${batteryInfo.temperatureCelsius}°C",
                        icon = Icons.Default.BatteryFull,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )

                    MetricTile(
                        title = stringResource(R.string.uptime),
                        value = "${uptimeInfo.days}d ${uptimeInfo.hours}h",
                        subValue = uptimeInfo.formattedLastBootDate.take(6),
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDevice
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        title = stringResource(R.string.storage),
                        value = storageInfo.formattedFree,
                        subValue = "${storageInfo.usedPercentage}% used",
                        icon = Icons.Default.Storage,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDevice
                    )

                    MetricTile(
                        title = stringResource(R.string.memory_ram),
                        value = memoryInfo.formattedAvailable,
                        subValue = "${memoryInfo.usedPercentage}% used",
                        icon = Icons.Default.Memory,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDevice
                    )
                }
            }

            // Storage and RAM Progress Bars Bar
            item {
                LuxuryGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.storage),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${storageInfo.formattedUsed} / ${storageInfo.formattedTotal}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { storageInfo.usedPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (storageInfo.isAlmostFull) StatusWarningAmber else accentColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.memory_ram),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${memoryInfo.formattedUsed} / ${memoryInfo.formattedTotal}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { memoryInfo.usedPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (memoryInfo.isLowRam) StatusWarningAmber else StatusHealthyGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Detected Issues Section
            if (issues.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.attention_required),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(issues.size) { index ->
                    IssueCard(issue = issues[index])
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Radar Scanning Overlay
        AnimatedVisibility(
            visible = scanState.isScanning,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ScannerRadarOverlay(
                currentStepIndex = scanState.stepIndex,
                currentStepName = scanState.stepName
            )
        }
    }
}
