package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.StatusCriticalRed
import com.example.ui.theme.StatusHealthyGreen
import com.example.ui.theme.StatusWarningAmber

@Composable
fun LuxuryRingProgress(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 14.dp,
    subText: String = "Health Score",
    primaryColorOverride: Color? = null
) {
    val animatedProgress = remember { Animatable(0f) }
    val accentColor = primaryColorOverride ?: LocalAccentColor.current

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = (score / 100f).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }

    val scoreColor = when {
        score >= 80 -> StatusHealthyGreen
        score >= 60 -> StatusWarningAmber
        else -> StatusCriticalRed
    }

    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = size.toPx()
            val arcSize = canvasSize - strokePx

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc with Gradient
            val gradient = Brush.sweepGradient(
                listOf(
                    accentColor.copy(alpha = 0.6f),
                    scoreColor,
                    accentColor
                )
            )

            drawArc(
                brush = gradient,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress.value,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress.value * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
