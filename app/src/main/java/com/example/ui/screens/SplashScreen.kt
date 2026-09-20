package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.ObsidianBlack

@Composable
fun SplashScreen(
    progress: Float,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val accent = LocalAccentColor.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "splash_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Glowing Luxury Emblem
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .border(1.5.dp, accent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = stringResource(R.string.app_name),
                    tint = accent,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = Color.White
            )

            Text(
                text = "Professional Luxury Diagnostics",
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.8.sp),
                color = accent.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accent,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Developer attribution
            Text(
                text = "Architected by Sina Naderi",
                style = MaterialTheme.typography.labelSmall,
                color = Color.DarkGray
            )
        }
    }
}
