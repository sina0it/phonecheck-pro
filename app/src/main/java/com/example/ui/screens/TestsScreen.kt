package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HardwareTestItem
import com.example.data.model.TestStatus
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StatusCriticalRed
import com.example.ui.theme.StatusHealthyGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.viewmodel.DiagnosticsViewModel
import kotlinx.coroutines.launch

@Composable
fun TestsScreen(
    viewModel: DiagnosticsViewModel,
    modifier: Modifier = Modifier
) {
    val tests by viewModel.hardwareTests.collectAsState()
    val sensors by viewModel.sensorReadings.collectAsState()
    val accent = LocalAccentColor.current
    val coroutineScope = rememberCoroutineScope()

    var activeTestId by remember { mutableStateOf<String?>(null) }

    // Live counts
    val passedCount = tests.count { it.status == TestStatus.PASSED }
    val failedCount = tests.count { it.status == TestStatus.FAILED }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            text = stringResource(R.string.hardware_test),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.hardware_test_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Score pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.15f))
                            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$passedCount / ${tests.size} ${stringResource(R.string.passed)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = accent
                        )
                    }
                }
            }

            items(tests) { test ->
                TestListItemCard(
                    test = test,
                    onClick = { activeTestId = test.id }
                )
            }

            // Live Sensors Section inside Tests tab
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sensors),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Live telemetry from hardware sensor bus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                LuxuryGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        sensors.values.forEach { sensor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sensor.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = sensor.displayValue,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (sensor.isAvailable) accent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (sensor.isAvailable) StatusHealthyGreen.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (sensor.isAvailable) stringResource(R.string.available) else stringResource(R.string.unavailable),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (sensor.isAvailable) StatusHealthyGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Active Interactive Test Modal / Fullscreen overlays
        when (activeTestId) {
            "touch" -> {
                InteractiveTouchGridModal(
                    onClose = { activeTestId = null },
                    onComplete = { passed, score ->
                        viewModel.updateTestStatus("touch", if (passed) TestStatus.PASSED else TestStatus.FAILED, "$score% Coverage")
                        activeTestId = null
                    }
                )
            }
            "multitouch" -> {
                InteractiveMultiTouchModal(
                    onClose = { activeTestId = null },
                    onComplete = { passed, maxPts ->
                        viewModel.updateTestStatus("multitouch", if (passed) TestStatus.PASSED else TestStatus.FAILED, "$maxPts Points")
                        activeTestId = null
                    }
                )
            }
            "display" -> {
                InteractiveDisplayModal(
                    onClose = { activeTestId = null },
                    onComplete = { passed ->
                        viewModel.updateTestStatus("display", if (passed) TestStatus.PASSED else TestStatus.FAILED)
                        activeTestId = null
                    }
                )
            }
            "vibration" -> {
                InteractiveVibrationModal(
                    viewModel = viewModel,
                    onClose = { activeTestId = null },
                    onComplete = { passed ->
                        viewModel.updateTestStatus("vibration", if (passed) TestStatus.PASSED else TestStatus.FAILED)
                        activeTestId = null
                    }
                )
            }
            "speaker" -> {
                InteractiveSpeakerModal(
                    viewModel = viewModel,
                    isEarpiece = false,
                    onClose = { activeTestId = null },
                    onComplete = { passed ->
                        viewModel.updateTestStatus("speaker", if (passed) TestStatus.PASSED else TestStatus.FAILED)
                        activeTestId = null
                    }
                )
            }
            "earpiece" -> {
                InteractiveSpeakerModal(
                    viewModel = viewModel,
                    isEarpiece = true,
                    onClose = { activeTestId = null },
                    onComplete = { passed ->
                        viewModel.updateTestStatus("earpiece", if (passed) TestStatus.PASSED else TestStatus.FAILED)
                        activeTestId = null
                    }
                )
            }
            "microphone" -> {
                InteractiveMicrophoneModal(
                    viewModel = viewModel,
                    onClose = { activeTestId = null },
                    onComplete = { passed, rms ->
                        viewModel.updateTestStatus("microphone", if (passed) TestStatus.PASSED else TestStatus.FAILED, "RMS: ${rms.toInt()}")
                        activeTestId = null
                    }
                )
            }
            "flash" -> {
                InteractiveFlashlightModal(
                    viewModel = viewModel,
                    onClose = { activeTestId = null },
                    onComplete = { passed ->
                        viewModel.updateTestStatus("flash", if (passed) TestStatus.PASSED else TestStatus.FAILED)
                        activeTestId = null
                    }
                )
            }
            "camera_rear", "camera_front" -> {
                InteractiveSimpleHardwareModal(
                    title = if (activeTestId == "camera_rear") "Rear Camera Test" else "Front Camera Test",
                    description = "Verify if camera hardware initialization succeeds and sensor reports valid parameters.",
                    onPass = {
                        viewModel.updateTestStatus(activeTestId!!, TestStatus.PASSED)
                        activeTestId = null
                    },
                    onFail = {
                        viewModel.updateTestStatus(activeTestId!!, TestStatus.FAILED)
                        activeTestId = null
                    },
                    onClose = { activeTestId = null }
                )
            }
            "connectivity", "sensors" -> {
                InteractiveSimpleHardwareModal(
                    title = if (activeTestId == "connectivity") "Wireless Connectivity Test" else "Sensors Bus Test",
                    description = "Verifying physical radio controllers, antennas, and I2C/SPI sensor buses.",
                    onPass = {
                        viewModel.updateTestStatus(activeTestId!!, TestStatus.PASSED)
                        activeTestId = null
                    },
                    onFail = {
                        viewModel.updateTestStatus(activeTestId!!, TestStatus.FAILED)
                        activeTestId = null
                    },
                    onClose = { activeTestId = null }
                )
            }
        }
    }
}

@Composable
fun TestListItemCard(
    test: HardwareTestItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalAccentColor.current
    val (badgeColor, badgeText) = when (test.status) {
        TestStatus.NOT_RUN -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, "TEST")
        TestStatus.PASSED -> Pair(StatusHealthyGreen, stringResource(R.string.passed))
        TestStatus.FAILED -> Pair(StatusCriticalRed, stringResource(R.string.failed))
        TestStatus.SKIPPED -> Pair(StatusWarningAmber, stringResource(R.string.skip))
    }

    val iconVector: ImageVector = when (test.iconName) {
        "touch_app" -> Icons.Default.TouchApp
        "pinch" -> Icons.Default.Smartphone
        "smartphone" -> Icons.Default.Smartphone
        "vibration" -> Icons.Default.Vibration
        "volume_up" -> Icons.Default.VolumeUp
        "hearing" -> Icons.Default.Hearing
        "mic" -> Icons.Default.Mic
        "flash_on" -> Icons.Default.FlashOn
        "sensors" -> Icons.Default.Sensors
        "wifi" -> Icons.Default.Wifi
        else -> Icons.Default.CameraAlt
    }

    LuxuryGlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = stringResource(test.titleResId),
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stringResource(test.titleResId),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (test.metricValue != null) {
                        Text(
                            text = test.metricValue,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    } else {
                        Text(
                            text = stringResource(test.descriptionResId),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor
                )
            }
        }
    }
}

// 1. Touch Grid Interactive Test
@Composable
fun InteractiveTouchGridModal(
    onClose: () -> Unit,
    onComplete: (Boolean, Int) -> Unit
) {
    val rows = 12
    val cols = 7
    val totalCells = rows * cols
    val touchedCells = remember { mutableStateListOf<Int>() }
    val accent = LocalAccentColor.current

    val coveragePercent = ((touchedCells.size.toFloat() / totalCells) * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val x = change.position.x
                    val y = change.position.y
                    val colWidth = size.width / cols
                    val rowHeight = size.height / rows
                    val c = (x / colWidth).toInt().coerceIn(0, cols - 1)
                    val r = (y / rowHeight).toInt().coerceIn(0, rows - 1)
                    val index = r * cols + c
                    if (!touchedCells.contains(index)) {
                        touchedCells.add(index)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val colWidth = size.width / cols
                    val rowHeight = size.height / rows
                    val c = (offset.x / colWidth).toInt().coerceIn(0, cols - 1)
                    val r = (offset.y / rowHeight).toInt().coerceIn(0, rows - 1)
                    val index = r * cols + c
                    if (!touchedCells.contains(index)) {
                        touchedCells.add(index)
                    }
                }
            }
    ) {
        // Grid lines and cells
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width / cols
            val cellH = size.height / rows

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val idx = r * cols + c
                    val isTouched = touchedCells.contains(idx)
                    val topLeft = Offset(c * cellW, r * cellH)

                    if (isTouched) {
                        drawRect(
                            color = Color(0xFF00E676).copy(alpha = 0.65f),
                            topLeft = topLeft,
                            size = androidx.compose.ui.geometry.Size(cellW, cellH)
                        )
                    }

                    drawRect(
                        color = Color.White.copy(alpha = 0.15f),
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(cellW, cellH),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )
                }
            }
        }

        // Overlay Instructions and Pass / Fail bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(ObsidianBlack.copy(alpha = 0.88f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${stringResource(R.string.touch_coverage)}: $coveragePercent%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.touch_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onComplete(false, coveragePercent) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCriticalRed)
                ) {
                    Text(stringResource(R.string.fail))
                }
                Button(
                    onClick = { onComplete(coveragePercent >= 75, coveragePercent) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusHealthyGreen, contentColor = Color.Black)
                ) {
                    Text(stringResource(R.string.pass))
                }
            }
        }
    }
}

// 2. Multi-Touch Modal
@Composable
fun InteractiveMultiTouchModal(
    onClose: () -> Unit,
    onComplete: (Boolean, Int) -> Unit
) {
    var activePoints by remember { mutableIntStateOf(0) }
    var maxPoints by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val count = event.changes.count { it.pressed }
                        activePoints = count
                        if (count > maxPoints) maxPoints = count
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.test_multitouch),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.multitouch_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "$activePoints",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, fontSize = 72.sp),
                color = Color(0xFF00E5FF)
            )
            Text(
                text = "Simultaneous Touch Points",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Max Detected: $maxPoints",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = StatusHealthyGreen
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onComplete(false, maxPoints) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.fail))
                }
                Button(
                    onClick = { onComplete(maxPoints >= 2, maxPoints) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusHealthyGreen, contentColor = Color.Black)
                ) {
                    Text(stringResource(R.string.pass))
                }
            }
        }
    }
}

// 3. Display Dead Pixel Modal
@Composable
fun InteractiveDisplayModal(
    onClose: () -> Unit,
    onComplete: (Boolean) -> Unit
) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black, Color.Gray)
    var colorIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[colorIndex])
            .clickable {
                colorIndex = (colorIndex + 1) % colors.size
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.display_test_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onComplete(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(R.string.fail))
                }
                Button(
                    onClick = { onComplete(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusHealthyGreen, contentColor = Color.Black)
                ) {
                    Text(stringResource(R.string.pass))
                }
            }
        }
    }
}

// 4. Vibration Modal
@Composable
fun InteractiveVibrationModal(
    viewModel: DiagnosticsViewModel,
    onClose: () -> Unit,
    onComplete: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        LuxuryGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            hasGlow = true
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = "Vibrate",
                    tint = LocalAccentColor.current,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.test_vibration),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.did_device_vibrate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.hardwareController.vibrateDevice() },
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAccentColor.current, contentColor = Color.Black)
                ) {
                    Text("Trigger Vibration Motor")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onComplete(false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.fail))
                    }
                    Button(
                        onClick = { onComplete(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusHealthyGreen, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.pass))
                    }
                }
            }
        }
    }
}

// 5. Speaker / Earpiece Modal
@Composable
fun InteractiveSpeakerModal(
    viewModel: DiagnosticsViewModel,
    isEarpiece: Boolean,
    onClose: () -> Unit,
    onComplete: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        LuxuryGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            hasGlow = true
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isEarpiece) Icons.Default.Hearing else Icons.Default.VolumeUp,
                    contentDescription = "Sound",
                    tint = LocalAccentColor.current,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isEarpiece) stringResource(R.string.test_earpiece) else stringResource(R.string.test_speaker),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.did_you_hear_sound),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isPlaying = true
                        coroutineScope.launch {
                            viewModel.hardwareController.playDiagnosticTone(isEarpiece)
                            isPlaying = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAccentColor.current, contentColor = Color.Black)
                ) {
                    Text(if (isPlaying) "Playing Tone…" else stringResource(R.string.play_test_sound))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onComplete(false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.fail))
                    }
                    Button(
                        onClick = { onComplete(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusHealthyGreen, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.pass))
                    }
                }
            }
        }
    }
}

// 6. Microphone Test Modal
@Composable
fun InteractiveMicrophoneModal(
    viewModel: DiagnosticsViewModel,
    onClose: () -> Unit,
    onComplete: (Boolean, Float) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRecording by remember { mutableStateOf(false) }
    var recordedRms by remember { mutableStateOf(0f) }

    val startRecording = {
        isRecording = true
        coroutineScope.launch {
            val rms = viewModel.hardwareController.recordAndAnalyzeMicrophone(3000)
            recordedRms = rms
            isRecording = false
        }
    }

    val audioPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        LuxuryGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            hasGlow = true
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = LocalAccentColor.current,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.test_microphone),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRecording) stringResource(R.string.recording_active) else "Speak or whistle into the phone microphone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                if (recordedRms > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (recordedRms > 400) stringResource(R.string.mic_response_good) else stringResource(R.string.mic_response_weak),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (recordedRms > 400) StatusHealthyGreen else StatusWarningAmber
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val hasAudioPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (hasAudioPerm) {
                            startRecording()
                        } else {
                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAccentColor.current, contentColor = Color.Black)
                ) {
                    Text(if (isRecording) "Recording Waveform…" else stringResource(R.string.record_test))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onComplete(false, recordedRms) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.fail))
                    }
                    Button(
                        onClick = { onComplete(recordedRms > 200, recordedRms) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusHealthyGreen, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.pass))
                    }
                }
            }
        }
    }
}

// 7. Flashlight Modal
@Composable
fun InteractiveFlashlightModal(
    viewModel: DiagnosticsViewModel,
    onClose: () -> Unit,
    onComplete: (Boolean) -> Unit
) {
    var isFlashOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        LuxuryGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            hasGlow = true
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Flash",
                    tint = if (isFlashOn) Color.Yellow else LocalAccentColor.current,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.test_flash),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap button below to toggle device LED torch flashlight.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isFlashOn = viewModel.hardwareController.toggleTorch()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isFlashOn) Color.Yellow else LocalAccentColor.current, contentColor = Color.Black)
                ) {
                    Text(if (isFlashOn) "Torch Active (Turn OFF)" else "Turn Torch ON")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onComplete(false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.fail))
                    }
                    Button(
                        onClick = { onComplete(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusHealthyGreen, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.pass))
                    }
                }
            }
        }
    }
}

// 8. General Hardware test pass/fail modal
@Composable
fun InteractiveSimpleHardwareModal(
    title: String,
    description: String,
    onPass: () -> Unit,
    onFail: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        LuxuryGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            hasGlow = true
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onFail,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.fail))
                    }
                    Button(
                        onClick = onPass,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusHealthyGreen, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.pass))
                    }
                }
            }
        }
    }
}
