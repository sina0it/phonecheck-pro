package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StatusHealthyGreen

@Composable
fun ScannerRadarOverlay(
    currentStepIndex: Int,
    currentStepName: String,
    modifier: Modifier = Modifier
) {
    val accentColor = LocalAccentColor.current
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_anim")

    // Radar scan beam rotation
    val scanAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    // Vertical laser scan line
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    val steps = listOf(
        R.string.step_device,
        R.string.step_battery,
        R.string.step_storage,
        R.string.step_memory,
        R.string.step_cpu,
        R.string.step_sensors,
        R.string.step_connectivity,
        R.string.step_display,
        R.string.step_camera,
        R.string.step_security
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.94f)),
        contentAlignment = Alignment.Center
    ) {
        // Futuristic Radar & Laser Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.38f)
            val maxRadius = size.width * 0.42f

            // Concentric radar rings
            for (i in 1..4) {
                val r = (maxRadius / 4f) * i
                drawCircle(
                    color = accentColor.copy(alpha = 0.15f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // Crosshair lines
            drawLine(
                color = accentColor.copy(alpha = 0.2f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.2f
            )
            drawLine(
                color = accentColor.copy(alpha = 0.2f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.2f
            )

            // Rotating sweep gradient
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = 0.05f),
                        accentColor.copy(alpha = 0.45f)
                    ),
                    center = center
                ),
                startAngle = scanAngle,
                sweepAngle = 60f,
                useCenter = true,
                topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
            )

            // Horizontal Laser Scanline
            val laserOffset = size.height * laserY
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = 0.2f),
                        accentColor,
                        accentColor.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, laserOffset),
                end = Offset(size.width, laserOffset),
                strokeWidth = 3f
            )
        }

        // Foreground status & step indicator
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.device_health_scanner),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.scanning_device),
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor
                )
            }

            // Middle radar spacer
            Spacer(modifier = Modifier.height(180.dp))

            // Bottom Diagnostics Step Pipeline
            LuxuryGlassCard(
                modifier = Modifier.fillMaxWidth(),
                hasGlow = true,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentStepName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = accentColor,
                            strokeWidth = 2.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 10 micro-step indicator pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        steps.forEachIndexed { index, _ ->
                            val isCompleted = index < currentStepIndex
                            val isCurrent = index == currentStepIndex
                            val pillColor = when {
                                isCompleted -> StatusHealthyGreen
                                isCurrent -> accentColor
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(pillColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Stage ${(currentStepIndex + 1).coerceAtMost(10)} / 10 • Performing Real-time Diagnostic Analysis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
